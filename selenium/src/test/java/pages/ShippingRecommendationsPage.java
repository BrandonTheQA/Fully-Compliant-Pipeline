package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for interacting with the shipping recommendations component.
 * The shipping recommendations component displays product recommendations to help users reach free shipping threshold.
 */
public class ShippingRecommendationsPage extends BasePage {
    
    // Locators
    private static final By SHIPPING_RECOMMENDATIONS = By.className("shipping-recommendations");
    private static final By RECOMMENDATIONS_TITLE = By.xpath("//h3[contains(text(), 'Get FREE Shipping')]");
    private static final By RECOMMENDATIONS_SUBTITLE = By.className("shipping-recommendations-subtitle");
    private static final By RECOMMENDATIONS_LOADING = By.className("shipping-recommendations-loading");
    private static final By RECOMMENDATIONS_TABS = By.className("shipping-recommendations-tab");
    private static final By PRODUCT_RECOMMENDATION_CARD = By.className("product-recommendation-card");
    private static final By PRODUCT_RECOMMENDATION_NAME = By.className("product-recommendation-name");
    private static final By PRODUCT_RECOMMENDATION_PRICE = By.className("product-recommendation-price");
    private static final By PRODUCT_RECOMMENDATION_ADD_BTN = By.className("product-recommendation-add-btn");
    private static final By OPTIMIZATION_PATH_ADD_ALL = By.className("optimization-path-add-all");

    public ShippingRecommendationsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Checks if the shipping recommendations component is displayed.
     * 
     * @return true if recommendations are visible
     */
    public boolean isShippingRecommendationsDisplayed() {
        try {
            WebElement recommendations = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_RECOMMENDATIONS));
            return recommendations.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the shipping recommendations to be displayed.
     */
    public void waitForShippingRecommendations() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_RECOMMENDATIONS));
        // Wait a bit for API call to complete and recommendations to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if recommendations are in loading state.
     * 
     * @return true if loading indicator is displayed
     */
    public boolean isLoading() {
        try {
            WebElement loading = driver.findElement(RECOMMENDATIONS_LOADING);
            return loading.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the recommendations header title.
     * 
     * @return Header title text
     */
    public String getRecommendationsTitle() {
        try {
            WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(RECOMMENDATIONS_TITLE));
            return title.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets the recommendations subtitle (remaining amount message).
     * 
     * @return Subtitle text
     */
    public String getRecommendationsSubtitle() {
        try {
            WebElement subtitle = wait.until(ExpectedConditions.presenceOfElementLocated(RECOMMENDATIONS_SUBTITLE));
            return subtitle.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets all product recommendation cards.
     * 
     * @return List of product recommendation WebElements
     */
    public List<WebElement> getProductRecommendationCards() {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(PRODUCT_RECOMMENDATION_CARD));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Gets the number of product recommendations displayed.
     * 
     * @return Number of recommendations
     */
    public int getRecommendationCount() {
        return getProductRecommendationCards().size();
    }

    /**
     * Gets the product name from a recommendation card.
     * 
     * @param cardIndex Index of the recommendation card (0-based)
     * @return Product name, or empty string if not found
     */
    public String getProductName(int cardIndex) {
        try {
            List<WebElement> cards = getProductRecommendationCards();
            if (cardIndex >= 0 && cardIndex < cards.size()) {
                WebElement nameElement = cards.get(cardIndex).findElement(PRODUCT_RECOMMENDATION_NAME);
                return nameElement.getText();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    /**
     * Gets the product price from a recommendation card.
     * 
     * @param cardIndex Index of the recommendation card (0-based)
     * @return Product price text, or empty string if not found
     */
    public String getProductPrice(int cardIndex) {
        try {
            List<WebElement> cards = getProductRecommendationCards();
            if (cardIndex >= 0 && cardIndex < cards.size()) {
                WebElement priceElement = cards.get(cardIndex).findElement(PRODUCT_RECOMMENDATION_PRICE);
                return priceElement.getText();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    /**
     * Clicks the "Add to Cart" button for a specific recommendation.
     * 
     * @param cardIndex Index of the recommendation card (0-based)
     */
    public void clickAddToCart(int cardIndex) {
        try {
            List<WebElement> cards = getProductRecommendationCards();
            if (cardIndex >= 0 && cardIndex < cards.size()) {
                WebElement addButton = cards.get(cardIndex).findElement(PRODUCT_RECOMMENDATION_ADD_BTN);
                addButton.click();
                // Wait a bit for cart to update
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Clicks the "Add to Cart" button for a product by name.
     * 
     * @param productName Name of the product to add
     */
    public void clickAddToCartByName(String productName) {
        try {
            List<WebElement> cards = getProductRecommendationCards();
            for (WebElement card : cards) {
                WebElement nameElement = card.findElement(PRODUCT_RECOMMENDATION_NAME);
                if (nameElement.getText().equals(productName)) {
                    WebElement addButton = card.findElement(PRODUCT_RECOMMENDATION_ADD_BTN);
                    addButton.click();
                    // Wait a bit for cart to update
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Gets all recommendation tabs (for multiple paths).
     * 
     * @return List of tab WebElements
     */
    public List<WebElement> getRecommendationTabs() {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(RECOMMENDATIONS_TABS));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Clicks a recommendation tab by index.
     * 
     * @param tabIndex Index of the tab (0-based)
     */
    public void clickTab(int tabIndex) {
        try {
            List<WebElement> tabs = getRecommendationTabs();
            if (tabIndex >= 0 && tabIndex < tabs.size()) {
                tabs.get(tabIndex).click();
                // Wait for content to switch
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Checks if "Add All" button is displayed (for bundle paths).
     * 
     * @return true if Add All button is present
     */
    public boolean hasAddAllButton() {
        try {
            WebElement addAllButton = driver.findElement(OPTIMIZATION_PATH_ADD_ALL);
            return addAllButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clicks the "Add All" button (for bundle paths).
     */
    public void clickAddAll() {
        try {
            WebElement addAllButton = wait.until(ExpectedConditions.elementToBeClickable(OPTIMIZATION_PATH_ADD_ALL));
            addAllButton.click();
            // Wait a bit for cart to update
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Checks if recommendations are hidden (when qualifies for free shipping).
     * 
     * @return true if recommendations are not displayed
     */
    public boolean isHidden() {
        try {
            List<WebElement> elements = driver.findElements(SHIPPING_RECOMMENDATIONS);
            return elements.isEmpty() || !elements.get(0).isDisplayed();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extracts numeric value from currency string (e.g., "$20.00" -> 20.00).
     * 
     * @param currencyString Currency string with $ prefix
     * @return Numeric value, or -1 if unable to parse
     */
    public double extractCurrencyValue(String currencyString) {
        try {
            if (currencyString == null || currencyString.trim().isEmpty()) {
                return -1.0;
            }
            // Remove $ and any whitespace
            String cleaned = currencyString.replace("$", "").replace(",", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return -1.0;
        }
    }
}

