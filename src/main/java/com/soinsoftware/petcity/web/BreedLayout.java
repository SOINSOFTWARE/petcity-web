// Soin Software, 2018
package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.soinsoftware.petcity.bll.BreedBll;
import com.soinsoftware.petcity.bll.PetTypeBll;
import com.soinsoftware.petcity.model.Breed;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.PetType;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Carlos Rodriguez
 * @since 22/11/2018
 */
public class BreedLayout extends AbstractEditableLayout<Breed> {

	private static final long serialVersionUID = -6868887503188086381L;
	private final BreedBll bll;
	private final PetTypeBll petTypeBll;
	private Grid<Breed> grid;
	private TextField txFilterByName;
	private ComboBox<PetType> cbFilterByPetType;
	private ComboBox<PetType> cbPetType;
	private TextField txName;
	private ConfigurableFilterDataProvider<Breed, Void, SerializablePredicate<Breed>> filterDataProvider;

	public BreedLayout() throws IOException {
		super("Razas");
		bll = BreedBll.getInstance();
		petTypeBll = PetTypeBll.getInstance();
	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists(true);
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Breed breed) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(breed);
		Panel dataPanel = buildEditionPanel(breed);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(breed -> breed.getPetType().getName()).setCaption("Especie");
		grid.addColumn(Breed::getName).setCaption("Nombre");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionPanel(Breed breed) {
		cbPetType = new ComboBox<>("Especie");
		cbPetType.setSizeFull();
		fillPetTypeCombo(cbPetType);
		cbPetType.setItemCaptionGenerator(PetType::getName);
		if (breed != null) {
			cbPetType.setSelectedItem(breed.getPetType());
		}
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(breed != null ? breed.getName() : "");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponents(cbPetType, txName);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		txFilterByName.setValue("");
		Company company = getSession().getAttribute(User.class).getCompany();
		PetType petType = cbFilterByPetType.getValue();
		ListDataProvider<Breed> dataProvider = new ListDataProvider<>(bll.select(company, petType));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(Breed breed) {
		Company company = getSession().getAttribute(User.class).getCompany();
		PetType petType = cbPetType.getValue();
		String name = txName.getValue();
		if (breed == null) {
			breed = Breed.builder().name(name).petType(petType).company(company).creation(new Date()).enabled(true)
					.build();
		} else {
			breed = Breed.builder(breed).name(name).petType(petType).build();
		}
		save(bll, breed, "Raza guardada");
	}
	
	@Override
	protected Breed getSelected() {
		Breed breed = null;
		Set<Breed> breeds = grid.getSelectedItems();
		if (breeds != null && !breeds.isEmpty()) {
			breed = (Breed) breeds.toArray()[0];
		}
		return breed;
	}
	
	@Override
	protected void delete(Breed breed) {
		breed = Breed.builder(breed).enabled(false).build();
		save(bll, breed, "Raza borrada");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		cbFilterByPetType = new ComboBox<>("Especie");
		fillPetTypeCombo(cbFilterByPetType);
		cbFilterByPetType.setItemCaptionGenerator(PetType::getName);
		cbFilterByPetType.addSelectionListener(e -> fillGridData());
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(cbFilterByPetType, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void fillPetTypeCombo(ComboBox<PetType> cbPetType) {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<PetType> dataProvider = new ListDataProvider<>(petTypeBll.select(company));
		cbPetType.setDataProvider(dataProvider);
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
}