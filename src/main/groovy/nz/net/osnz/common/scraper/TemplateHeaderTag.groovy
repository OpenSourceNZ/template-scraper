package nz.net.osnz.common.scraper

import nz.net.osnz.common.scraper.util.SpringBodyTagSupportAdapter

import javax.inject.Inject


public class TemplateHeaderTag extends SpringBodyTagSupportAdapter {

    @Inject ScraperConfiguration scraperConfiguration

    /**
     * Layout to use
     */
    private String layout = ScraperLayout.DEFAULT_LAYOUT;

    private boolean isDebug = false;

    @Override
    public int doStartTag() {
        TemplateInterpreter templateInterpreter = new TemplateInterpreter(scraperConfiguration.getLayoutInformation(this.layout))

        if (this.debug) {
            pageContext.getOut().write("<!-- scraper header start -->");
        }

        pageContext.getOut().write(templateInterpreter.htmlHead)

        return SKIP_BODY
    }

    /**
     * Called after this tag was closed off.
     *
     * @return skip_body so we won't render the body.
     */
    @Override
    public int doAfterBody() {
        if (this.debug) {
            pageContext.getOut().write("<!-- scraper header end -->");
        }

        return SKIP_BODY;
    }

    public boolean isDebug() {
        return this.isDebug
    }

    public void setIsDebug(boolean debug) {
        this.isDebug = debug
    }

    public String getLayout() {
        return layout ?: ScraperLayout.DEFAULT_LAYOUT
    }

    /**
     * Set the layout. Will check validity of layout name
     *
     * @param layout is the layout name to use.
     */
    public void setLayout(String layout) {
        if (scraperConfiguration.isValidLayout(layout)) {
            this.layout = layout
        } else {
            throw new IllegalArgumentException("No such layout defined in the scraper configuration")
        }
    }


}
