package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.petcity.bll.FoodBrandBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.FoodBrand;
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
public class FoodBrandLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5862329317115273275L;
	private static final Logger log = Logger.getLogger(FoodBrandLayout.class);
	private final FoodBrandBll bll;
	private Grid<FoodBrand> grid;
	private TextField txFilterByName;
	private TextField txName;
	private ConfigurableFilterDataProvider<FoodBrand, Void, SerializablePredicate<FoodBrand>> filterDataProvider;

	public FoodBrandLayout() throws IOException {
		super();
		bll = FoodBrandBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		buildListView();
	}

	private void buildListView() {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Listado de marca de alimentos de mascotas");
		h1.addStyleName("h1");
		addComponent(h1);

		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newFoodBrand());

		Button editButton = new Button("Editar", FontAwesome.PENCIL);
		editButton.addStyleName("friendly");
		editButton.addClickListener(e -> updateFoodBrand());

		Button deleteButton = new Button("Borrar", FontAwesome.ERASER);
		deleteButton.addStyleName("danger");
		deleteButton.addClickListener(e -> deleteFoodBrand());

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
		grid.addColumn(FoodBrand::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();

		Panel dataPanel = new Panel(grid);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void buildEditionView(FoodBrand foodBrand) {
		removeAllComponents();
		setMargin(true);

		Label h1 = new Label("Marca de alimento de mascotas");
		h1.addStyleName("h1");
		addComponent(h1);

		Button returnButton = new Button("Volver", FontAwesome.UNDO);
		returnButton.addClickListener(e -> buildListView());

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save(foodBrand));

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
		txName.setValue(foodBrand != null ? foodBrand.getName() : "");
		dataLayout.addComponent(txName);

		Panel dataPanel = new Panel(dataLayout);
		dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}

	private void fillGridData() {
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<FoodBrand> dataProvider = new ListDataProvider<>(bll.select(company));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
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

	private void newFoodBrand() {
		buildEditionView(null);
	}

	private void updateFoodBrand() {
		FoodBrand foodBrand = getSelected();
		if (foodBrand != null) {
			buildEditionView(foodBrand);
		} else {
			new Notification("No has seleccionado ninguna marca", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private void deleteFoodBrand() {
		FoodBrand foodBrand = getSelected();
		if (foodBrand != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
					"Aceptar", "Cancelar", e -> {
						if (e.isConfirmed()) {
							delete(foodBrand);
						}
					});
		} else {
			new Notification("No has seleccionado ninguna marca", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		}
	}

	private FoodBrand getSelected() {
		FoodBrand foodBrand = null;
		Set<FoodBrand> foodBrands = grid.getSelectedItems();
		if (foodBrands != null && !foodBrands.isEmpty()) {
			foodBrand = (FoodBrand) foodBrands.toArray()[0];
		}
		return foodBrand;
	}

	private void delete(FoodBrand foodBrand) {
		foodBrand = FoodBrand.builder(foodBrand).enabled(false).build();
		bll.update(foodBrand);
		fillGridData();
		new Notification("Alimento borrado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}

	private void save(FoodBrand foodBrand) {
		Company company = getSession().getAttribute(User.class).getCompany();
		String name = txName.getValue();
		try {
			if (foodBrand == null) {
				foodBrand = FoodBrand.builder().name(name).company(company).creation(new Date()).enabled(true).build();
				foodBrand.validate();
				bll.save(foodBrand);
				new Notification("Alimento guardado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				foodBrand = FoodBrand.builder(foodBrand).name(name).build();
				foodBrand.validate();
				bll.update(foodBrand);
				new Notification("Alimento actualizado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
			buildListView();
		} catch (ModelValidationException ex) {
			log.error(ex);
			new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		} catch (Exception ex) {
			log.error(ex);
			new Notification("Los datos de la marca no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
}