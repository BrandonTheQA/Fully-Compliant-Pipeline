package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the home page.
 */
public class HomePage extends BasePage {
    
    // Locators
    private static final By NAVIGATION_USER_LINK = By.linkText("User");
    private static final By NAVIGATION_PRODUCTS_LINK = By.linkText("Products");
    private static final By NAVIGATION_ORDERS_LINK = By.linkText("Orders");
    private static final By NAVIGATION_HOME_LINK = By.linkText("Home");
    private static final By WELCOME_HEADING = By.tagName("h1");
    private static final By WORKFLOW_SECTION = By.cssSelector("main > *");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the home page.
     */
    public void navigateToHome() {
        navigateTo("/");
    }

    /**
     * Clicks on the User navigation link.
     */
    public void clickUserLink() {
        WebElement userLink = wait.until(ExpectedConditions.elementToBeClickable(NAVIGATION_USER_LINK));
        userLink.click();
    }

    /**
     * Clicks on the Products navigation link.
     */
    public void clickProductsLink() {
        WebElement productsLink = wait.until(ExpectedConditions.elementToBeClickable(NAVIGATION_PRODUCTS_LINK));
        productsLink.click();
    }

    /**
     * Clicks on the Orders navigation link.
     */
    public void clickOrdersLink() {
        WebElement ordersLink = wait.until(ExpectedConditions.elementToBeClickable(NAVIGATION_ORDERS_LINK));
        ordersLink.click();
    }

    /**
     * Verifies that the welcome heading is displayed.
     */
    public void verifyWelcomeHeading() {
        // Wait for page to load and h1 to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_HEADING));
        // Additional check to ensure text is loaded
        WebElement heading = driver.findElement(WELCOME_HEADING);
        wait.until(ExpectedConditions.textToBePresentInElement(heading, ""));
    }

    /**
     * Gets the text of the welcome heading.
     * 
     * @return Welcome heading text
     */
    public String getWelcomeHeading() {
        WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(WELCOME_HEADING));
        return heading.getText();
    }
}

