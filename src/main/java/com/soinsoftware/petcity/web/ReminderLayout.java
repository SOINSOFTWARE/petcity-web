package com.soinsoftware.petcity.web;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.soinsoftware.petcity.model.Company;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class ReminderLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -5995926457571993892L;
	private Grid<Company> grid;

	public ReminderLayout() {
		setMargin(true);

		Label h1 = new Label("Eventos");
		h1.addStyleName("h1");
		addComponent(h1);
		
		Button newButton = new Button("Nuevo", FontAwesome.PLUS);
		newButton.addStyleName("primary");
		newButton.addClickListener(e -> newReminder());
		
		Button sendMailButton = new Button("Enviar emails", FontAwesome.ENVELOPE);
		sendMailButton.addStyleName("friendly");
		sendMailButton.addClickListener(e -> sendEmails());
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(newButton, sendMailButton);
		
		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);
		
		DateField dateField = new DateField();
		dateField.setValue(LocalDate.now());
		dateField.setDateFormat("dd/MM/yyyy");
		dateField.setTextFieldEnabled(false);
		dateField.addValueChangeListener(e -> updateGrid(e.getValue()));

        grid = new Grid<>();
        grid.setItems(buildCompanies());
        grid.addColumn(Company::getName).setCaption("Name");
        grid.setSelectionMode(SelectionMode.MULTI);

        VerticalLayout dataLayout = new VerticalLayout();
        dataLayout.setSpacing(true);
        dataLayout.setMargin(true);
        dataLayout.addComponents(dateField, grid);
        
        Panel dataPanel = new Panel(dataLayout);
        dataPanel.addStyleName("well");
		addComponent(dataPanel);
	}
	
	private List<Company> buildCompanies() {
		return Arrays.asList(
		    Company.builder().id(new BigInteger("1")).name("Soin Software").document("900900900-1").build(),
		    Company.builder().id(new BigInteger("2")).name("Other Company").document("900900900-2").build());
	}
	
	private void newReminder() {
		
	}
	
	private void sendEmails() {
		Set<Company> selected = grid.getSelectedItems();
		Notification.show(selected.size() + " items selected");
	}
	
	private void updateGrid(LocalDate localDate) {
		Notification.show(localDate.toString());
	}
}