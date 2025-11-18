package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for interacting with the shipping cost calculator component.
 * The shipping cost calculator displays estimated shipping cost, free shipping progress, and total cost breakdown.
 */
public class ShippingCostCalculatorPage extends BasePage {
    
    // Locators
    private static final By SHIPPING_COST_CALCULATOR = By.className("shipping-cost-calculator");
    private static final By SHIPPING_COST_DISPLAY = By.className("shipping-cost-display");
    private static final By SHIPPING_COST_LABEL = By.className("shipping-cost-label");
    private static final By SHIPPING_COST_VALUE = By.className("shipping-cost-value");
    private static final By SHIPPING_FREE_TEXT = By.className("shipping-free-text");
    private static final By COST_BREAKDOWN = By.className("cost-breakdown");
    private static final By COST_ROW = By.className("cost-row");
    private static final By COST_TOTAL = By.className("cost-total");
    private static final By TOTAL_AMOUNT = By.className("total-amount");
    private static final By SHIPPING_PROGRESS_WRAPPER = By.className("shipping-progress-wrapper");
    private static final By SUBTOTAL_LABEL = By.xpath("//div[contains(@class, 'cost-row')]//span[contains(text(), 'Subtotal:')]");
    private static final By SHIPPING_LABEL = By.xpath("//div[contains(@class, 'cost-row')]//span[contains(text(), 'Shipping:')]");
    private static final By TOTAL_LABEL = By.xpath("//div[contains(@class, 'cost-total')]//span[contains(text(), 'Total:')]");

    public ShippingCostCalculatorPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Checks if the shipping cost calculator is displayed.
     * 
     * @return true if shipping cost calculator is visible
     */
    public boolean isShippingCostCalculatorDisplayed() {
        try {
            WebElement calculator = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_COST_CALCULATOR));
            return calculator.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the shipping cost calculator to be displayed.
     */
    public void waitForShippingCostCalculator() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_COST_CALCULATOR));
        // Wait a bit for API call to complete and values to stabilize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the estimated shipping cost text.
     * 
     * @return Shipping cost text (e.g., "$5.99" or "FREE")
     */
    public String getShippingCostText() {
        try {
            WebElement costValue = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_COST_VALUE));
            return costValue.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if shipping cost displays as FREE.
     * 
     * @return true if shipping is FREE
     */
    public boolean isShippingFree() {
        try {
            WebElement freeText = driver.findElement(SHIPPING_FREE_TEXT);
            return freeText.isDisplayed() && freeText.getText().contains("FREE");
        } catch (Exception e) {
            String costText = getShippingCostText();
            return costText.contains("FREE");
        }
    }

    /**
     * Gets the subtotal from the cost breakdown.
     * 
     * @return Subtotal text, or empty string if not found
     */
    public String getSubtotal() {
        try {
            WebElement subtotalLabel = wait.until(ExpectedConditions.presenceOfElementLocated(SUBTOTAL_LABEL));
            WebElement costRow = subtotalLabel.findElement(By.xpath("./.."));
            WebElement valueElement = costRow.findElement(By.className("cost-value"));
            return valueElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets the shipping cost from the cost breakdown.
     * 
     * @return Shipping cost text (e.g., "$5.99" or "FREE")
     */
    public String getShippingInBreakdown() {
        try {
            WebElement shippingLabel = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_LABEL));
            WebElement costRow = shippingLabel.findElement(By.xpath("./.."));
            WebElement valueElement = costRow.findElement(By.className("cost-value"));
            return valueElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets the total amount from the cost breakdown.
     * 
     * @return Total amount text
     */
    public String getTotalAmount() {
        try {
            WebElement totalElement = wait.until(ExpectedConditions.presenceOfElementLocated(TOTAL_AMOUNT));
            return totalElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if the shipping progress banner is displayed (when below threshold).
     * 
     * @return true if progress wrapper is displayed
     */
    public boolean isShippingProgressDisplayed() {
        try {
            WebElement progressWrapper = driver.findElement(SHIPPING_PROGRESS_WRAPPER);
            return progressWrapper.isDisplayed() && progressWrapper.findElements(By.className("shipping-banner")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if shipping cost display is in free shipping state (green highlight).
     * 
     * @return true if display has "shipping-free" class
     */
    public boolean isShippingFreeState() {
        try {
            WebElement costDisplay = driver.findElement(SHIPPING_COST_DISPLAY);
            String classAttribute = costDisplay.getAttribute("class");
            return classAttribute != null && classAttribute.contains("shipping-free");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the full cost breakdown section.
     * 
     * @return Cost breakdown WebElement
     */
    public WebElement getCostBreakdown() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(COST_BREAKDOWN));
    }

    /**
     * Checks if cost breakdown contains all required rows (Subtotal, Shipping, Total).
     * 
     * @return true if all rows are present
     */
    public boolean hasAllCostRows() {
        try {
            boolean hasSubtotal = driver.findElements(SUBTOTAL_LABEL).size() > 0;
            boolean hasShipping = driver.findElements(SHIPPING_LABEL).size() > 0;
            boolean hasTotal = driver.findElements(TOTAL_LABEL).size() > 0;
            return hasSubtotal && hasShipping && hasTotal;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts numeric value from currency string (e.g., "$5.99" -> 5.99).
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





