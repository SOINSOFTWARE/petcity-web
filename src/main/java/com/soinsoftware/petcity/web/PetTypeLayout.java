package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.PetTypeBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.PetType;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;

@SuppressWarnings("deprecation")
public class PetTypeLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5862329317115273275L;
	private static final Logger log = Logger.getLogger(PetTypeLayout.class);
	private final PetTypeBll bll;
	private Grid<PetType> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<PetType, Void, SerializablePredicate<PetType>> filterYourObjectDataProvider;

	public PetTypeLayout() throws IOException {
		super();
		bll = PetTypeBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		buildListView();
	}

	private void buildListView() {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Listado de especies");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newPetType());

		Button editButton = new Button("Editar", FontAwesome.PENCIL);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updatePetType());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deletePetType());

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(newButton, editButton, deleteButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);

		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshyourObjectGrid());

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
		grid.addColumn(PetType::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void buildEditionView(PetType petType) {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Especies");
		h1.addStyleName("h1");
		addComponent(h1);

		Button returnButton = new Button("Volver", FontAwesome.UNDO);
		returnButton.addClickListener(e -> buildListView());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(petType));

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
		txName.setValue(petType != null ? petType.getName() : "");
		dataLayout.addComponent(txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<PetType> dataProvider = new ListDataProvider<>(bll.select(company));
		filterYourObjectDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterYourObjectDataProvider);
	}

	private void refreshyourObjectGrid() {
		filterYourObjectDataProvider.setFilter(filterYourObjectGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<PetType> filterYourObjectGrid() {
		SerializablePredicate<PetType> columnPredicate = null;
		columnPredicate = petType -> (petType.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

	private void newPetType() {
		buildEditionView(null);
	}

	private void updatePetType() {
		PetType petType = getSelected();
		if (petType != null && petType.getCompany() != null) {
			buildEditionView(petType);
		} else {
			String message;
			if (petType == null) {
				message = "No has seleccionado ninguna especie";
			} else {
				message = "Esta especie no puede ser editada";
			}
			new Notification(message, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private void deletePetType() {
		PetType petType = getSelected();
		if (petType != null && petType.getCompany() != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(petType);
						}
					});
		} else {
			String message;
			if (petType == null) {
				message = "No has seleccionado ninguna especie";
			} else {
				message = "Esta especie no puede ser eliminada";
			}
			new Notification(message, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private PetType getSelected() {
		PetType petType = null;
		Set<PetType> petTypes = grid.getSelectedItems();
		if (petTypes != null && !petTypes.isEmpty()) {
			petType = (PetType) petTypes.toArray()[0];
		}
		return petType;
	}

	private void delete(PetType petType) {
		petType = PetType.builder(petType).enabled(false).build();
		bll.update(petType);
		fillGridData();
	}

	private void save(PetType petType) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		try {
			if (petType == null) {
				petType = PetType.builder().name(name).company(company).creation(new Date()).enabled(true).build();
				petType.validate();
				bll.save(petType);
			} else {
				petType = PetType.builder(petType).name(name).build();
				petType.validate();
				bll.update(petType);
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification("Los datos de la especie no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}