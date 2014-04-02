package nz.net.osnz.common.scraper.polisher

import org.jsoup.select.Elements

/**
 * @author Kefeng Deng
 *
 * Template render polisher for scraper
 */
public interface ElementsPolisher {

    /**
     * polish the selected elements before render to HTML
     *
     * @param elements is the selected elements that want to deal with
     * @return elements after some magic actions
     */
    public Elements polishElements(Elements elements)

}