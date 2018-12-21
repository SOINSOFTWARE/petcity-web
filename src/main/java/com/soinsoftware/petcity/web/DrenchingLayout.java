package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.soinsoftware.petcity.bll.DrenchingBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.Drenching;
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

public class DrenchingLayout extends AbstractEditableLayout<Drenching> {

	private static final long serialVersionUID = -5862329317115273275L;

	private final DrenchingBll bll;
	private Grid<Drenching> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<Drenching, Void, SerializablePredicate<Drenching>> filterDataProvider;

	public DrenchingLayout() throws IOException {
		super("Productos antiparasitarios");
		bll = DrenchingBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(Drenching drenching) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(drenching);
		Panel dataPanel = buildEditionComponent(drenching);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Drenching::getName).setCaption("Nombre");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionComponent(Drenching drenching) {
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(drenching != null ? drenching.getName() : "");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponent(txName);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<Drenching> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(Drenching drenching) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		if (drenching == null) {
			drenching = Drenching.builder().name(name).company(company).creation(new Date()).enabled(true).build();
		} else {
			drenching = Drenching.builder(drenching).name(name).build();
		}
		save(bll, drenching, "Antiparasitario guardado");
	}

	@Override
	protected Drenching getSelected() {
		Drenching drenching = null;
		Set<Drenching> drenchings = grid.getSelectedItems();
		if (drenchings != null && !drenchings.isEmpty()) {
			drenching = (Drenching) drenchings.toArray()[0];
		}
		return drenching;
	}

	@Override
	protected void delete(Drenching drenching) {
		drenching = Drenching.builder(drenching).enabled(false).build();
		save(bll, drenching, "Antiparasitario borrado");
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

	private SerializablePredicate<Drenching> filterGrid() {
		SerializablePredicate<Drenching> columnPredicate = null;
		columnPredicate = drenching -> (drenching.getName().toLowerCase()
				.contains(txFilterByName.getValue().toLowerCase()) || txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}
}