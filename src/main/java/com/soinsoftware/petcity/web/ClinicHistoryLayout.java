package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.ClinicHistoryBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.ClinicHistory;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.User;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class ClinicHistoryLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5862329317115273275L;
	private static final Logger log = Logger.getLogger(ClinicHistoryLayout.class);
	private final ClinicHistoryBll bll;
	private TabSheet tabSheet;
	private Grid<ClinicHistory> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<ClinicHistory, Void, SerializablePredicate<ClinicHistory>> filterDataProvider;

	public ClinicHistoryLayout() throws IOException {
		super();
		bll = ClinicHistoryBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		setMargin(true);
		buildListView();
	}

	private void buildListView() {
		Label h1 = new Label("Historias clínicas");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newClinicHistory());

		Button editButton = new Button("Editar", FontAwesome.EDIT);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updateClinicHistory());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deleteClinicHistory());

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(newButton, editButton, deleteButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");

		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);
		filterLayout.setMargin(true);
		filterLayout.addComponent(txFilterByName);

		Panel filterPanel = new Panel("Filtrar por");
		filterPanel.setContent(filterLayout);
		filterPanel.addStyleName("well");

		grid = new Grid<>();
		fillGridData();
		grid.addColumn(ClinicHistory::getRecordCustomId).setCaption("# Historia");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getName()).setCaption("Mascota");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getPetType().getName()).setCaption("Especie");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getBreed().getName()).setCaption("Raza");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getName() + " "
				+ clinicHistory.getPet().getOwner().getLastName()).setCaption("Propietario");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getDocument()).setCaption("# Documento");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getPhone2()).setCaption("# Celular");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");

		VerticalLayout listLayout = new VerticalLayout();
		listLayout.addComponents(buttonPanel, filterPanel, dataPanel);

		tabSheet = new TabSheet();
		tabSheet.addStyleName("framed");
		Tab tab = tabSheet.addTab(listLayout, "Listado");
		tab.setIcon(FontAwesome.LIST);

		addComponent(tabSheet);
	}

	private void buildEditionView(ClinicHistory clinicHistory) {
		Button cancelButton = new Button("Cancelar", FontAwesome.CLOSE);
		cancelButton.addStyleName("danger");
		cancelButton.addClickListener(e -> removeEditionTab());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(clinicHistory));

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(cancelButton, saveButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");

		VerticalLayout dataLayout = new VerticalLayout();
		dataLayout.setWidth("40%");
		dataLayout.setSpacing(true);
		dataLayout.setMargin(true);
		txName = new TextField("Nombre");
		txName.setSizeFull();
		dataLayout.addComponent(txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");

		VerticalLayout editionLayout = new VerticalLayout();
		editionLayout.addComponents(buttonPanel, dataPanel);
		removeEditionTab();
		Tab tab = tabSheet.addTab(editionLayout, clinicHistory == null ? "Nueva" : buildEditionTabTitle(clinicHistory));
		tab.setIcon(clinicHistory == null ? FontAwesome.PLUS : FontAwesome.EDIT);
		tabSheet.setSelectedTab(1);
	}

	private void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<ClinicHistory> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<ClinicHistory> filterGrid() {
		SerializablePredicate<ClinicHistory> columnPredicate = null;
		columnPredicate = clinicHistory -> (((ClinicHistory) clinicHistory).getPet().getName().toLowerCase()
				.contains(txFilterByName.getValue().toLowerCase()) || txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

	private void removeEditionTab() {
		Tab tab = tabSheet.getTab(1);
		if (tab != null) {
			tabSheet.removeTab(tab);
		}
	}

	private String buildEditionTabTitle(ClinicHistory clinicHistory) {
		String recordId = clinicHistory.getRecordCustomId() != null ? clinicHistory.getRecordCustomId().toString() : "";
		return "Historia clínica #" + recordId + " - " + clinicHistory.getPet().getName();
	}

	private void newClinicHistory() {
		buildEditionView(null);
	}

	private void updateClinicHistory() {
		ClinicHistory clinicHistory = getSelected();
		if (clinicHistory != null) {
			buildEditionView(clinicHistory);
		} else {
			new Notification("No has seleccionado ninguna historia", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private void deleteClinicHistory() {
		ClinicHistory clinicHistory = getSelected();
		if (clinicHistory != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(clinicHistory);
						}
					});
		} else {
			new Notification("No has seleccionado ninguna historia", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private ClinicHistory getSelected() {
		ClinicHistory clinicHistory = null;
		Set<ClinicHistory> clinicHistories = grid.getSelectedItems();
		if (clinicHistories != null && !clinicHistories.isEmpty()) {
			clinicHistory = (ClinicHistory) clinicHistories.toArray()[0];
		}
		return clinicHistory;
	}

	private void delete(ClinicHistory clinicHistory) {
		clinicHistory = ClinicHistory.builder(clinicHistory).enabled(false).build();
		bll.save(clinicHistory);
		fillGridData();
		new Notification("Historia borrada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}

	private void save(ClinicHistory clinicHistory) {
		Company company = getSession().getAttribute(User.class).getCompany();
		try {
			if (clinicHistory == null) {
				clinicHistory = ClinicHistory.builder().company(company).creation(new Date()).enabled(true).build();
				clinicHistory.validate();
				bll.save(clinicHistory);
				new Notification("Historia guardada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				clinicHistory = ClinicHistory.builder(clinicHistory).build();
				clinicHistory.validate();
				bll.save(clinicHistory);
				new Notification("Historia actualizada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification(
					"Los datos de la historia no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}