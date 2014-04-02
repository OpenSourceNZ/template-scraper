package nz.net.osnz.common.scraper.polisher

/**
 * @author Kefeng Deng
 *
 * Template render polisher for scraper
 */
public interface ContentPolisher {

    /**
     * customize polish the HTML content that be rendered from elements before it renders on the page
     *
     * @param htmlContent is the HTML content wants to be polished
     * @return a string of HTML content after polish
     */
    public String polishContent(String htmlContent)

}