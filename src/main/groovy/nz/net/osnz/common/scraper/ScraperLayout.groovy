package nz.net.osnz.common.scraper

class ScraperLayout {

    public static final String DEFAULT_LAYOUT = "default";

    private String url;
    private String assets;
    private String container;

    /**
     * Initialize data-members
     *
     * @param name
     * @param url
     * @param assets
     */

    private ScraperLayout(final String url, final String assets, final String container) {
        this.url = url;
        this.assets = assets;
        this.container = container;
    }

    /**
     * Return a scraper layout instance
     *
     * @param url is the url to pass through
     * @param assets is the asset url to pass through
     * @param filter is the filter to remove remote resources from header
     * @return
     */
    public static ScraperLayout newLayout(final String url, final String assets, final String container) {
        if (url && assets) {
            return new ScraperLayout(url, assets, container);
        }
        throw new IllegalArgumentException("Either url, or asset were empty");
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(final String assets) {
        this.assets = assets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setContainer(final String container) {
        this.container = container;
    }

    public String getContainer() {
        return this.container;
    }

}
