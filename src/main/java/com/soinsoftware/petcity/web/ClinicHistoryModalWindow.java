package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Set;

import org.vaadin.ui.NumberField;

import com.soinsoftware.petcity.bll.ClinicHistoryBll;
import com.soinsoftware.petcity.model.ClinicHistory;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ClinicHistoryModalWindow extends AbstractModalWindow<ClinicHistory> {

	private static final long serialVersionUID = -5165673175467412293L;
	
	private NumberField txFilterByHistory;
	private TextField txFilterByPetName;
	private NumberField txFilterByDocument;
	private Grid<ClinicHistory> grid;
	private ConfigurableFilterDataProvider<ClinicHistory, Void, SerializablePredicate<ClinicHistory>> filterDataProvider;

	public ClinicHistoryModalWindow(String caption, Company company) throws IOException {
		super(caption, ClinicHistoryBll.getInstance(), company);
		setHeight("90%");
		setWidth("90%");
		setPositionX(20);
		setPositionY(20);
	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(ClinicHistory entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(ClinicHistory::getRecordCustomId).setCaption("# Historia");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getName()).setCaption("Mascota");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getPetType().getName()).setCaption("Especie");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getBreed().getName()).setCaption("Raza");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getName() + " "
				+ clinicHistory.getPet().getOwner().getLastName()).setCaption("Propietario");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getDocument()).setCaption("# Documento");
		grid.addColumn(clinicHistory -> clinicHistory.getPet().getOwner().getPhone2()).setCaption("# Celular");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Component buildEditionComponent(ClinicHistory entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<ClinicHistory> dataProvider = new ListDataProvider<>(((ClinicHistoryBll) getBll()).select(getCompany()));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
	}

	@Override
	protected void saveButtonAction(ClinicHistory entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ClinicHistory getSelected() {
		ClinicHistory clinicHistory = null;
		Set<ClinicHistory> clinicHistories = grid.getSelectedItems();
		if (clinicHistories != null && !clinicHistories.isEmpty()) {
			clinicHistory = (ClinicHistory) clinicHistories.toArray()[0];
		}
		return clinicHistory;
	}
	
	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByHistory = new NumberField("# Historia");
		txFilterByHistory.setDecimalAllowed(false);
		txFilterByHistory.addValueChangeListener(e -> refreshGrid());
		txFilterByPetName = new TextField("Mascota");
		txFilterByPetName.addValueChangeListener(e -> refreshGrid());
		txFilterByDocument = new NumberField("# Documento");
		txFilterByDocument.setDecimalAllowed(false);
		txFilterByDocument.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByHistory, txFilterByPetName, txFilterByDocument);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}
	
	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}
	
	private SerializablePredicate<ClinicHistory> filterGrid() {
		SerializablePredicate<ClinicHistory> columnPredicate = null;
		columnPredicate = clinicHistory -> {
			if (clinicHistory.getRecordCustomId() != null) {
				return filterByRecordCustomId(clinicHistory) && filterByPetName(clinicHistory)
						&& filterByOwnerDocument(clinicHistory);
			} else {
				return txFilterByHistory.getValue().trim().isEmpty() && filterByPetName(clinicHistory)
						&& filterByOwnerDocument(clinicHistory);
			}
		};
		return columnPredicate;
	}
	
	private boolean filterByRecordCustomId(ClinicHistory clinicHistory) {
		return clinicHistory.getRecordCustomId().toString().contains(txFilterByHistory.getValue())
				|| txFilterByHistory.getValue().trim().isEmpty();
	}

	private boolean filterByPetName(ClinicHistory clinicHistory) {
		return clinicHistory.getPet().getName().toLowerCase().contains(txFilterByPetName.getValue().toLowerCase())
				|| txFilterByPetName.getValue().trim().isEmpty();
	}

	private boolean filterByOwnerDocument(ClinicHistory clinicHistory) {
		return clinicHistory.getPet().getOwner().getDocument().contains(txFilterByDocument.getValue())
				|| txFilterByDocument.getValue().trim().isEmpty();
	}
}