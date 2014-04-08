package nz.net.osnz.common.scraper

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.text.ParseException

@Component('scraperConfiguration')
public class ScraperConfiguration {

    /**
     * Layout information
     */
    private Map<String, ScraperLayout> layouts;

    /**
     * Parse the properties file
     *
     * @param properties
     */
    @PostConstruct
    protected void parseProperties() throws ParseException {
        this.layouts = [:]

        String layoutNames = System.getProperty('scraper.layouts', "default")

        layoutNames.split(",").each { String token ->

            String sanitizedToken = token.trim()

            if (!sanitizedToken) {
                return
            }

            this.layouts[sanitizedToken] = this.parseSpecificLayout(sanitizedToken)
        }
    }

    /**
     * Get layout specific information
     *
     * @param properties are the properties to parse
     * @return a mapping for this layout containing the base url, and the scrape url
     */
    protected ScraperLayout parseSpecificLayout(String layout) throws ParseException {

        String url = System.getProperty("scraper.${layout}.url"),
               assetBase = System.getProperty("scraper.${layout}.assetbase"),
               container = System.getProperty("scraper.${layout}.container", "main");

        if (!url) {
            throw new IllegalStateException("Unable to properly parse the layout information for: ${layout}")
        }

        if (!assetBase) {
            assetBase = url;
        }

        return ScraperLayout.newLayout(url, assetBase, container);
    }

    /**
     * @return a list of layout identifiers
     */
    public String[] getLayoutNames() {
        return this.layouts.keySet().toArray(new String[this.layouts.keySet().size()]);
    }

    /**
     * Get layout information
     *
     * @param layoutName is the layout name
     * @return
     */
    public ScraperLayout getLayoutInformation(String layoutName) {
        if (!isValidLayout(layoutName)) {
            return null;
        }
        return layouts[layoutName]
    }

    /**
     * Is this a valid layout
     *
     * @param layoutName the name to check for
     * @return true if it is a valid layout name
     */
    public boolean isValidLayout(String layoutName) {
        return layoutName.trim() in this.layoutNames
    }


}
