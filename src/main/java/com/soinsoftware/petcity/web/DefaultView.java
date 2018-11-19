package com.soinsoftware.petcity.web;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class DefaultView extends Composite implements View {
	
	private static final long serialVersionUID = -4823544347906605726L;

	public DefaultView() {
		HorizontalLayout layout = new HorizontalLayout();
		Button button = new Button("Save");
		Label label = new Label("This is the default view");
		layout.addComponents(label, button);
		setCompositionRoot(layout);
    }
}