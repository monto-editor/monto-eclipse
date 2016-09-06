package monto.eclipse.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import monto.eclipse.Activator;
import monto.service.product.Products;
import monto.service.run.LaunchConfiguration;
import monto.service.types.ServiceId;
import monto.service.types.Source;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
  private static int launchSessionIdCounter = 0;

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    if (mode.equals("run")) {
      String mainClass =
          configuration.getAttribute(MainClassLaunchConfigurationTab.ATTR_MAIN_CLASS, "");

      launchSessionIdCounter += 1;

      MontoProcess process = new MontoProcess(launch, launchSessionIdCounter);
      Activator.getDefault().getDemultiplexer().addProductListener(Products.STREAM_OUTPUT,
          process::onStreamOutputProduct);

      Activator.getDefault().getDemultiplexer().addProductListener(Products.PROCESS_TERMINATED,
          process::onProcessTerminatedProduct);

      launch.addProcess(process);

      Activator.sendCommandMessage(LaunchConfiguration.createCommandMessage(launchSessionIdCounter,
          1, new ServiceId("javaRunner"), mode, new Source(mainClass)));
    }
  }
}
