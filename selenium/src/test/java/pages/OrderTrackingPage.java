package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the order tracking page.
 */
public class OrderTrackingPage extends BasePage {
    
    // Locators
    private static final By TRACKING_HEADER = By.className("tracking-header");
    private static final By ORDER_ID_HEADER = By.xpath("//div[contains(@class, 'tracking-header')]//h2");
    private static final By STATUS_BADGE = By.cssSelector(".status-badge");
    private static final By TRACKING_NUMBER = By.cssSelector(".tracking-code");
    private static final By CARRIER_NAME = By.cssSelector(".carrier-name");
    private static final By ESTIMATED_DELIVERY = By.xpath("//div[contains(@class, 'delivery-info')]//span[2]");
    private static final By STATUS_TIMELINE = By.className("status-timeline");
    private static final By TIMELINE_ITEMS = By.cssSelector(".timeline-item");
    private static final By CURRENT_STATUS_ITEM = By.cssSelector(".timeline-item.current");
    private static final By ORDER_DETAILS_CARD = By.className("order-details-card");
    private static final By NOTIFICATION_PREFERENCES = By.className("notification-preferences");
    private static final By LIVE_INDICATOR = By.className("live-indicator");
    private static final By BACK_TO_ORDERS_BUTTON = By.xpath("//a[contains(text(), 'Back to Orders')]");
    
    public OrderTrackingPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Navigates to the order tracking page for a specific order.
     * 
     * @param orderId Order ID to track
     */
    public void navigateToTrackingPage(String orderId) {
        navigateTo("/orders/" + orderId + "/tracking");
    }
    
    /**
     * Verifies the tracking page is loaded.
     */
    public void verifyTrackingPageLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(TRACKING_HEADER));
    }
    
    /**
     * Gets the order ID displayed in the header.
     * 
     * @return Order ID text
     */
    public String getOrderIdFromHeader() {
        WebElement orderIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_ID_HEADER));
        return orderIdElement.getText();
    }
    
    /**
     * Gets the current order status.
     * 
     * @return Status text
     */
    public String getCurrentStatus() {
        WebElement statusElement = wait.until(ExpectedConditions.presenceOfElementLocated(STATUS_BADGE));
        return statusElement.getText();
    }
    
    /**
     * Verifies status badge is displayed.
     * 
     * @return true if status badge is displayed
     */
    public boolean isStatusBadgeDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(STATUS_BADGE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the tracking number if available.
     * 
     * @return Tracking number text, or null if not available
     */
    public String getTrackingNumber() {
        try {
            List<WebElement> trackingElements = driver.findElements(TRACKING_NUMBER);
            if (!trackingElements.isEmpty()) {
                return trackingElements.get(0).getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Verifies tracking number is displayed.
     * 
     * @return true if tracking number is displayed
     */
    public boolean isTrackingNumberDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(TRACKING_NUMBER));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the carrier name if available.
     * 
     * @return Carrier name text, or null if not available
     */
    public String getCarrierName() {
        try {
            List<WebElement> carrierElements = driver.findElements(CARRIER_NAME);
            if (!carrierElements.isEmpty()) {
                return carrierElements.get(0).getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets the estimated delivery date if available.
     * 
     * @return Estimated delivery date text, or null if not available
     */
    public String getEstimatedDeliveryDate() {
        try {
            List<WebElement> deliveryElements = driver.findElements(ESTIMATED_DELIVERY);
            if (!deliveryElements.isEmpty()) {
                return deliveryElements.get(0).getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Verifies status timeline is displayed.
     * 
     * @return true if status timeline is displayed
     */
    public boolean isStatusTimelineDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(STATUS_TIMELINE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the count of status history entries.
     * 
     * @return Number of timeline items
     */
    public int getStatusHistoryCount() {
        try {
            List<WebElement> timelineItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TIMELINE_ITEMS));
            return timelineItems.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Verifies current status item is highlighted.
     * 
     * @return true if current status item exists
     */
    public boolean isCurrentStatusHighlighted() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CURRENT_STATUS_ITEM));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifies order details card is displayed.
     * 
     * @return true if order details card is displayed
     */
    public boolean isOrderDetailsCardDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_DETAILS_CARD));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifies notification preferences section is displayed.
     * 
     * @return true if notification preferences is displayed
     */
    public boolean isNotificationPreferencesDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(NOTIFICATION_PREFERENCES));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifies live indicator is displayed (for real-time updates).
     * 
     * @return true if live indicator is displayed
     */
    public boolean isLiveIndicatorDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(LIVE_INDICATOR));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clicks the back to orders button.
     */
    public void clickBackToOrders() {
        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(BACK_TO_ORDERS_BUTTON));
        backButton.click();
    }
    
    /**
     * Gets status text from a timeline item by index.
     * 
     * @param index Index of the timeline item (0-based)
     * @return Status text
     */
    public String getStatusFromTimeline(int index) {
        List<WebElement> timelineItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TIMELINE_ITEMS));
        if (index >= 0 && index < timelineItems.size()) {
            WebElement statusElement = timelineItems.get(index).findElement(By.cssSelector(".status-name"));
            return statusElement.getText();
        }
        return null;
    }
}
