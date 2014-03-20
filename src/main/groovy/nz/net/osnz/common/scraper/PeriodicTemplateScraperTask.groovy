package nz.net.osnz.common.scraper

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.inject.Inject
import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

public class PeriodicTemplateScraperTask implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(PeriodicTemplateScraperTask.class);

    /**
     * Refresh every 20 minutes
     */
    public static final long INTERVAL_LENGTH = 1000 * 60 * 20;

    /**
     * Interval is running
     */
    private boolean intervalRunning = true;

    /**
     * Contains reference to thread that is waiting.
     */
    private Thread intervalThread

    @Inject ScraperConfiguration scraperConfiguration

    /**
     * Initialize a thread that fires off every INTERVAL_LENGTH ms
     *
     * @param servletContextEvent
     */
    @Override
    void contextInitialized(ServletContextEvent servletContextEvent) {

        ApplicationContext appCtx = WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.servletContext)
        appCtx.getAutowireCapableBeanFactory().autowireBean(this)

        log.debug("Context initialized");
        this.intervalThread = new Thread([
                run: { ->
                    this.fireEvent(servletContextEvent.servletContext)
                }
        ] as Runnable
        ).start()
    }

    /**
     * Upon destroy make sure to kill the thread
     *
     * @param servletContextEvent
     */
    @Override
    void contextDestroyed(ServletContextEvent servletContextEvent) {
        this.intervalRunning = false
    }

    /**
     * Fire an event every INTERVAL_LENGTH
     */
    protected void fireEvent(ServletContext context) {


        while (intervalRunning) {
            log.debug("Starting to refresh the template")

            scraperConfiguration.layoutNames.each { String layoutName ->
                ScraperLayout layout = scraperConfiguration.getLayoutInformation(layoutName)
                TemplateInterpreter interpreter = new TemplateInterpreter(layout);

                // refresh this layout.
                interpreter.getTemplateContent(true)

            }

            try {
                Thread.sleep(INTERVAL_LENGTH)
            }
            catch (InterruptedException iEx) {
                log.info("Was interrupt during execution of interval thread, retrying");
            }

        }
    }

}
