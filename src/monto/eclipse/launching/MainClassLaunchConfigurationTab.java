package monto.eclipse.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MainClassLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
  public static final String ATTR_MAIN_CLASS = "montoMainClass";

  private Text mainClassNameText;
  private String lastSavedMainClassText;

  @Override
  public void createControl(Composite parent) {
    Composite control = new Composite(parent, SWT.NONE);
    control.setLayout(new GridLayout(3, false));
    control.setFont(parent.getFont());
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 1;
    control.setLayoutData(gd);

    setupMainClassControls(control);
    lastSavedMainClassText = "";
    setControl(control);
  }

  private void setupMainClassControls(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Main class:");
    GridData gdLabel = new GridData(SWT.LEAD);
    gdLabel.horizontalSpan = 1;
    label.setLayoutData(gdLabel);

    mainClassNameText = new Text(parent, SWT.BORDER);
    GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
    gdText.horizontalSpan = 1;
    mainClassNameText.setLayoutData(gdText);
    mainClassNameText.addModifyListener(e -> {
      updateLaunchConfigurationDialog();
      /*
       * updateLaunchConfigurationDialog() triggers a performApply(). The resulting
       * ILaunchConfigurationWorkingCopy is then compared to the old ILaunchConfigurationWorkingCopy
       * by a comparator provided by the framework and the apply button is enabled if the two
       * working copies differ. There is also a setDirty() and isDirty() method, that can be
       * overridden is this class, but those don't affect the UI button, they are only for handling
       * state internally.
       */
    });

    Button browseButton = createPushButton(parent, "Browse", null);
    GridData gdButton = new GridData(SWT.TRAIL);
    gdButton.horizontalSpan = 1;
    browseButton.setLayoutData(gdButton);

    browseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        System.out.println("Browse class dialog should open here.");
      }
    });
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(ATTR_MAIN_CLASS, "");
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    lastSavedMainClassText = "";

    try {
      lastSavedMainClassText = configuration.getAttribute(ATTR_MAIN_CLASS, "");
    } catch (CoreException ignored) {
    }

    mainClassNameText.setText(lastSavedMainClassText);
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    lastSavedMainClassText = mainClassNameText.getText();
    configuration.setAttribute(ATTR_MAIN_CLASS, lastSavedMainClassText);
  }

  @Override
  public String getName() {
    return "Main class";
  }
}
