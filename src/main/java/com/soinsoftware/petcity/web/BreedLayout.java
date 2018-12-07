// Soin Software, 2018
package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.BreedBll;
import com.soinsoftware.petcity.bll.PetTypeBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Breed;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Carlos Rodriguez
 * @since 22/11/2018
 */
@SuppressWarnings("deprecation")
public class BreedLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -6868887503188086381L;
	private static final Logger log = Logger.getLogger(BreedLayout.class);
	private final BreedBll bll;
	private final PetTypeBll petTypeBll;
	private Grid<Breed> grid;
	private TextField txFilterByName;
	private ComboBox<PetType> cbPetType;
	private TextField txName;
	private ConfigurableFilterDataProvider<Breed, Void, SerializablePredicate<Breed>> filterDataProvider;

	public BreedLayout() throws IOException {
		super();
		bll = BreedBll.getInstance();
		petTypeBll = PetTypeBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		buildListView();
	}

	private void buildListView() {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Listado de razas");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newBreed());

		Button editButton = new Button("Editar", FontAwesome.PENCIL);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updateBreed());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deleteBreed());

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(newButton, editButton, deleteButton);

		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);

		cbPetType = new ComboBox<>("Especie");
		fillPetTypeCombo();
		cbPetType.setItemCaptionGenerator(PetType::getName);
		cbPetType.addSelectionListener(e -> fillGridData());

		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);
		filterLayout.setMargin(true);
		filterLayout.addComponents(cbPetType, txFilterByName);

		Panel filterPanel = new Panel("Filtrar por");
		filterPanel.setContent(filterLayout);
		filterPanel.addStyleName("well");
		addComponent(filterPanel);

		grid = new Grid<>();
		fillGridData();
		grid.addColumn(breed -> breed.getPetType().getName()).setCaption("Especie");
		grid.addColumn(Breed::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void buildEditionView(Breed breed) {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Razas");
		h1.addStyleName("h1");
		addComponent(h1);

		Button returnButton = new Button("Volver", FontAwesome.UNDO);
		returnButton.addClickListener(e -> buildListView());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(breed));

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

		cbPetType = new ComboBox<>("Especie");
		cbPetType.setSizeFull();
		fillPetTypeCombo();
		cbPetType.setItemCaptionGenerator(PetType::getName);
		if (breed != null) {
			cbPetType.setSelectedItem(breed.getPetType());
		}

		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(breed != null ? breed.getName() : "");
		dataLayout.addComponents(cbPetType, txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void fillPetTypeCombo() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<PetType> dataProvider = new ListDataProvider<>(petTypeBll.select(company));
		cbPetType.setDataProvider(dataProvider);
	}

	private void fillGridData() {
		txFilterByName.setValue("");
		Company company = getSession().getAttribute(User.class).getCompany();
		PetType petType = cbPetType.getValue();
		ListDataProvider<Breed> dataProvider = new ListDataProvider<>(bll.select(company, petType));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Breed> filterGrid() {
		SerializablePredicate<Breed> columnPredicate = null;
		columnPredicate = petType -> (petType.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

	private void newBreed() {
		buildEditionView(null);
	}

	private void updateBreed() {
		Breed breed = getSelected();
		if (breed != null && breed.getCompany() != null) {
			buildEditionView(breed);
		} else {
			String message;
			if (breed == null) {
				message = "No has seleccionado ninguna raza";
			} else {
				message = "Esta raza no puede ser editada";
			}
			new Notification(message, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private void deleteBreed() {
		Breed breed = getSelected();
		if (breed != null && breed.getCompany() != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(breed);
						}
					});
		} else {
			String message;
			if (breed == null) {
				message = "No has seleccionado ninguna raza";
			} else {
				message = "Esta raza no puede ser eliminada";
			}
			new Notification(message, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private Breed getSelected() {
		Breed breed = null;
		Set<Breed> breeds = grid.getSelectedItems();
		if (breeds != null && !breeds.isEmpty()) {
			breed = (Breed) breeds.toArray()[0];
		}
		return breed;
	}

	private void delete(Breed breed) {
		breed = Breed.builder(breed).enabled(false).build();
		bll.save(breed);
		fillGridData();
		new Notification("Raza borrada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}

	private void save(Breed breed) {
		Company company = getSession().getAttribute(User.class).getCompany();
		PetType petType = cbPetType.getValue();
		String name = txName.getValue();
		try {
			if (breed == null) {
				breed = Breed.builder().name(name).petType(petType).company(company).creation(new Date()).enabled(true)
						.build();
				breed.validate();
				bll.save(breed);
				new Notification("Raza guardada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				breed = Breed.builder(breed).name(name).petType(petType).build();
				breed.validate();
				bll.save(breed);
				new Notification("Raza actualizada", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification("Los datos de la raza no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}