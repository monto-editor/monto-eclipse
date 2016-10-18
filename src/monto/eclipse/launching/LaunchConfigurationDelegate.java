package monto.eclipse.launching;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private static int runSessionIdCounter = 0;
  private static int debugSessionIdCounter = 1000000000;

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    String mainClassPhysicalName =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_PHYSICAL_NAME, "");
    String mainClassLogicalName =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_LOGICAL_NAME, "");
    String mainClassLanguage =
        configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_LAGUAGE, "");

    Source source = new Source(mainClassPhysicalName, mainClassLogicalName);
    Language language = new Language(mainClassLanguage);

    if (mode.equals("run")) {
      runSessionIdCounter += 1;

      Activator.sendCommandMessage(
          new CommandMessage(runSessionIdCounter, 1, Commands.RUN_LAUNCH_CONFIGURATION, language,
              GsonMonto.toJsonTree(new LaunchConfiguration(source))));

      launch.addProcess(createMontoProcess(launch, runSessionIdCounter, mode, language));
    } else if (mode.equals("debug")) {
      debugSessionIdCounter += 1;

      IBreakpoint[] eclipseBreakpoints =
          DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(Activator.PLUGIN_ID);

      List<Breakpoint> breakpoints =
          Arrays.stream(eclipseBreakpoints).flatMap(eclipseBreakpoint -> {
            if (eclipseBreakpoint != null && eclipseBreakpoint instanceof MontoLineBreakpoint) {
              MontoLineBreakpoint montoBreakpoint = (MontoLineBreakpoint) eclipseBreakpoint;
              try {
                return Stream.of(
                    new Breakpoint(montoBreakpoint.getSource(), montoBreakpoint.getLineNumber()));
              } catch (CoreException e) {
                System.err.printf("Couldn't translate MontoLineBreakpoint to Breakpoint: %s (%s)",
                    e.getClass().getName(), e.getMessage());
                e.printStackTrace();
              }
            }
            return Stream.empty();
          }).collect(Collectors.toList());

      Activator.sendCommandMessage(new CommandMessage(debugSessionIdCounter, 1,
          Commands.DEBUG_LAUNCH_CONFIGURATION, language,
          GsonMonto.toJsonTree(new DebugLaunchConfiguration(source, breakpoints))));
      
      MontoProcess process = createMontoProcess(launch, debugSessionIdCounter, mode, language);
      MontoDebugTarget debugTarget = new MontoDebugTarget(debugSessionIdCounter, language, launch, process);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.HIT_BREAKPOINT,
          debugTarget::onBreakpointHit);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.THREADS_RESUMED,
          debugTarget::onThreadsResumed);
      launch.addProcess(process);
      launch.addDebugTarget(debugTarget);
      debugTarget.fireEvent(DebugEvent.CREATE);
    }
  }

  MontoProcess createMontoProcess(ILaunch launch, int sessionId, String mode, Language language) {
    MontoProcess process = new MontoProcess(launch, sessionId, mode, language);
    Activator.getDefault().getDemultiplexer().addProductListener(Products.STREAM_OUTPUT,
        process::onStreamOutputProduct);

    Activator.getDefault().getDemultiplexer().addProductListener(Products.PROCESS_TERMINATED,
        process::onProcessTerminatedProduct);
    return process;
  }
}
