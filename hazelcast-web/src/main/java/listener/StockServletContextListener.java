package listener;

import hazelcast.ClientInstance;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StockServletContextListener implements ServletContextListener {

	private static final Logger LOG = Logger.getLogger(StockServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		LOG.info("Servlet Context Destroyed - Shutting down Hazelcast Client");
		ClientInstance.getInstance().getClient().shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		LOG.info("Servlet Context Initialized - Creating Hazelcast Client");
		ClientInstance.getInstance();
	}
}
