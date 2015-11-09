package monto.eclipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.javatuples.Pair;

import monto.service.configuration.BooleanConfiguration;
import monto.service.configuration.NumberConfiguration;
import monto.service.configuration.Option;
import monto.service.configuration.OptionGroup;
import monto.service.configuration.TextConfiguration;
import monto.service.discovery.DiscoveryRequest;
import monto.service.discovery.LanguageFilter;
import monto.service.discovery.ServiceDescription;

public class ServiceConfigurationPage extends PropertyPage implements IWorkbenchPropertyPage {

	public ServiceConfigurationPage() {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createContents(Composite parent) {
		DiscoveryRequest request;
		try {
			IFileEditorInput editorInput = (IFileEditorInput) getElement();
			UniversalEditor editor = (UniversalEditor) EditorUtility.isOpenInEditor(editorInput);
			String language = editor.getLanguage().getName();
			request = new DiscoveryRequest(new LanguageFilter(language));
		} catch (Exception e) {
			request = new DiscoveryRequest();
		}
		
		List<ServiceDescription> services = Activator.discover(request)
			.map(resp -> resp.getServices())
			.orElse(new ArrayList<>());
		TabFolder folder = new TabFolder(parent, SWT.BORDER);
		services.forEach(service -> {
			// if there are no options, avoid creating and empty configuration section
			if(service.getOptions().size() == 0)
				return;
			
			Map<String,Button> controls = new HashMap<>();
			List<Pair<OptionGroup,Group>> optionGroups = new ArrayList<>();
			TabItem item = new TabItem(folder,SWT.NONE);
			item.setText(service.getLabel());
			Composite composite = new Composite(folder,SWT.NONE);
			composite.setLayout(new RowLayout(SWT.VERTICAL));
			service.getOptions().forEach(option -> {
				createOptions(service.getServiceID(),option,controls,optionGroups,composite);	
			});
			optionGroups.forEach(pair -> {
				OptionGroup optionGroup = pair.getValue0();
				Group controlGroup = pair.getValue1();
				Button button = controls.get(optionGroup.getRequiredOption());
				if(button == null) {
					Activator.debug("Could not find boolean option %s required by option group",
							optionGroup.getRequiredOption());
				} else { 
					button.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							setEnabled(button,controlGroup);
						}
					});
					setEnabled(button,controlGroup);
				}
			});
			item.setControl(composite);
			composite.pack();
		});
		folder.pack();
		return folder;
	}
	
	<T> void createOptions(String serviceID, Option<T> option, Map<String,Button> controls, List<Pair<OptionGroup,Group>> optionGroups, Composite parent) {
		@SuppressWarnings("unchecked")
		Control control = option.<Control>match(
        		booleanOption -> {
        			Button button = new Button(parent,SWT.CHECK);
        			button.setText(booleanOption.getLabel());
        			button.setSelection(booleanOption.getDefaultValue());
        			controls.put(booleanOption.getOptionId(),button);
        			button.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					BooleanConfiguration conf = new BooleanConfiguration(booleanOption.getOptionId(), button.getSelection()); 
        					Activator.configure(serviceID,conf);
        				}
					});
        			return button;
        		},
        		numberOption -> {
        	        Spinner spinner = new Spinner(parent, SWT.NONE);
        	        spinner.setMinimum((int) numberOption.getFrom());
        	        spinner.setMaximum((int) numberOption.getTo());
        	        spinner.setSelection(numberOption.getDefaultValue().intValue());
        	        spinner.addSelectionListener(new SelectionAdapter() {
        	        	@Override
        				public void widgetSelected(SelectionEvent e) {
        					NumberConfiguration conf = new NumberConfiguration(numberOption.getOptionId(), spinner.getSelection()); 
        					Activator.configure(serviceID,conf);
        				}
					});
        	        return spinner;
        		},
        		textOption -> {
        			Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        			text.setText(textOption.getDefaultValue());
        			text.addVerifyListener(new VerifyListener() {
						@Override
						public void verifyText(VerifyEvent e) {
							TextConfiguration conf = new TextConfiguration(textOption.getOptionId(), text.getText());
							
							Activator.configure(serviceID,conf);
						}
					});;
        			return text;
        		},
        		xorOption -> {
        			Combo combo = new Combo(parent, SWT.DROP_DOWN);
        			String[] items = new String[xorOption.getValues().size()];
        			items = xorOption.getValues().toArray(items);
        			combo.setItems(items);
        			combo.select(xorOption.getValues().indexOf(xorOption.getDefaultValue()));
        			combo.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					String selected = combo.getItem(combo.getSelectionIndex());
        					TextConfiguration conf = new TextConfiguration(xorOption.getOptionId(), selected); 
        					Activator.configure(serviceID,conf);
        				}
					});
        			return combo;
        		},
        		optionGroup -> {
        			Group group = new Group(parent, SWT.BORDER);
        			optionGroup.getMembers().forEach(opt -> {
        				createOptions(serviceID,opt, controls, optionGroups, group);
        			});
        			optionGroups.add(new Pair<OptionGroup,Group>(optionGroup,group));
        			group.setLayout(new RowLayout(SWT.VERTICAL));
        			return group;
        		});
		control.pack();
	}
	
	private void setEnabled(Button button, Group group) {
		boolean enabled = button.getSelection();
		group.setEnabled(enabled);
		for(Control child : group.getChildren())
			child.setEnabled(enabled);
	}
}
