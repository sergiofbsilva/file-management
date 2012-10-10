package pt.ist.file.rest.utils.presentationTier.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class FileRestUtilsInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		
	}
	
	@Override
    public void contextDestroyed(ServletContextEvent event) {
		
    }
}
