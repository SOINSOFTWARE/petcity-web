package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.soinsoftware.petcity.bll.FoodBrandBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.FoodBrand;
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

public class FoodBrandLayout extends AbstractEditableLayout<FoodBrand> {
	
	private static final long serialVersionUID = 1013768458294628159L;
	
	private final FoodBrandBll bll;
	private Grid<FoodBrand> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<FoodBrand, Void, SerializablePredicate<FoodBrand>> filterDataProvider;

	public FoodBrandLayout() throws IOException {
		super("Marca de alimentos de mascotas");
		bll = FoodBrandBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(FoodBrand foodBrand) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(foodBrand);
		Panel dataPanel = buildEditionComponent(foodBrand);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(FoodBrand::getName).setCaption("Nombre");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionComponent(FoodBrand foodBrand) {
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(foodBrand != null ? foodBrand.getName() : "");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponent(txName);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<FoodBrand> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(FoodBrand foodBrand) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		if (foodBrand == null) {
			foodBrand = FoodBrand.builder().name(name).company(company).creation(new Date()).enabled(true).build();
		} else {
			foodBrand = FoodBrand.builder(foodBrand).name(name).build();
		}
		save(bll, foodBrand, "Marca de alimento de mascotas guardada");
	}

	@Override
	protected FoodBrand getSelected() {
		FoodBrand foodBrand = null;
		Set<FoodBrand> foodBrands = grid.getSelectedItems();
		if (foodBrands != null && !foodBrands.isEmpty()) {
			foodBrand = (FoodBrand) foodBrands.toArray()[0];
		}
		return foodBrand;
	}

	@Override
	protected void delete(FoodBrand foodBrand) {
		foodBrand = FoodBrand.builder(foodBrand).enabled(false).build();
		save(bll, foodBrand, "Marca de alimento de mascotas borrada");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponent(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<FoodBrand> filterGrid() {
		SerializablePredicate<FoodBrand> columnPredicate = null;
		columnPredicate = foodBrand -> (foodBrand.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}
}