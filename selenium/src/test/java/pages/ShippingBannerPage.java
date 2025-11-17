package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for interacting with the shipping banner component.
 * The shipping banner displays free shipping threshold information based on cart total.
 */
public class ShippingBannerPage extends BasePage {
    
    // Locators
    private static final By SHIPPING_BANNER = By.className("shipping-banner");
    private static final By SHIPPING_BANNER_MESSAGE = By.className("shipping-banner-message");
    private static final By SHIPPING_BANNER_SUCCESS_MESSAGE = By.className("shipping-banner-success-message");
    private static final By SHIPPING_BANNER_PROGRESS = By.className("shipping-banner-progress");
    private static final By SHIPPING_BANNER_PROGRESS_BAR = By.className("shipping-banner-progress-bar");
    private static final By SHIPPING_BANNER_ICON = By.className("shipping-banner-icon");

    public ShippingBannerPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Checks if the shipping banner is displayed.
     * 
     * @return true if shipping banner is visible
     */
    public boolean isShippingBannerDisplayed() {
        try {
            WebElement banner = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER));
            return banner.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the shipping banner to be displayed.
     */
    public void waitForShippingBanner() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER));
    }
    
    /**
     * Waits for the banner to be in a stable state (either info or success).
     * This helps ensure the shipping threshold API call has completed.
     */
    public void waitForBannerState() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER));
        // Wait a bit for API call to complete and banner state to stabilize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Verify banner is in either info or success state (not in transition)
        boolean isStable = isFreeShippingInfo() || isFreeShippingQualified();
        int attempts = 0;
        while (!isStable && attempts < 10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            isStable = isFreeShippingInfo() || isFreeShippingQualified();
            attempts++;
        }
    }

    /**
     * Gets the shipping banner message text.
     * 
     * @return Banner message text
     */
    public String getShippingBannerMessage() {
        // Try success message first, then fall back to regular message
        try {
            WebElement successMsg = driver.findElement(SHIPPING_BANNER_SUCCESS_MESSAGE);
            if (successMsg.isDisplayed()) {
                return successMsg.getText();
            }
        } catch (Exception e) {
            // Success message not found, continue
        }
        
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER_MESSAGE));
        return message.getText();
    }

    /**
     * Checks if the banner indicates qualified for free shipping (success state).
     * 
     * @return true if banner is in success state
     */
    public boolean isFreeShippingQualified() {
        try {
            WebElement banner = driver.findElement(SHIPPING_BANNER);
            String classAttribute = banner.getAttribute("class");
            return classAttribute != null && classAttribute.contains("shipping-banner-success");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the banner is in info state (not qualified yet).
     * 
     * @return true if banner is in info state
     */
    public boolean isFreeShippingInfo() {
        try {
            WebElement banner = driver.findElement(SHIPPING_BANNER);
            String classAttribute = banner.getAttribute("class");
            return classAttribute != null && classAttribute.contains("shipping-banner-info");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the progress bar is displayed (should only be visible when not qualified).
     * 
     * @return true if progress bar is visible
     */
    public boolean isProgressBarDisplayed() {
        try {
            WebElement progressBar = driver.findElement(SHIPPING_BANNER_PROGRESS);
            return progressBar.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the progress bar width percentage.
     * Only works if the progress bar is displayed (banner is in info state).
     * 
     * @return Progress percentage (0-100), or -1 if progress bar is not displayed
     */
    public double getProgressBarPercentage() {
        // Only get progress if banner is in info state (not qualified)
        if (!isFreeShippingInfo() || !isProgressBarDisplayed()) {
            return -1.0; // Progress bar is not visible when qualified
        }
        
        try {
            WebElement progressBar = wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER_PROGRESS_BAR));
            
            // Try to get from style attribute first (more reliable)
            String style = progressBar.getAttribute("style");
            if (style != null && style.contains("width:")) {
                String widthStr = style.split("width:")[1].split("%")[0].trim();
                return Double.parseDouble(widthStr);
            }
            
            // Fallback: Get from CSS width
            String width = progressBar.getCssValue("width");
            String containerWidth = driver.findElement(SHIPPING_BANNER_PROGRESS).getCssValue("width");
            
            if (width != null && containerWidth != null && !width.isEmpty() && !containerWidth.isEmpty()) {
                // Extract numeric values (remove "px" suffix)
                double widthValue = Double.parseDouble(width.replace("px", "").trim());
                double containerValue = Double.parseDouble(containerWidth.replace("px", "").trim());
                
                if (containerValue > 0) {
                    return (widthValue / containerValue) * 100;
                }
            }
        } catch (Exception e) {
            // If unable to parse, return -1
        }
        return -1.0;
    }

    /**
     * Gets the remaining amount needed for free shipping from the banner message.
     * 
     * @return Remaining amount, or -1 if unable to parse
     */
    public double getRemainingAmount() {
        try {
            String message = getShippingBannerMessage();
            // Message format: "Add $XX.XX more to qualify for FREE shipping!"
            if (message.contains("Add $")) {
                String amountStr = message.split("Add \\$")[1].split(" more")[0].trim();
                return Double.parseDouble(amountStr);
            }
        } catch (Exception e) {
            // Unable to parse
        }
        return -1.0;
    }

    /**
     * Checks if the success icon (🎉) is displayed.
     * 
     * @return true if success icon is visible
     */
    public boolean isSuccessIconDisplayed() {
        try {
            WebElement icon = driver.findElement(SHIPPING_BANNER_ICON);
            return icon.isDisplayed() && icon.getText().contains("🎉");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the full banner element for additional checks.
     * 
     * @return Shipping banner WebElement
     */
    public WebElement getShippingBanner() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(SHIPPING_BANNER));
    }

    /**
     * Checks if banner message contains specific text.
     * 
     * @param text Text to search for
     * @return true if message contains the text
     */
    public boolean messageContains(String text) {
        try {
            String message = getShippingBannerMessage();
            return message.contains(text);
        } catch (Exception e) {
            return false;
        }
    }
}

