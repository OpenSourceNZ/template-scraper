package nz.net.osnz.common.scraper.processor

import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

import java.lang.annotation.Inherited

/**
 * @author Kefeng Deng
 *
 * Introduce some behaviours before the scraper parse and render the HTML
 */
interface BeforeParseProcessor {

    /**
     * perform some behaviours to given document
     * @param document document to parse
     */
    public void perform(Document document)

}
