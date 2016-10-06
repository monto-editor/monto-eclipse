package monto.eclipse.launching;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import monto.eclipse.Activator;
import monto.eclipse.launching.debug.MontoEclipseDebugTarget;
import monto.service.product.Products;
import monto.service.launching.DebugLaunchConfiguration;
import monto.service.launching.LaunchConfiguration;
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
      MontoEclipseDebugTarget debugTarget = new MontoEclipseDebugTarget(debugSessionIdCounter, launch, process);
      launch.addDebugTarget(debugTarget);

      String mainClass =
          configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_MAIN_CLASS, "");

      Activator
          .sendCommandMessage(DebugLaunchConfiguration.createCommandMessage(debugSessionIdCounter,
              1, new ServiceId("javaDebugger"), mode, new Source(mainClass, "JRunnable"), new ArrayList<>()));
      
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
