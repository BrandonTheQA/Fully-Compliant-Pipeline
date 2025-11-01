package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the orders page.
 */
public class OrdersPage extends BasePage {
    
    // Locators
    private static final By ORDERS_HEADING = By.xpath("//h1[contains(text(), 'Orders')]");
    private static final By CREATE_ORDER_BUTTON = By.xpath("//button[contains(text(), 'Create Order')]");
    private static final By MY_ORDERS_BUTTON = By.xpath("//button[contains(text(), 'My Orders')]");
    private static final By INFO_MESSAGE = By.className("info-message");
    private static final By ERROR_MESSAGE = By.className("error-message");
    private static final By ORDER_SUCCESS_CONTAINER = By.className("order-success");
    private static final By ORDER_DETAILS_CONTAINER = By.className("order-details-container");
    private static final By ORDER_ID_DISPLAY = By.xpath("//div[contains(@class, 'order-details-container')]//span[contains(@class, 'label')][contains(text(), 'Order ID:')]/following-sibling::span");
    private static final By ORDER_TOTAL_DISPLAY = By.xpath("//div[contains(@class, 'order-details-container')]//span[contains(@class, 'label')][contains(text(), 'Total Amount:')]/following-sibling::span");
    private static final By ORDER_STATUS_DISPLAY = By.xpath("//div[contains(@class, 'order-details-container')]//span[contains(@class, 'label')][contains(text(), 'Status:')]/following-sibling::span");
    private static final By CART_ITEMS = By.cssSelector(".cart-item");
    private static final By PLACE_ORDER_BUTTON = By.xpath("//button[contains(text(), 'Place Order')]");
    private static final By TOTAL_AMOUNT_DISPLAY = By.cssSelector(".total-amount");
    private static final By ORDERS_LIST = By.cssSelector(".orders-grid, .orders-list");
    private static final By ORDER_CARDS = By.cssSelector(".order-card");
    private static final By QUANTITY_CONTROL_PLUS = By.xpath("//div[contains(@class, 'quantity-control')]//button[contains(text(), '+')]");
    private static final By QUANTITY_CONTROL_MINUS = By.xpath("//div[contains(@class, 'quantity-control')]//button[contains(text(), '-')]");

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the orders page.
     */
    public void navigateToOrdersPage() {
        navigateTo("/orders");
    }

    /**
     * Clicks the "Create Order" button.
     */
    public void clickCreateOrderButton() {
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(CREATE_ORDER_BUTTON));
        createButton.click();
    }

    /**
     * Clicks the "My Orders" button.
     */
    public void clickMyOrdersButton() {
        WebElement myOrdersButton = wait.until(ExpectedConditions.elementToBeClickable(MY_ORDERS_BUTTON));
        myOrdersButton.click();
    }

    /**
     * Verifies the info message is displayed (for empty cart or no user scenarios).
     */
    public void verifyInfoMessage() {
        wait.until(ExpectedConditions.presenceOfElementLocated(INFO_MESSAGE));
    }

    /**
     * Gets the info message text.
     * 
     * @return Info message text
     */
    public String getInfoMessage() {
        WebElement infoMsg = wait.until(ExpectedConditions.presenceOfElementLocated(INFO_MESSAGE));
        return infoMsg.getText();
    }

    /**
     * Checks if the info message contains specific text.
     * 
     * @param text Text to check for
     * @return true if info message contains the text
     */
    public boolean isInfoMessageDisplayed(String text) {
        try {
            WebElement infoMsg = wait.until(ExpectedConditions.presenceOfElementLocated(INFO_MESSAGE));
            return infoMsg.getText().contains(text);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Submits the order form (places the order).
     */
    public void submitOrder() {
        WebElement placeOrderButton = wait.until(ExpectedConditions.elementToBeClickable(PLACE_ORDER_BUTTON));
        placeOrderButton.click();
    }

    /**
     * Verifies order details are displayed (after order is created).
     */
    public void verifyOrderSuccess() {
        wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_DETAILS_CONTAINER));
    }

    /**
     * Gets the displayed order ID.
     * 
     * @return Order ID text
     */
    public String getOrderIdDisplayed() {
        WebElement orderIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_ID_DISPLAY));
        return orderIdElement.getText();
    }

    /**
     * Gets the displayed total amount.
     * 
     * @return Total amount text
     */
    public String getTotalAmountDisplayed() {
        WebElement totalElement = wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_TOTAL_DISPLAY));
        return totalElement.getText();
    }

    /**
     * Gets the displayed order status.
     * 
     * @return Order status text
     */
    public String getOrderStatusDisplayed() {
        WebElement statusElement = wait.until(ExpectedConditions.presenceOfElementLocated(ORDER_STATUS_DISPLAY));
        return statusElement.getText();
    }

    /**
     * Gets the cart total before placing order.
     * 
     * @return Cart total text
     */
    public String getCartTotal() {
        WebElement totalElement = wait.until(ExpectedConditions.presenceOfElementLocated(TOTAL_AMOUNT_DISPLAY));
        return totalElement.getText();
    }

    /**
     * Gets count of items in cart.
     * 
     * @return Number of cart items
     */
    public int getCartItemCount() {
        List<WebElement> cartItems = driver.findElements(CART_ITEMS);
        return cartItems.size();
    }

    /**
     * Prints cart items for debugging.
     */
    public void printCartItems() {
        List<WebElement> cartItems = driver.findElements(CART_ITEMS);
        System.out.println("Cart has " + cartItems.size() + " items:");
        for (WebElement item : cartItems) {
            System.out.println("  - " + item.getText());
        }
    }

    /**
     * Verifies orders list is displayed.
     */
    public void verifyOrdersListDisplayed() {
        wait.until(ExpectedConditions.presenceOfElementLocated(ORDERS_LIST));
    }

    /**
     * Gets count of orders displayed.
     * 
     * @return Number of orders
     */
    public int getOrdersCount() {
        List<WebElement> orderCards = driver.findElements(ORDER_CARDS);
        return orderCards.size();
    }

    /**
     * Updates the quantity of a cart item by clicking the increment button.
     * Finds the cart item by product name and clicks the + button the specified number of times.
     * 
     * @param productName Product name to update quantity for
     * @param additionalQuantity Number of times to click the + button
     */
    public void updateCartItemQuantity(String productName, int additionalQuantity) {
        // Wait for cart items to be present
        List<WebElement> cartItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(CART_ITEMS));
        
        // Find the cart item with the matching product name
        for (WebElement cartItem : cartItems) {
            // Find the h4 element containing the product name
            WebElement nameElement = cartItem.findElement(By.cssSelector("h4"));
            if (nameElement.getText().equals(productName)) {
                // Find the + button within this cart item
                WebElement plusButton = cartItem.findElement(QUANTITY_CONTROL_PLUS);
                
                // Click the + button the specified number of times
                for (int i = 0; i < additionalQuantity; i++) {
                    plusButton.click();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                break;
            }
        }
    }
}

