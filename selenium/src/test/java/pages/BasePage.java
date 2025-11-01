package pages;

import config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base page object class providing common functionality for all page objects.
 */
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = TestConfig.createWebDriverWait(driver);
        this.baseUrl = TestConfig.BASE_URL;
    }

    /**
     * Navigates to the page URL.
     * 
     * @param url relative or absolute URL to navigate to
     */
    protected void navigateTo(String url) {
        if (url.startsWith("http")) {
            driver.get(url);
        } else {
            driver.get(baseUrl + url);
        }
    }
}

