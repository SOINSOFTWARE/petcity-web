package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.soinsoftware.petcity.bll.PetTypeBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.PetType;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class PetTypeLayout extends AbstractEditableLayout<PetType> {

	private static final long serialVersionUID = -5862329317115273275L;
	private final PetTypeBll bll;
	private Grid<PetType> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<PetType, Void, SerializablePredicate<PetType>> filterDataProvider;

	public PetTypeLayout() throws IOException {
		super();
		bll = PetTypeBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(PetType petType) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(petType);
		Panel dataPanel = buildEditionPanel(petType);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(PetType::getName).setCaption("Nombre");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionPanel(PetType petType) {
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(petType != null ? petType.getName() : "");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponent(txName);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<PetType> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(PetType petType) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		if (petType == null) {
			petType = PetType.builder().name(name).company(company).creation(new Date()).enabled(true).build();
		} else {
			petType = PetType.builder(petType).name(name).build();
		}
		save(bll, petType, "Especie guardada");
	}

	@Override
	protected PetType getSelected() {
		PetType petType = null;
		Set<PetType> petTypes = grid.getSelectedItems();
		if (petTypes != null && !petTypes.isEmpty()) {
			petType = (PetType) petTypes.toArray()[0];
		}
		return petType;
	}

	@Override
	protected void delete(PetType petType) {
		petType = PetType.builder(petType).enabled(false).build();
		save(bll, petType, "Especie borrada");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout filterLayout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		filterLayout.addComponent(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", filterLayout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<PetType> filterGrid() {
		SerializablePredicate<PetType> columnPredicate = null;
		columnPredicate = petType -> (petType.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}
}