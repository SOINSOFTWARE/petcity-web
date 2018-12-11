// Soin Software, 2018
package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import com.soinsoftware.petcity.bll.NotificationBll;
import com.soinsoftware.petcity.model.Notification;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Carlos Rodriguez
 * @since 10/12/2018
 */
public class NotificationLayout extends AbstractEditableLayout<Notification> {

	private static final long serialVersionUID = -6746080509992946223L;
	private final NotificationBll bll;
	private DateField dfFilterDate; 
	private Grid<Notification> grid;

	public NotificationLayout() throws IOException {
		super("Eventos");
		bll = NotificationBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(Notification entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Notification::getNotificationTitle).setCaption("Título");
		grid.addColumn(Notification::getMessage).setCaption("Mensaje");
		grid.addColumn(notification -> notification.getPet().getName()).setCaption("Mascota");
		grid.addColumn(notification -> notification.getPet().getOwner().getName() + " "
				+ notification.getPet().getOwner().getLastName()).setCaption("Propietario");
		grid.addColumn(notification -> notification.getPet().getOwner().getPhone2()).setCaption("Celular");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionPanel(Notification entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillGridData() {
		LocalDate localDate = dfFilterDate.getValue();
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveButtonAction(Notification entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Notification getSelected() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void delete(Notification entity) {
		// TODO Auto-generated method stub

	}
	
	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		dfFilterDate = new DateField("Custom format"); 
		dfFilterDate.setValue(LocalDate.now()); 
		dfFilterDate.setDateFormat("dd/MM/yyyy");
		layout.addComponents(dfFilterDate);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}
}