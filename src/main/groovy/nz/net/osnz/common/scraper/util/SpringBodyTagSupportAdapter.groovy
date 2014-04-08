package nz.net.osnz.common.scraper.util

import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.jsp.tagext.BodyTagSupport

/**
 * Author: Marnix
 *
 * This class overrides the doAfterBody method to make sure the class has been properly
 * hooked up with spring beans
 */
public abstract class SpringBodyTagSupportAdapter extends BodyTagSupport {


    /**
     * Set to true when the tag has been initialized properly
     */
    protected boolean initialized = false;

    /**
     * Make sure stuff has been initialized then delegate to wrappedDoAfterBody
     */
    public int doAfterBody() {
        initializeBeansIfNecessary();
        return wrappedDoAfterBody();
    }

    /**
     * Make sure stuff has been initialized then delegate to wrappedDoStartTag
     */
    public int doStartTag() {
        initializeBeansIfNecessary();
        return wrappedDoStartTag();
    }

    /**
     * The actual implementation of the doAfterBody method
     */
    public int wrappedDoAfterBody() {
        return SKIP_BODY
    }

    /**
     * The actual implementation of the do start tag method
     */
    public int wrappedDoStartTag() {
        return EVAL_BODY_BUFFERED
    }

    /**
     * Hook up the spring beans correctly
     */
    protected void initializeBeansIfNecessary() {
        if (!initialized) {
//            ApplicationContext appCtx = WebApplicationContextUtils.getWebApplicationContext(pageContext.servletContext)
//            appCtx.getAutowireCapableBeanFactory().autowireBean(this)
            SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, pageContext.servletContext);
            this.initialized = true
        }
    }

}
