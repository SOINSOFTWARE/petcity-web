package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;

import org.apache.log4j.Logger;

import com.soinsoftware.petcity.bll.UserBll;
import com.soinsoftware.petcity.manager.PetCityManagerFactory;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.User;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of an HTML page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@SuppressWarnings("deprecation")
public class PetCityUI extends UI {

	private static final long serialVersionUID = 7412593442523938389L;
	private static final Logger log = Logger.getLogger(PetCityUI.class);
	private static final String KEY_BREED = "breed";
	private static final String KEY_COMPANY_DATA = "companyData";
	private static final String KEY_DRENCHING = "drenching";
	private static final String KEY_EVENTS = "events";
	private static final String KEY_FOOD_BRAND = "foodBrand";
	private static final String KEY_HISTORIES = "histories";
	private static final String KEY_PET_TYPE = "petType";
	private static final String KEY_VACCINE = "vaccine";
	private static final String VALUE_BREED = "Raza";
	private static final String VALUE_COMPANY_DATA = "Datos de la veterinaria";
	private static final String VALUE_CONFIGURATIONS = "Configuración";
	private static final String VALUE_DRENCHING = "Productos antiparasitarios";
	private static final String VALUE_EVENTS = "Eventos";
	private static final String VALUE_FOOD_BRAND = "Alimentos de mascotas";
	private static final String VALUE_HISTORIES = "Historias";
	private static final String VALUE_PET_TYPE = "Especies";
	private static final String VALUE_VACCINE = "Productos de vacunación";
	private LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, FontAwesome> menuIconItems = new LinkedHashMap<String, FontAwesome>();
	private UserBll userBll;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		try {
			PetCityManagerFactory.getInstance();
			userBll = UserBll.getInstance();
			getPage().setTitle("Pet City Software");
			addStyleName(ValoTheme.UI_WITH_MENU);
			Responsive.makeResponsive(this);
			buildUI(getUser());
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void buildValoMenuLayout() {
		ValoMenuLayout root = new ValoMenuLayout();
		CssLayout menu = buildMenu(root);
		if (getPage().getWebBrowser().isIE() && getPage().getWebBrowser().getBrowserMajorVersion() == 9) {
			menu.setWidth("320px");
		}
		root.addMenu(menu);
		root.setWidth("100%");
		setContent(root);
	}

	private void buildMenuItems() {
		menuItems.put(KEY_EVENTS, VALUE_EVENTS);
		menuItems.put(KEY_HISTORIES, VALUE_HISTORIES);
		menuItems.put(KEY_COMPANY_DATA, VALUE_COMPANY_DATA);
		menuItems.put(KEY_PET_TYPE, VALUE_PET_TYPE);
		menuItems.put(KEY_BREED, VALUE_BREED);
		menuItems.put(KEY_FOOD_BRAND, VALUE_FOOD_BRAND);
		menuItems.put(KEY_VACCINE, VALUE_VACCINE);
		menuItems.put(KEY_DRENCHING, VALUE_DRENCHING);
	}

	private void buildMenuIconItems() {
		menuIconItems.put(KEY_EVENTS, FontAwesome.LIST);
		menuIconItems.put(KEY_HISTORIES, FontAwesome.AMBULANCE);
		menuIconItems.put(KEY_COMPANY_DATA, FontAwesome.BOOK);
		menuIconItems.put(KEY_PET_TYPE, FontAwesome.BOOKMARK);
		menuIconItems.put(KEY_BREED, FontAwesome.TAGS);
		menuIconItems.put(KEY_FOOD_BRAND, FontAwesome.NEWSPAPER_O);
		menuIconItems.put(KEY_VACCINE, FontAwesome.PRODUCT_HUNT);
		menuIconItems.put(KEY_DRENCHING, FontAwesome.PRODUCT_HUNT);
	}

	private CssLayout buildMenu(ValoMenuLayout root) {
		CssLayout menu = new CssLayout();
		CssLayout menuItemsLayout = buildMenuItemsLayout();
		menu.addComponent(buildTopLayout());
		menu.addComponent(buildShowMenuButton(menu));
		menu.addComponent(buildMenuBar());
		menu.addComponent(menuItemsLayout);

		buildNavigator(root, menu, menuItemsLayout);
		return menu;
	}

	private HorizontalLayout buildTopLayout() {
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		top.addStyleName(ValoTheme.MENU_TITLE);

		Label title = new Label("<h3><strong>Pet City</strong></h3>", ContentMode.HTML);
		title.setSizeUndefined();
		top.addComponent(title);
		top.setExpandRatio(title, 1);
		return top;
	}

	private Button buildShowMenuButton(CssLayout menu) {
		Button showMenu = new Button("Menu", e -> {
			if (menu.getStyleName().contains("valo-menu-visible")) {
				menu.removeStyleName("valo-menu-visible");
			} else {
				menu.addStyleName("valo-menu-visible");
			}
		});
		showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
		showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
		showMenu.addStyleName("valo-menu-toggle");
		showMenu.setIcon(FontAwesome.LIST);
		return showMenu;
	}

	private MenuBar buildMenuBar() {
		Company company = getUser().getCompany();
		MenuBar settings = new MenuBar();
		settings.addStyleName("user-menu");
		MenuItem settingsItem = settings.addItem(company.getName(), new ClassResource("new-logo-white.png"), null);
		settingsItem.addItem("Cerrar session", e -> buildUI(null));
		return settings;
	}

	private CssLayout buildMenuItemsLayout() {
		buildMenuItems();
		buildMenuIconItems();
		CssLayout menuItemsLayout = new CssLayout();
		menuItemsLayout.setPrimaryStyleName("valo-menuitems");

		Label label = null;
		for (final Entry<String, String> item : menuItems.entrySet()) {
			if (item.getKey().equals(KEY_COMPANY_DATA)) {
				label = new Label(VALUE_CONFIGURATIONS, ContentMode.HTML);
				label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
				label.addStyleName(ValoTheme.LABEL_H4);
				label.setSizeUndefined();
				menuItemsLayout.addComponent(label);
			}
			Button b = new Button(item.getValue(), e -> getNavigator().navigateTo(item.getKey()));
			b.setCaptionAsHtml(true);
			b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			if (menuIconItems.containsKey(item.getKey())) {
				b.setIcon(menuIconItems.get(item.getKey()));
			}
			menuItemsLayout.addComponent(b);
		}
		label.setValue(label.getValue() + " <span class=\"valo-menu-badge\"></span>");
		return menuItemsLayout;
	}

	private void buildNavigator(ValoMenuLayout root, CssLayout menu, CssLayout menuItemsLayout) {
		ComponentContainer viewContainer = root.getContentContainer();
		Navigator navigator = new Navigator(this, viewContainer);
		navigator.addView(KEY_EVENTS, NotificationLayout.class);
		navigator.addView(KEY_HISTORIES, ClinicHistoryLayout.class);
		navigator.addView(KEY_COMPANY_DATA, CompanyLayout.class);
		navigator.addView(KEY_PET_TYPE, PetTypeLayout.class);
		navigator.addView(KEY_BREED, BreedLayout.class);
		navigator.addView(KEY_FOOD_BRAND, FoodBrandLayout.class);
		navigator.addView(KEY_VACCINE, VaccineLayout.class);
		navigator.addView(KEY_DRENCHING, DrenchingLayout.class);
		navigator.setErrorView(DefaultView.class);

		navigator.addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = -7939732210155999013L;

			@Override
			public boolean beforeViewChange(final ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(final ViewChangeEvent event) {
				for (final Iterator<Component> it = menuItemsLayout.iterator(); it.hasNext();) {
					it.next().removeStyleName("selected");
				}
				for (final Entry<String, String> item : menuItems.entrySet()) {
					if (event.getViewName().equals(item.getKey())) {
						for (final Iterator<Component> it = menuItemsLayout.iterator(); it.hasNext();) {
							final Component c = it.next();
							if (c.getCaption() != null && c.getCaption().startsWith(item.getValue())) {
								c.addStyleName("selected");
								break;
							}
						}
						break;
					}
				}
				menu.removeStyleName("valo-menu-visible");
			}
		});
	}

	private void buildUI(User user) {
		getSession().setAttribute(User.class, user);
		if (user == null) {
			buildLoginForm();
		} else {
			buildValoMenuLayout();
		}
	}

	private User getUser() {
		return getSession().getAttribute(User.class);
	}

	private void buildLoginForm() {
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("400px");

		TextField txtUsername = new TextField("Usuario");
		txtUsername.setSizeFull();
		layout.addComponent(txtUsername);

		PasswordField txtPassword = new PasswordField("Contraseña");
		txtPassword.setSizeFull();
		layout.addComponent(txtPassword);

		Button loginButton = new Button("Ingresar", e -> authenticate(txtUsername.getValue(), txtPassword.getValue()));
		loginButton.setSizeFull();
		loginButton.addStyleName("friendly");
		layout.addComponent(loginButton);

		contentLayout.addComponent(layout);
		contentLayout.setComponentAlignment(layout, Alignment.TOP_CENTER);

		Panel loginPanel = new Panel("<center><h1>Inicio de sesión - Pet City</h1></center>");
		loginPanel.addStyleName("well");
		loginPanel.setContent(contentLayout);
		setContent(loginPanel);
	}

	private void authenticate(String username, String password) {
		User user = userBll.select(username, password);
		if (user == null) {
			showNotification("Login failed!");
		} else {
			buildUI(user);
		}
	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = PetCityUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
		private static final long serialVersionUID = -2743165194104880059L;
	}
}
