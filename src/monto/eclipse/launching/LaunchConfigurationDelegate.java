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
import monto.service.launching.DebugLaunchConfiguration;
import monto.service.launching.LaunchConfiguration;
import monto.service.launching.debug.Breakpoint;
import monto.service.product.Products;
import monto.service.types.Product;
import monto.service.types.ServiceId;
import monto.service.types.Source;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
  private static int runSessionIdCounter = 0;
  private static int debugSessionIdCounter = 0;

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    if (mode.equals("run")) {
      runSessionIdCounter += 1;
      launch.addProcess(createMontoProcess(launch, runSessionIdCounter));

      String mainClass =
          configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_MAIN_CLASS, "");
      Activator.sendCommandMessage(LaunchConfiguration.createCommandMessage(runSessionIdCounter, 1,
          new ServiceId("javaRunner"), mode, new Source(mainClass)));
    } else if (mode.equals("debug")) {
      debugSessionIdCounter += 1;
      MontoProcess process = createMontoProcess(launch, debugSessionIdCounter);
      MontoDebugTarget debugTarget = new MontoDebugTarget(debugSessionIdCounter, launch, process);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.HIT_BREAKPOINT, debugTarget::onBreakpointHit);
      launch.addDebugTarget(debugTarget);

      String mainClass =
          configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_MAIN_CLASS, "");

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

      Activator.sendCommandMessage(DebugLaunchConfiguration.createCommandMessage(
          debugSessionIdCounter, 1, new ServiceId("javaDebugger"), mode,
          new Source(mainClass /*
                                * TODO: Source should not be created here, instead Source of opened
                                * MontoParseController should be used
                                */), breakpoints));

      debugTarget.fireEvent(DebugEvent.CREATE, DebugEvent.UNSPECIFIED);
    }
  }

  MontoProcess createMontoProcess(ILaunch launch, int sessionId) {
    MontoProcess process = new MontoProcess(launch, runSessionIdCounter);
    Activator.getDefault().getDemultiplexer().addProductListener(Products.STREAM_OUTPUT,
        process::onStreamOutputProduct);

    Activator.getDefault().getDemultiplexer().addProductListener(Products.PROCESS_TERMINATED,
        process::onProcessTerminatedProduct);
    return process;
  }
}
