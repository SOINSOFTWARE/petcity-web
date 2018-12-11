package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.soinsoftware.petcity.bll.VaccineBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.model.Vaccine;
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

public class VaccineLayout extends AbstractEditableLayout<Vaccine> {

	private static final long serialVersionUID = -5862329317115273275L;

	private final VaccineBll bll;
	private Grid<Vaccine> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<Vaccine, Void, SerializablePredicate<Vaccine>> filterDataProvider;

	public VaccineLayout() throws IOException {
		super("Productos de vacunación");
		bll = VaccineBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(Vaccine vaccine) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(vaccine);
		Panel dataPanel = buildEditionPanel(vaccine);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Vaccine::getName).setCaption("Nombre");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionPanel(Vaccine vaccine) {
		txName = new TextField("Nombre");
		txName.setSizeFull();
		txName.setValue(vaccine != null ? vaccine.getName() : "");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponent(txName);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<Vaccine> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(Vaccine vaccine) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		if (vaccine == null) {
			vaccine = Vaccine.builder().name(name).company(company).creation(new Date()).enabled(true).build();
		} else {
			vaccine = Vaccine.builder(vaccine).name(name).build();
		}
		save(bll, vaccine, "Vacuna guardada");
	}

	@Override
	protected Vaccine getSelected() {
		Vaccine vaccine = null;
		Set<Vaccine> vaccines = grid.getSelectedItems();
		if (vaccines != null && !vaccines.isEmpty()) {
			vaccine = (Vaccine) vaccines.toArray()[0];
		}
		return vaccine;
	}

	@Override
	protected void delete(Vaccine vaccine) {
		vaccine = Vaccine.builder(vaccine).enabled(false).build();
		save(bll, vaccine, "Vacuna borrada");
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

	private SerializablePredicate<Vaccine> filterGrid() {
		SerializablePredicate<Vaccine> columnPredicate = null;
		columnPredicate = vaccine -> (vaccine.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}
}