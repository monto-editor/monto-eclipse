package monto.eclipse.launching;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import monto.eclipse.Activator;
import monto.eclipse.launching.debug.MontoDebugTarget;
import monto.eclipse.launching.debug.MontoLineBreakpoint;
import monto.service.command.CommandMessage;
import monto.service.command.Commands;
import monto.service.gson.GsonMonto;
import monto.service.launching.DebugLaunchConfiguration;
import monto.service.launching.LaunchConfiguration;
import monto.service.launching.debug.Breakpoint;
import monto.service.product.Products;
import monto.service.types.Language;
import monto.service.types.Source;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
  
  private static int sessionIdCounter = 0;
  
  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    String mainClassPhysicalName =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_PHYSICAL_NAME, "");
    String mainClassLogicalName =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_LOGICAL_NAME, "");
    String mainClassLanguage =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_LANGUAGE, "");

    Source source = new Source(mainClassPhysicalName, mainClassLogicalName);
    Language language = new Language(mainClassLanguage);

    if (mode.equals("run")) {
      sessionIdCounter += 1;

      Activator.sendCommandMessage(
          new CommandMessage(sessionIdCounter, 1, Commands.RUN, language,
              GsonMonto.toJsonTree(new LaunchConfiguration(source))));

      launch.addProcess(createMontoProcess(launch, sessionIdCounter, mode, language));
    } else if (mode.equals("debug")) {
      sessionIdCounter += 1;

      IBreakpoint[] eclipseBreakpoints =
          DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(Activator.PLUGIN_ID);

      List<Breakpoint> breakpoints = Arrays.stream(eclipseBreakpoints)
          .filter(eclipseBreakpoint -> (eclipseBreakpoint != null && eclipseBreakpoint instanceof MontoLineBreakpoint))
          .map(MontoLineBreakpoint.class::cast)
          .flatMap(MontoLineBreakpoint::getBreakpointStream)
          .collect(Collectors.toList());

      Activator.sendCommandMessage(new CommandMessage(sessionIdCounter, 1,
          Commands.DEBUG, language,
          GsonMonto.toJsonTree(new DebugLaunchConfiguration(source, breakpoints))));
      
      MontoProcess process = createMontoProcess(launch, sessionIdCounter, mode, language);
      MontoDebugTarget debugTarget = new MontoDebugTarget(sessionIdCounter, language, launch, process);
      DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(debugTarget);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.HIT_BREAKPOINT,
          debugTarget::onBreakpointHit, debugTarget);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.THREADS_RESUMED,
          debugTarget::onThreadsResumed, debugTarget);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.PROCESS_TERMINATED,
          debugTarget::onProcessTerminated, debugTarget);
      launch.addProcess(process);
      launch.addDebugTarget(debugTarget);
      debugTarget.fireEvent(DebugEvent.CREATE);
    }
  }

  MontoProcess createMontoProcess(ILaunch launch, int sessionId, String mode, Language language) {
    MontoProcess process = new MontoProcess(launch, sessionId, mode, language);
    Activator.getDefault().getDemultiplexer().addProductListener(Products.STREAM_OUTPUT,
        process::onStreamOutputProduct, process);

    Activator.getDefault().getDemultiplexer().addProductListener(Products.PROCESS_TERMINATED,
        process::onProcessTerminatedProduct, process);
    return process;
  }
}
