// Soin Software 2018
package com.soinsoftware.petcity.util;

import java.time.LocalDate;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Carlos Rodriguez
 * @since 06/12/2018
 *
 */
public class ViewHelper {

	public static Button buildButton(String caption, Resource icon, String style) {
		Button button = new Button(caption, icon);
		button.addStyleName(style);
		return button;
	}

	@SuppressWarnings("rawtypes")
	public static Grid buildGrid(SelectionMode selectionMode) {
		Grid grid = new Grid<>();
		grid.setSelectionMode(selectionMode);
		grid.setSizeFull();
		return grid;
	}

	public static HorizontalLayout buildHorizontalLayout(boolean spacing, boolean margin) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(spacing);
		layout.setMargin(margin);
		return layout;
	}

	public static VerticalLayout buildVerticalLayout(boolean spacing, boolean margin) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(spacing);
		layout.setMargin(margin);
		return layout;
	}

	public static Panel buildPanel(String caption, Component content) {
		Panel panel = (caption != null) ? new Panel(caption) : new Panel();
		panel.setContent(content);
		panel.addStyleName("well");
		return panel;
	}

	public static void showNotification(String caption, Notification.Type type) {
		new Notification(caption, type).show(Page.getCurrent());
	}

	public static DateField buildDateField(String caption, LocalDate localDate) {
		DateField dfField = (caption != null) ? new DateField(caption) : new DateField();
		dfField.setDateFormat("dd/MM/yyyy");
		dfField.setTextFieldEnabled(false);
		if (localDate != null) {
			dfField.setValue(localDate);
		}
		return dfField;
	}

	public static FormLayout buildFormLayout() {
		final FormLayout form = new FormLayout();
		form.addStyleName("light");
		form.setWidth("50%");
		return form;
	}

	public static TextField buildTextField(String caption, boolean enabled, boolean isFullSize) {
		TextField textField = new TextField(caption);
		textField.setEnabled(enabled);
		if (isFullSize) {
			textField.setSizeFull();
		}
		return textField;
	}

	public static TextArea buildTextArea(String caption, boolean enabled, boolean isFullSize, int rows) {
		TextArea textArea = new TextArea(caption);
		textArea.setEnabled(enabled);
		textArea.setRows(rows);
		if (isFullSize) {
			textArea.setSizeFull();
		}
		return textArea;
	}

	public static HorizontalLayout buildLayoutWithTextFieldAndButton(TextField textField, Button button) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(false, false);
		layout.setSizeFull();
		layout.addComponents(textField, button);
		layout.setExpandRatio(textField, 1.0f);
		layout.setComponentAlignment(button, Alignment.BOTTOM_RIGHT);
		return layout;
	}
}