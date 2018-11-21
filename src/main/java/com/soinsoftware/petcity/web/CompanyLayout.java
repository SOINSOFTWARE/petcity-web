package com.soinsoftware.petcity.web;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.petcity.bll.CompanyBll;
import com.soinsoftware.petcity.model.Company;
import com.soinsoftware.petcity.model.User;
import com.soinsoftware.petcity.util.ImageUploader;
import com.soinsoftware.petcity.util.StreamResourceUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class CompanyLayout extends VerticalLayout implements View {

    private static final long serialVersionUID = -5629026822575812871L;
    private static final Logger log = Logger.getLogger(CompanyLayout.class);
    private TextField txNit;
    private TextField txName;
    private NumberField nfInitialHistory;
    private NumberField nfCurrentHistory;
    private Image imgCompany;
    
    @Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		
		setMargin(true);

		Label h1 = new Label("Datos de la veterinaria");
		h1.addStyleName("h1");
		addComponent(h1);
		
		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");
		saveButton.addClickListener(e -> save());
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(saveButton);
		
		Panel buttonPanel = new Panel(buttonLayout);
		buttonPanel.addStyleName("well");
		addComponent(buttonPanel);
		
		Panel dataPanel = new Panel(buildDataLayout());
        dataPanel.addStyleName("well");
		addComponent(dataPanel);
		loadData(getSession().getAttribute(User.class));
	}
	
	private VerticalLayout buildDataLayout() {
		imgCompany = new Image();
		imgCompany.setWidth("30%");
		ImageUploader receiver = new ImageUploader(imgCompany, false);
		Upload upload = new Upload("Cambiar el logo", receiver);
		upload.addSucceededListener(receiver);
		
		HorizontalLayout firstRowLayout = new HorizontalLayout();
		firstRowLayout.setWidth("60%");
        txNit = new TextField("NIT");
        txNit.setEnabled(false);
        txNit.setSizeFull();
        firstRowLayout.addComponent(txNit);
        txName = new TextField("Nombre");
        txName.setEnabled(false);
        txName.setSizeFull();
        firstRowLayout.addComponent(txName);
        
        HorizontalLayout secondRowLayout = new HorizontalLayout();
        secondRowLayout.setWidth("60%");
        nfInitialHistory = new NumberField("Número de historia inicial");
        nfInitialHistory.setDecimalAllowed(false);
        nfInitialHistory.setSizeFull();
        secondRowLayout.addComponent(nfInitialHistory);
        nfCurrentHistory = new NumberField("Número de historia actual");
        nfCurrentHistory.setDecimalAllowed(false);
        nfCurrentHistory.setSizeFull();
        secondRowLayout.addComponent(nfCurrentHistory);
        
        VerticalLayout dataLayout = new VerticalLayout();
        dataLayout.setSpacing(true);
        dataLayout.setMargin(true);
        dataLayout.setSizeFull();
        dataLayout.addComponents(imgCompany, upload, firstRowLayout, secondRowLayout);
        return dataLayout;
	}
	
	private void loadData(User user) {
        Company company = user.getCompany();
        txNit.setValue(company.getDocument());
        txName.setValue(company.getName());
        nfInitialHistory.setValue(company.getInitialCustomId().toString());
        nfCurrentHistory.setValue(company.getActualCustomId().toString());
        if (company.getPhotoBlob() != null) {
        	imgCompany.setSource(StreamResourceUtil.loadImage(company.getPhotoBlob()));
        }
	}
	
	private void save() {
		try {
			User user = getSession().getAttribute(User.class);
			byte[] photoBlob = StreamResourceUtil.getByteArray(imgCompany.getSource());
			BigInteger actualCustomId = new BigInteger(nfCurrentHistory.getValue());
			BigInteger initialCustomId = new BigInteger(nfInitialHistory.getValue());
			Company company = Company.builder(user.getCompany())
				.photoBlob(photoBlob)
				.actualCustomId(actualCustomId)
				.initialCustomId(initialCustomId)
				.build();
			CompanyBll.getInstance().update(company);
			
			user = User.builder(user).company(company).build();
			getSession().setAttribute(User.class, user);
			
			new Notification("Datos actualizados correctamente",Notification.Type.TRAY_NOTIFICATION)
				.show(Page.getCurrent());
		} catch (IOException ex) {
			new Notification("Los datos de la veterinaria no pudieron ser actualizados, contacte al desarrollador (3007200405)",
					Notification.Type.ERROR_MESSAGE)
				.show(Page.getCurrent());
			log.error(ex);
		}
	}
}