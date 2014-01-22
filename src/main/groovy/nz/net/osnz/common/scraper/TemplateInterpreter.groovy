package nz.net.osnz.common.scraper

import nz.net.osnz.common.scraper.processor.BeforeParseProcessor
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Autowired


class TemplateInterpreter {

    @Autowired(required = false)
    protected List<BeforeParseProcessor> parseProcessors;

    /**
     * The parsed document
     */
    private static Map<ScraperLayout, Document> parsedDoc = new HashMap<>();

    /**
     * Layout configuration
     */
    private ScraperLayout layout;

    /**
     * The list of resources will be removed from JSoup document
     */
    private List<String> excludeResources = [];

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

        this.layout.filter.each {
            if (StringUtils.isNotBlank(it)) {
                if (it.indexOf(',') >= 0) {
                    excludeResources.addAll(encodeAsRegularExpression(it).split(','))
                } else {
                    excludeResources.add(encodeAsRegularExpression(it))
                }
            }
        }

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

                doFilterResources(doc)

                parseProcessors?.each { BeforeParseProcessor processor ->
                    processor.perform(doc)
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
    public String getBodyWithContent(String container, String content, boolean leftBar = false, String leftBarContainerId = 'leftBar', boolean rightBar = false, String rightBarContainerId = 'rightBar') {
        Document freshDoc = this.getTemplateContent().clone()

        if (!leftBar && leftBarContainerId) {
            freshDoc.select("#${leftBarContainerId}")?.remove()
        }

        if (!rightBar && rightBarContainerId) {
            freshDoc.select("#${rightBarContainerId}")?.remove()
        }

        if (!StringUtils.isEmpty(this.layout.container)) {
            container = this.layout.container
        }

        freshDoc.select("#${container}").html(content)

        return freshDoc.select("body").html()
    }

    /**
     * Remove those required excluded resources
     *
     * @param doc - JSoup document to go through
     */
    protected void doFilterResources(Document doc) {
        def skipResources = []

        doc.select('link[type=text/css]').each { Element node ->
            if (isSkip(node.attr('href'))) {
                skipResources.add(node)
            }
        }

        doc.select('script').each { Element node ->
            if (StringUtils.isBlank(node.attr('src')) || isSkip(node.attr('src'))) {
                skipResources.add(node)
            }
        }

        removeSkipResources(skipResources)
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
     * Validate whether the given resource is excluded or not
     * @param resource - given resource
     * @return - true means the given resource is excluded
     */
    protected boolean isSkip(String resource) {

        boolean skip = false

        excludeResources.each {
            while (resource.contains('#') || resource.contains('?')) {
                if (resource.contains('#')) {
                    resource = resource.substring(0, resource.indexOf('#')).toLowerCase()
                }
                if (resource.contains('?')) {
                    resource = resource.substring(0, resource.indexOf('?')).toLowerCase()
                }
            }
            skip |= resource.matches(it)
        }
        return skip
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
     * Encode a given string as regular expression
     *
     * @param - given string to encode
     * @return - a string can be used in regular expression
     */
    protected String encodeAsRegularExpression(String value) {
        return value.replace('.', '\\.').replace('*', '.*').replace(' ', '').toLowerCase()
    }

}
