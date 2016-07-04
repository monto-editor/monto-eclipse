package monto.eclipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.javatuples.Pair;

import monto.service.configuration.Option;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.DiscoveryResponse;
import monto.service.types.ServiceId;

@SuppressWarnings("rawtypes")
public class ServiceConfigurationPage extends PropertyPage implements IWorkbenchPropertyPage {
  private Map<ServiceId, List<Pair<Option, Control>>> controlMap;
  private DiscoveryResponse discoveryResponse;

  @SuppressWarnings("unchecked")
  @Override
  protected Control createContents(Composite parent) {
    controlMap = new HashMap<>();
    TabFolder folder = new TabFolder(parent, SWT.BORDER);
    Activator.getDefault().discover(DiscoveryRequest.create()).ifPresent(discoverResponse -> {
      this.discoveryResponse = discoverResponse;
      discoverResponse.get().forEach(serviceDescription -> {
        // if there are no options, avoid creating an empty configuration section
        if (serviceDescription.getOptions().size() == 0)
          return;

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(serviceDescription.getLabel());
        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.VERTICAL));

        ArrayList<Pair<Option, Control>> optionControlPairs = new ArrayList<>();
        controlMap.put(serviceDescription.getServiceId(), optionControlPairs);
        serviceDescription.getOptions().forEach(option -> {
          createControls(serviceDescription.getServiceId(), option, optionControlPairs, composite);
        });

        item.setControl(composite);
        composite.pack();
      });
    });

    folder.pack();
    return folder;
  }

  <T> void createControls(ServiceId serviceId, Option<T> option,
      List<Pair<Option, Control>> controlPairList, Composite parent) {
    IPreferenceStore store = getPreferenceStore();
    String storeKey = Activator.getStoreKey(serviceId, option);

    @SuppressWarnings("unchecked")
    Control control = option.<Control>match(booleanOption -> {
      Button button = new Button(parent, SWT.CHECK);
      button.setText(booleanOption.getLabel());
      button.setSelection(store.getBoolean(storeKey));
      controlPairList.add(new Pair(option, button));
      return button;
    }, numberOption -> {
      Spinner spinner = new Spinner(parent, SWT.NONE);
      spinner.setMinimum((int) numberOption.getFrom());
      spinner.setMaximum((int) numberOption.getTo());
      spinner.setSelection(store.getInt(storeKey));
      controlPairList.add(new Pair(option, spinner));
      return spinner;
    }, textOption -> {
      Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
      text.setText(store.getString(storeKey));
      controlPairList.add(new Pair(option, text));
      return text;
    }, xorOption -> {
      Combo combo = new Combo(parent, SWT.DROP_DOWN);
      String[] items = new String[xorOption.getValues().size()];
      items = xorOption.getValues().toArray(items);
      combo.setItems(items);
      combo.select(xorOption.getValues().indexOf(store.getInt(storeKey)));
      controlPairList.add(new Pair(option, combo));
      return combo;
    }, optionGroup -> {
      Group group = new Group(parent, SWT.BORDER);
      group.setText(optionGroup.getLabel());
      optionGroup.getMembers().forEach(memberOption -> {
        createControls(serviceId, memberOption, controlPairList, group);
      });
      group.setLayout(new RowLayout(SWT.VERTICAL));
      return group;
    });

    control.pack();
  }

  public void storeOptionValues() {
    IPreferenceStore store = getPreferenceStore();

    controlMap.forEach((serviceId, optionControlPairs) -> {
      optionControlPairs.forEach(optionControlPair -> {
        Option<?> option = optionControlPair.getValue0();
        Control control = optionControlPair.getValue1();
        String storeKey = Activator.getStoreKey(serviceId, option);

        option.matchVoid(booleanOption -> {
          store.setValue(storeKey, ((Button) control).getSelection());
        }, numberOption -> {
          store.setValue(storeKey, ((Spinner) control).getSelection());
        }, textOption -> {
          store.setValue(storeKey, ((Text) control).getText());
        }, xorOption -> {
          store.setValue(storeKey, ((Combo) control).getSelectionIndex());
        }, optionGroup -> {
        });
      });
    });
  }

  @Override
  protected IPreferenceStore doGetPreferenceStore() {
    return Activator.getDefault().getPreferenceStore();
  }

  @Override
  public boolean performOk() {
    // also called on Apply button click
    storeOptionValues();
    Activator.sendConfigurationsFromStore(discoveryResponse.get());
    return true;
  }
}
