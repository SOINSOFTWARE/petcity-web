package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.VaccineBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.model.Vaccine;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class VaccineLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5862329317115273275L;
	private static final Logger log = Logger.getLogger(VaccineLayout.class);
	private final VaccineBll bll;
	private Grid<Vaccine> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<Vaccine, Void, SerializablePredicate<Vaccine>> filterDataProvider;

	public VaccineLayout() throws IOException {
		super();
		bll = VaccineBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		buildListView();
	}

	private void buildListView() {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Listado de productos de vacunación");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newVaccine());

		Button editButton = new Button("Editar", FontAwesome.PENCIL);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updateVaccine());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deleteVaccine());

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(newButton, editButton, deleteButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);

		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);
		filterLayout.setMargin(true);
		filterLayout.addComponent(txFilterByName);

		Panel filterPanel = new Panel("Filtrar por");
		filterPanel.setContent(filterLayout);
		filterPanel.addStyleName("well");
		addComponent(filterPanel);

		grid = new Grid<>();
		fillGridData();
		grid.addColumn(Vaccine::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void buildEditionView(Vaccine vaccine) {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Producto de vacunación");
		h1.addStyleName("h1");
		addComponent(h1);

		Button returnButton = new Button("Volver", FontAwesome.UNDO);
		returnButton.addClickListener(e -> buildListView());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(vaccine));

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(returnButton, saveButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);

		VerticalLayout dataLayout = new VerticalLayout();
		dataLayout.setWidth("40%");
		dataLayout.setSpacing(true);
		dataLayout.setMargin(true);
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(vaccine != null ? vaccine.getName() : "");
		dataLayout.addComponent(txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<Vaccine> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Vaccine> filterGrid() {
		SerializablePredicate<Vaccine> columnPredicate = null;
		columnPredicate = vaccine -> (vaccine.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

	private void newVaccine() {
		buildEditionView(null);
	}

	private void updateVaccine() {
		Vaccine vaccine = getSelected();
		if (vaccine != null) {
			buildEditionView(vaccine);
		} else {
			new Notification("No has seleccionado ninguna producto", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private void deleteVaccine() {
		Vaccine vaccine = getSelected();
		if (vaccine != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(vaccine);
						}
					});
		} else {
			new Notification("No has seleccionado ninguna producto", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private Vaccine getSelected() {
		Vaccine vaccine = null;
		Set<Vaccine> vaccines = grid.getSelectedItems();
		if (vaccines != null && !vaccines.isEmpty()) {
			vaccine = (Vaccine) vaccines.toArray()[0];
		}
		return vaccine;
	}

	private void delete(Vaccine vaccine) {
		vaccine = Vaccine.builder(vaccine).enabled(false).build();
		bll.update(vaccine);
		fillGridData();
		new Notification("Vacuna borrada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}

	private void save(Vaccine vaccine) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		try {
			if (vaccine == null) {
				vaccine = Vaccine.builder().name(name).company(company).creation(new Date()).enabled(true).build();
				vaccine.validate();
				bll.save(vaccine);
				new Notification("Vacuna guardada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				vaccine = Vaccine.builder(vaccine).name(name).build();
				vaccine.validate();
				bll.update(vaccine);
				new Notification("Vacuna actualizada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification("Los datos de la vacuna no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}