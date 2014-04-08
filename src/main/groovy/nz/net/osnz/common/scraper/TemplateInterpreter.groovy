package nz.net.osnz.common.scraper

import nz.net.osnz.common.scraper.polisher.ContentPolisher
import nz.net.osnz.common.scraper.polisher.ElementsPolisher
import nz.net.osnz.common.scraper.util.TemplateLayout
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

public class TemplateInterpreter {

    /**
     * The parsed document
     */
    private static Map<ScraperLayout, Document> parsedDoc = new HashMap<>();

    /**
     * Layout configuration
     */
    private ScraperLayout layout;

    /**
     * Some Elements polishers to polish the elements
     */
    List<ElementsPolisher> elementsPolishers

    /**
     * Some content polishers to polish the HTML content
     */
    List<ContentPolisher> contentPolishers

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

        elementsPolishers = [] as ArrayList<ElementsPolisher>

        contentPolishers = [] as ArrayList<ContentPolisher>

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
        return renderWithPolishers(this.getTemplateContent().select("head"))
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

        return renderWithPolishers(freshDoc.select("body"))
    }

    public void setContentPolisher(List<ContentPolisher> contentPolisherList) {
        if (contentPolisherList) {
            this.contentPolishers.addAll(contentPolisherList)
        }
    }

    public void setElementsPolisher(List<ElementsPolisher> elementsPolisherList) {
        if (elementsPolisherList) {
            this.elementsPolishers.addAll(elementsPolisherList)
        }
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

    /**
     * Render selected elements to HTML content, and trigger the polisher during the process
     *
     * @param elements are the selected elements will be polished through
     * @return
     */
    protected String renderWithPolishers(Elements elements) {
        return doPolishContent(doPolishElements(elements).html().trim())
    }

    /**
     * Polish the given elements before it be rendered to HTML
     *
     * @param selectedElements are the elements need to be polished
     * @return the elements after polish
     */
    protected Elements doPolishElements(Elements selectedElements) {
        Elements cloneElements = selectedElements.clone()
        elementsPolishers?.each { ElementsPolisher polisher ->
            cloneElements = polisher.polishElements(cloneElements)
        }
        return cloneElements
    }

    /**
     * Polish the HTML content before render onto the page
     *
     * @param htmlContent is the HTML content need to be polished
     * @return the HTML content after polish, and ready to render on the page
     */
    protected String doPolishContent(String htmlContent) {
        // maybe a performance issue because keeping to re-assign a string value, any good suggestion?
        contentPolishers?.each { ContentPolisher polisher ->
            htmlContent = polisher.polishContent(htmlContent)
        }
        return htmlContent
    }


}
