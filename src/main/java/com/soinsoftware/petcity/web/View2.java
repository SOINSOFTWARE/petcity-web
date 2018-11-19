package com.soinsoftware.petcity.web;

import com.vaadin.navigator.View;
import com.vaadin.ui.Composite;
import com.vaadin.ui.Label;

public class View2 extends Composite implements View {

    private static final long serialVersionUID = -5629026822575812871L;

	public View2() {
        setCompositionRoot(new Label("This is view 2"));
    }
}