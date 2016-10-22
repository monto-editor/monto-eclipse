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
  public static final String ATTR_PHYSICAL_NAME = "montoMainClassPhysical";
  public static final String ATTR_LOGICAL_NAME = "montoMainClassLogical";
  public static final String ATTR_LANGUAGE = "montoMainClassLanguage";

  private Text physicalNameText;
  private Text logicalNameText;
  private Text languageText;

  @Override
  public void createControl(Composite parent) {
    Composite control = new Composite(parent, SWT.NONE);
    control.setLayout(new GridLayout(3, false));
    control.setFont(parent.getFont());
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 1;
    control.setLayoutData(gd);

    setupMainClassControls(control);
    setControl(control);
  }

  private void setupMainClassControls(Composite parent) {

    // PHYSICAL NAME UI ELEMENTS

    Label physicalNameLabel = new Label(parent, SWT.NONE);
    physicalNameLabel.setText("Physical name of main class:");
    GridData gdPhysicalNameLabel = new GridData(SWT.LEAD);
    gdPhysicalNameLabel.horizontalSpan = 1;
    physicalNameLabel.setLayoutData(gdPhysicalNameLabel);

    physicalNameText = new Text(parent, SWT.BORDER);
    GridData gdPhysicalNameText = new GridData(GridData.FILL_HORIZONTAL);
    gdPhysicalNameText.horizontalSpan = 1;
    physicalNameText.setLayoutData(gdPhysicalNameText);
    physicalNameText.addModifyListener(e -> {
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

    // LOGICAL NAME UI ELEMENTS

    Label logicalNameLabel = new Label(parent, SWT.NONE);
    logicalNameLabel.setText("Logical name of main class:");
    GridData gdLogicalNameLabel = new GridData(SWT.LEAD);
    gdLogicalNameLabel.horizontalSpan = 1;
    logicalNameLabel.setLayoutData(gdLogicalNameLabel);

    logicalNameText = new Text(parent, SWT.BORDER);
    GridData gdLogicalNameText = new GridData(GridData.FILL_HORIZONTAL);
    gdLogicalNameText.horizontalSpan = 1;
    logicalNameText.setLayoutData(gdLogicalNameText);
    logicalNameText.addModifyListener(e -> {
      updateLaunchConfigurationDialog();
    });

    // LANGUAGE UI ELEMENTS

    Label languageLabel = new Label(parent, SWT.NONE);
    languageLabel.setText("Project language:");
    GridData gdLanguageLabel = new GridData(SWT.LEAD);
    gdLanguageLabel.horizontalSpan = 1;
    languageLabel.setLayoutData(gdLanguageLabel);

    languageText = new Text(parent, SWT.BORDER);
    GridData gdLanguageText = new GridData(GridData.FILL_HORIZONTAL);
    gdLanguageText.horizontalSpan = 1;
    languageText.setLayoutData(gdLanguageText);
    languageText.addModifyListener(e -> {
      updateLaunchConfigurationDialog();
    });
    
    // BROWSE BUTTON

    Button browseButton = createPushButton(parent, "Browse", null);
    GridData gdBrowseButton = new GridData(SWT.TRAIL);
    gdBrowseButton.horizontalSpan = 1;
    gdBrowseButton.verticalSpan = 3;
    browseButton.setLayoutData(gdBrowseButton);

    browseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        System.out.println("Browse class dialog should open here.");
      }
    });

  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(ATTR_PHYSICAL_NAME, "");
    configuration.setAttribute(ATTR_LOGICAL_NAME, "");
    configuration.setAttribute(ATTR_LANGUAGE, "");
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    String physicalName = "";
    String logicalName = "";
    String language = "";

    try {
      physicalName = configuration.getAttribute(ATTR_PHYSICAL_NAME, "");
      logicalName = configuration.getAttribute(ATTR_LOGICAL_NAME, "");
      language = configuration.getAttribute(ATTR_LANGUAGE, "");
    } catch (CoreException ignored) {
    }

    physicalNameText.setText(physicalName);
    logicalNameText.setText(logicalName);
    languageText.setText(language);
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(ATTR_PHYSICAL_NAME, physicalNameText.getText());
    configuration.setAttribute(ATTR_LOGICAL_NAME, physicalNameText.getText());
    configuration.setAttribute(ATTR_LANGUAGE, languageText.getText());
  }

  @Override
  public String getName() {
    return "Main class";
  }
}
