package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.DrenchingBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.Drenching;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class DrenchingLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5862329317115273275L;
	private static final Logger log = Logger.getLogger(DrenchingLayout.class);
	private final DrenchingBll bll;
	private Grid<Drenching> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<Drenching, Void, SerializablePredicate<Drenching>> filterDataProvider;

	public DrenchingLayout() throws IOException {
		super();
		bll = DrenchingBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		buildListView();
	}

	private void buildListView() {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Listado de productos antiparasitarios");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newDrenching());

		Button editButton = new Button("Editar", FontAwesome.PENCIL);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updateDrenching());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deleteDrenching());

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
		grid.addColumn(Drenching::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void buildEditionView(Drenching drenching) {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Producto antiparasitario");
		h1.addStyleName("h1");
		addComponent(h1);

		Button returnButton = new Button("Volver", FontAwesome.UNDO);
		returnButton.addClickListener(e -> buildListView());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(drenching));

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
		txName.setValue(drenching != null ? drenching.getName() : "");
		dataLayout.addComponent(txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<Drenching> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Drenching> filterGrid() {
		SerializablePredicate<Drenching> columnPredicate = null;
		columnPredicate = drenching -> (drenching.getName().toLowerCase()
				.contains(txFilterByName.getValue().toLowerCase()) || txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

	private void newDrenching() {
		buildEditionView(null);
	}

	private void updateDrenching() {
		Drenching drenching = getSelected();
		if (drenching != null) {
			buildEditionView(drenching);
		} else {
			new Notification("No has seleccionado ninguna producto", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private void deleteDrenching() {
		Drenching drenching = getSelected();
		if (drenching != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(drenching);
						}
					});
		} else {
			new Notification("No has seleccionado ninguna producto", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		}
	}

	private Drenching getSelected() {
		Drenching drenching = null;
		Set<Drenching> drenchings = grid.getSelectedItems();
		if (drenchings != null && !drenchings.isEmpty()) {
			drenching = (Drenching) drenchings.toArray()[0];
		}
		return drenching;
	}

	private void delete(Drenching drenching) {
		drenching = Drenching.builder(drenching).enabled(false).build();
		bll.update(drenching);
		fillGridData();
		new Notification("Antiparasitario borrado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}

	private void save(Drenching drenching) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		try {
			if (drenching == null) {
				drenching = Drenching.builder().name(name).company(company).creation(new Date()).enabled(true).build();
				drenching.validate();
				bll.save(drenching);
				new Notification("Antiparasitario guardado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				drenching = Drenching.builder(drenching).name(name).build();
				drenching.validate();
				bll.update(drenching);
				new Notification("Antiparasitario actualizado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification("Los datos del antiparasitario no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}