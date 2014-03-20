package nz.net.osnz.common.scraper

import nz.net.osnz.common.scraper.util.TemplateLayout
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class TemplateInterpreter {

    /**
     * The parsed document
     */
    private static Map<ScraperLayout, Document> parsedDoc = new HashMap<>();

    /**
     * Layout configuration
     */
    private ScraperLayout layout;

    /**
     * Layout information
     *
     * @param layout is the layout to use
     */
    public TemplateInterpreter(ScraperLayout layout) {

        if (layout == null) {
            throw new IllegalArgumentException("Not a valid layout instance");
        }

        this.layout = layout;

    }

    /**
     * Retrieve the template content
     */
    public Document getTemplateContent(boolean doRefresh = false) {

        if (!parsedDoc[layout] || doRefresh) {

            Document doc = getTemplateDocument()

            if (doc) {

                doc.select("html")

                // remove the title
                doc.select("head title").remove()

                // remove the analytics block
                removeAnalyticsScriptBlock(doc)

                // sanitize stylesheet urls
                doc.select("[href]").each {
                    node -> rebaseAssetForNode(node, "href")
                }
                doc.select("[src]").each {
                    node -> rebaseAssetForNode(node, "src")
                }

                parsedDoc[layout] = doc
            }
        }

        return parsedDoc[layout]

    }

    /**
     * Retrieve the <head /> content
     *
     * @return
     */
    public String getHtmlHead() {
        return this.getTemplateContent().select("head").html();
    }

    /**
     * Get the university body
     *
     * @param leftBar show the left
     * @param rightBar
     * @param content
     * @return
     */
    public String getBodyWithContent(String content, TemplateLayout templateLayout) {

        Document freshDoc = this.getTemplateContent().clone()

        if (!templateLayout.displayLeftBar && templateLayout.leftBarContainerId) {
            freshDoc.select("#${templateLayout.leftBarContainerId}")?.remove()
        }

        if (!templateLayout.displayRightBar && templateLayout.rightBarContainerId) {
            freshDoc.select("#${templateLayout.rightBarContainerId}")?.remove()
        }

        if (!StringUtils.isEmpty(this.layout.container)) {
            templateLayout.mainContainerId = this.layout.container
        }

        freshDoc.select("#${templateLayout.mainContainerId}").html(content)

        return freshDoc.select("body").html()
    }

    /**
     * Remove skipp scripts from the given document
     *
     * @param skipResources is the list of skip resources need to be removed
     */
    protected void removeSkipResources(List<Element> skipResources) {
        skipResources.each { Element element ->
            element.remove()
        }
    }

    /**
     * Run closure on nodes that have analytics information
     *
     * @param doc is the jsoup document to go through
     */
    protected void removeAnalyticsScriptBlock(Document doc) {
        for (Element node : doc.select("script")) {
            if (hasAnalyticsInformation(node)) {
                node.remove();
            }
        }
    }

    /**
     * Analytics information?
     *
     * @param node
     * @return
     */
    protected boolean hasAnalyticsInformation(Element node) {
        return node.html().contains("_setAccount");
    }

    /**
     * Sanitize the node
     *
     * @param node node to sanitize
     * @param attr attribute to query
     */
    protected void rebaseAssetForNode(org.jsoup.nodes.Node node, String attr) {
        String url = node.attr(attr);
        if (!isAbsoluteURL(url)) {
            node.attr(attr, this.getRelativeLinksBase() + url)
        }
    }

    /**
     * @return the document parsed from the university's page.
     */
    protected Document getTemplateDocument() throws IOException {
        return Jsoup.connect(getTemplateBaseLocation()).get();
    }

    /**
     * @return the base location for the static files
     */
    protected String getRelativeLinksBase() {
        return layout.getAssets();
    }

    /**
     * @return configured template location
     */
    protected String getTemplateBaseLocation() {
        return layout.getUrl();
    }

    /**
     * Verify whether the given URL is an absolute URL or not
     * @param url - given URL to verify
     * @return - true if the give URL is an absolute URL
     */
    protected boolean isAbsoluteURL(String url) {
        try {
            return StringUtils.isNotBlank(url) && (new URI(url)).absolute
        } catch (URISyntaxException ex) {
            return false
        }
    }

}
