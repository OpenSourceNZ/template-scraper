package nz.net.osnz.common.scraper.util

import groovy.transform.CompileStatic
import nz.net.osnz.common.scraper.ScraperLayout

/**
 * @author Kefeng Deng
 */
@CompileStatic
public class TemplateLayout {

    String layout = ScraperLayout.DEFAULT_LAYOUT;

    /**
     * the ID of container will be replaced
     */
    String mainContainerId = 'main'

    /**
     * the container ID of left bar
     */
    String leftBarContainerId = 'leftBar'

    /**
     * the container ID of right bar
     */
    String rightBarContainerId = 'rightBar'

    /**
     * Has left bar?
     */
    Boolean displayLeftBar = true

    /**
     * Has right bar?
     */
    Boolean displayRightBar = true

    public TemplateLayout() {
        // nothing to do
    }

}
