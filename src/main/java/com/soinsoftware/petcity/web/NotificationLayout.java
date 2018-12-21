// Soin Software, 2018
package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import com.soinsoftware.petcity.bll.NotificationBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.Notification;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Carlos Rodriguez
 * @since 10/12/2018
 */
@SuppressWarnings("deprecation")
public class NotificationLayout extends AbstractEditableLayout<Notification> {

	private static final long serialVersionUID = -6746080509992946223L;
	private final NotificationBll bll;
	private DateField dfFilterDate;
	private Grid<Notification> grid;
	private DateField dfDate;
	private TextField tfPetName;
	private TextField tfOwnerFullName;
	private TextField tfNotificationTitle;
	private TextArea taNotificationMessage;

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
	protected AbstractOrderedLayout buildEditionView(Notification notification) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(notification);
		Component dataComponent = buildEditionComponent(notification);
		layout.addComponents(buttonPanel, dataComponent);
		return layout;
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
	protected Component buildEditionComponent(Notification entity) {
		dfDate = ViewHelper.buildDateField("Fecha:", LocalDate.now());
		dfDate.setWidth("160px");
		tfPetName = ViewHelper.buildTextField("Mascota:", false, true);
		Button btFind = ViewHelper.buildButton(null, FontAwesome.SEARCH, "friendly");
		btFind.addClickListener(e -> findPet());
		HorizontalLayout petLayout = ViewHelper.buildLayoutWithTextFieldAndButton(tfPetName, btFind);
		tfOwnerFullName = ViewHelper.buildTextField("Propietario:", false, true);
		tfNotificationTitle = ViewHelper.buildTextField("Título:", true, true);
		taNotificationMessage = ViewHelper.buildTextArea("Mensaje:", true, true, 10);
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("50%");
		layout.addComponents(dfDate, petLayout, tfOwnerFullName, tfNotificationTitle, taNotificationMessage);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		LocalDate localDate = dfFilterDate.getValue();
		Company company = getSession().getAttribute(User.class).getCompany();
		ListDataProvider<Notification> dataProvider = new ListDataProvider<>(bll.select(company, localDate));
		grid.setDataProvider(dataProvider);

	}

	@Override
	protected void saveButtonAction(Notification entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Notification getSelected() {
		Notification notification = null;
		Set<Notification> notifications = grid.getSelectedItems();
		if (notifications != null && !notifications.isEmpty()) {
			notification = (Notification) notifications.toArray()[0];
		}
		return notification;
	}

	@Override
	protected void delete(Notification entity) {
		// TODO Auto-generated method stub

	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		dfFilterDate = ViewHelper.buildDateField(null, LocalDate.now());
		dfFilterDate.addValueChangeListener(e -> fillGridData());
		layout.addComponents(dfFilterDate);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void findPet() {
		try {
			ClinicHistoryModalWindow mywindow = new ClinicHistoryModalWindow("Historias clínicas", getSession().getAttribute(User.class).getCompany());
			UI.getCurrent().addWindow(mywindow);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}