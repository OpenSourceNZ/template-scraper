package nz.net.osnz.common.scraper

import nz.net.osnz.common.scraper.util.SpringBodyTagSupportAdapter
import nz.net.osnz.common.scraper.util.TemplateLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.servlet.jsp.JspWriter
import javax.servlet.jsp.tagext.BodyContent

class TemplateBodyTag extends SpringBodyTagSupportAdapter {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(TemplateBodyTag.class);

    TemplateLayout templateLayout = new TemplateLayout()

    @Inject ScraperConfiguration scraperConfiguration

    /**
     * Called after this tag was closed off.
     *
     * @return skip_body so we won't render the body.
     */
    @Override
    int wrappedDoAfterBody() {

        BodyContent body = this.getBodyContent()
        JspWriter out = body.getEnclosingWriter()

        TemplateInterpreter templateInterpreter = new TemplateInterpreter(scraperConfiguration.getLayoutInformation(this.layout))

        try {
            out.print(templateInterpreter.getBodyWithContent(body.string, templateLayout))
        }
        catch (IOException ioEx) {
            LOG.info("Unable to properly parse the template body for layout ${this.layout}")
        }

        return SKIP_BODY
    }

    /**
     * Attribute setter and getter
     */

    public void setContainer(String container) {
        templateLayout.mainContainerId = container
    }

    public String getContainer() {
        return templateLayout.mainContainerId
    }

    public boolean getRightBar() {
        return templateLayout.displayRightBar
    }

    public void setRightBar(boolean rightBar) {
        templateLayout.displayRightBar = rightBar
    }

    public void setRightBarId(String rightBarId) {
        templateLayout.rightBarContainerId = rightBarId
    }

    public String getRightBarId() {
        return templateLayout.rightBarContainerId
    }

    public boolean getLeftBar() {
        return templateLayout.displayLeftBar
    }

    public void setLeftBar(boolean leftBar) {
        templateLayout.displayLeftBar = leftBar
    }

    public String getLeftBarId() {
        return templateLayout.leftBarContainerId
    }

    public void setLeftBarId(String leftBarId) {
        templateLayout.leftBarContainerId = leftBarId
    }

    public String getLayout() {
        return templateLayout?.layout ?: ScraperLayout.DEFAULT_LAYOUT
    }

    /**
     * Set the layout. Will check validity of layout name
     *
     * @param layout is the layout name to use.
     */
    public void setLayout(String layout) {
        if (scraperConfiguration.isValidLayout(layout)) {
            templateLayout.layout = layout
        } else {
            throw new IllegalArgumentException("No such layout defined in the scraper configuration")
        }
    }


}
