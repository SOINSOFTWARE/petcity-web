// Soin Software 2018
package com.soinsoftware.petcity.web;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.soinsoftware.petcity.bll.AbstractBll;
import com.soinsoftware.petcity.exception.ModelValidationException;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.util.ViewHelper;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Carlos Rodriguez
 * @since 19/12/2018
 *
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public abstract class AbstractModalWindow<E> extends Window {

	private static final long serialVersionUID = -4960256751591876767L;
	protected static final Logger log = Logger.getLogger(AbstractModalWindow.class);
	
	private final AbstractBll bll;
	private final Company company;
	private TabSheet tabSheet;
	private final VerticalLayout windowLayout;
	
	public AbstractModalWindow(String caption, AbstractBll bll, Company company) {
		super(caption);
		this.bll = bll;
		this.company = company;
		windowLayout = ViewHelper.buildVerticalLayout(true, true);
		setModal(true);
		addPageTitle(caption);
		setContent(windowLayout);
		addListTab();
	}
	
	public AbstractBll getBll() {
		return bll;
	}

	public Company getCompany() {
		return company;
	}
	
	protected void addPageTitle(String title) {
		Label h1 = new Label(title);
		h1.addStyleName("h1");
		windowLayout.addComponent(h1);
	}
	
	private void addListTab() {
		AbstractOrderedLayout layout = buildListView();
		tabSheet = new TabSheet();
		tabSheet.addStyleName("framed");
		Tab tab = tabSheet.addTab(layout, "Listado");
		tab.setIcon(FontAwesome.LIST);
		windowLayout.addComponent(tabSheet);
	}
	
	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction();
		Button btSelect = buildButtonForSelectAction();
		layout.addComponents(btNew, btSelect);
		return ViewHelper.buildPanel(null, layout);
	}
	
	protected Panel buildButtonPanelForEdition(E entity) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btCancel = buildButtonForCancelAction();
		Button btSave = buildButtonForSaveAction(entity);
		layout.addComponents(btCancel, btSave);
		return ViewHelper.buildPanel(null, layout);
	}
	
	protected Button buildButtonForNewAction() {
		Button button = ViewHelper.buildButton("Nuevo", FontAwesome.PLUS, "primary");
		button.addClickListener(e -> newButtonAction());
		return button;
	}

	protected Button buildButtonForSelectAction() {
		Button button = ViewHelper.buildButton("Seleccionar", FontAwesome.CHECK, "friendly");
		button.addClickListener(e -> selectButtonAction());
		return button;
	}
	
	protected Button buildButtonForCancelAction() {
		Button button = ViewHelper.buildButton("Cancelar", FontAwesome.CLOSE, "danger");
		button.addClickListener(e -> cancelButtonAction());
		return button;
	}

	protected Button buildButtonForSaveAction(E entity) {
		Button button = ViewHelper.buildButton("Guardar", FontAwesome.SAVE, "primary");
		button.addClickListener(e -> saveButtonAction(entity));
		return button;
	}
	
	protected void newButtonAction() {
		showEditionTab(null, "Nuevo", FontAwesome.PLUS);
	}

	protected E selectButtonAction() {
		E entity = getSelected();
		if (entity == null) {
			ViewHelper.showNotification("No has seleccionado ningún registro", Notification.Type.TRAY_NOTIFICATION);
		}
		return entity;
	}
	
	protected void showEditionTab(E entity, String caption, Resource icon) {
		AbstractOrderedLayout layout = buildEditionView(entity);
		addEditionTab(layout, caption, icon);
	}
	
	protected void addEditionTab(AbstractOrderedLayout layout, String caption, Resource icon) {
		cancelButtonAction();
		Tab tab = tabSheet.addTab(layout, caption);
		tab.setIcon(icon);
		tabSheet.setSelectedTab(1);
	}
	
	protected void cancelButtonAction() {
		Tab tab = tabSheet.getTab(1);
		if (tab != null) {
			tabSheet.removeTab(tab);
		}
	}
	
	protected void save(AbstractBll<E, ?> bll, E entity, String caption) {
		try {
			bll.save(entity);
			afterSave(caption);
		} catch (ModelValidationException ex) {
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(ex);
			bll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	private void afterSave(String caption) {
		fillGridData();
		cancelButtonAction();
		ViewHelper.showNotification(caption, Notification.Type.TRAY_NOTIFICATION);
	}
	
	protected abstract AbstractOrderedLayout buildListView();
	
	protected abstract AbstractOrderedLayout buildEditionView(E entity);

	protected abstract Panel buildGridPanel();

	protected abstract Component buildEditionComponent(E entity);

	protected abstract void fillGridData();
	
	protected abstract void saveButtonAction(E entity);
	
	protected abstract E getSelected();
}