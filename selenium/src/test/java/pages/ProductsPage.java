package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the products page.
 */
public class ProductsPage extends BasePage {
    
    // Locators
    private static final By PRODUCTS_HEADING = By.tagName("h1");
    private static final By CREATE_NEW_PRODUCT_BUTTON = By.xpath("//button[contains(text(), 'Create New Product')]");
    private static final By HIDE_CREATE_FORM_BUTTON = By.xpath("//button[contains(text(), 'Hide Create Form')]");
    private static final By PRODUCT_FORM_CONTAINER = By.className("product-form-container");
    private static final By PRODUCT_NAME_INPUT = By.id("name");
    private static final By PRODUCT_DESCRIPTION_TEXTAREA = By.id("description");
    private static final By PRODUCT_PRICE_INPUT = By.id("price");
    private static final By PRODUCT_QUANTITY_INPUT = By.id("quantity");
    private static final By PRODUCT_CATEGORY_INPUT = By.id("category");
    private static final By SUBMIT_PRODUCT_BUTTON = By.xpath("//button[contains(text(), 'Create Product')]");
    private static final By SUCCESS_MESSAGE = By.className("success-message");
    private static final By ERROR_MESSAGE = By.className("error-message");
    private static final By PRODUCT_LIST_CONTAINER = By.className("products-container");
    private static final By PRODUCT_CARDS = By.cssSelector(".product-card, .product-item");
    private static final By PRODUCT_NAMES = By.cssSelector(".product-name, h3");
    private static final By PRODUCT_PRICES = By.cssSelector(".product-price");
    private static final By LOADING_MESSAGE = By.xpath("//*[contains(text(), 'Loading products')]");
    private static final By ADD_TO_CART_BUTTON = By.xpath("//button[contains(text(), 'Add to Cart')]");
    private static final By STOCK_STATUS_BADGE = By.className("stock-status-badge");
    private static final By NOTIFY_ME_BUTTON = By.xpath("//button[contains(text(), 'Notify Me When Available')]");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the products page.
     */
    public void navigateToProductsPage() {
        navigateTo("/products");
    }

    /**
     * Clicks the "Create New Product" button or ensures form is visible.
     */
    public void clickCreateNewProductButton() {
        // First, ensure the page is loaded by waiting for the products heading or container
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(PRODUCTS_HEADING),
                ExpectedConditions.presenceOfElementLocated(PRODUCT_LIST_CONTAINER)
            ));
        } catch (Exception e) {
            // Page might still be loading, continue anyway
        }
        
        // Check if form is already visible
        List<WebElement> existingForms = driver.findElements(PRODUCT_FORM_CONTAINER);
        if (!existingForms.isEmpty() && existingForms.get(0).isDisplayed()) {
            // Form is already visible, no need to click
            return;
        }
        
        // Form not visible, click the button to show it
        // Wait for button to be present first, then clickable
        wait.until(ExpectedConditions.presenceOfElementLocated(CREATE_NEW_PRODUCT_BUTTON));
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(CREATE_NEW_PRODUCT_BUTTON));
        createButton.click();
        // Wait for form to appear and be visible, then wait for name input to be ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_FORM_CONTAINER));
        // Additional wait for the form inputs to be ready
        wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCT_NAME_INPUT));
    }

    /**
     * Fills in the product creation form.
     * 
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param quantity Product quantity
     * @param category Product category
     */
    public void fillProductForm(String name, String description, double price, int quantity, String category) {
        // Wait for form container and name input to be visible and ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_FORM_CONTAINER));
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(PRODUCT_NAME_INPUT));
        nameInput.clear();
        nameInput.sendKeys(name);
        
        WebElement descInput = wait.until(ExpectedConditions.elementToBeClickable(PRODUCT_DESCRIPTION_TEXTAREA));
        descInput.clear();
        descInput.sendKeys(description);
        
        WebElement priceInput = wait.until(ExpectedConditions.elementToBeClickable(PRODUCT_PRICE_INPUT));
        priceInput.clear();
        priceInput.sendKeys(String.valueOf(price));
        
        WebElement quantityInput = wait.until(ExpectedConditions.elementToBeClickable(PRODUCT_QUANTITY_INPUT));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
        
        WebElement categoryInput = wait.until(ExpectedConditions.elementToBeClickable(PRODUCT_CATEGORY_INPUT));
        categoryInput.clear();
        categoryInput.sendKeys(category);
    }

    /**
     * Submits the product creation form.
     */
    public void submitProductForm() {
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_PRODUCT_BUTTON));
        submitButton.click();
    }

    /**
     * Waits for and verifies the success message is displayed.
     */
    public void verifySuccessMessage() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE));
    }

    /**
     * Gets the success message text.
     * 
     * @return Success message text
     */
    public String getSuccessMessage() {
        WebElement successMsg = wait.until(ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE));
        return successMsg.getText();
    }

    /**
     * Waits for product list to load (waits for loading message to disappear).
     */
    public void waitForProductListToLoad() {
        try {
            // Wait for loading message to disappear if it exists
            wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_MESSAGE));
        } catch (Exception e) {
            // Loading message may not appear, which is fine
        }
        // Wait for products container or heading to be present
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(PRODUCT_LIST_CONTAINER),
            ExpectedConditions.presenceOfElementLocated(PRODUCTS_HEADING)
        ));
    }

    /**
     * Gets all product cards currently displayed.
     * 
     * @return List of product card elements
     */
    public List<WebElement> getProductCards() {
        return driver.findElements(PRODUCT_CARDS);
    }

    /**
     * Checks if a product with the given name exists in the product list.
     * 
     * @param productName Product name to search for
     * @return true if product is found
     */
    public boolean isProductDisplayed(String productName) {
        List<WebElement> products = driver.findElements(PRODUCT_CARDS);
        for (WebElement product : products) {
            WebElement nameElement = product.findElement(By.cssSelector("h3"));
            if (nameElement.getText().equals(productName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clicks on a product card to view details or add to cart.
     * 
     * @param productName Product name to click on
     */
    public void clickOnProduct(String productName) {
        List<WebElement> products = driver.findElements(PRODUCT_CARDS);
        for (WebElement product : products) {
            WebElement nameElement = product.findElement(By.cssSelector("h3"));
            if (nameElement.getText().equals(productName)) {
                product.click();
                break;
            }
        }
    }

    /**
     * Gets the count of products displayed.
     * 
     * @return Number of products
     */
    public int getProductCount() {
        return getProductCards().size();
    }

    /**
     * Adds a product to cart by clicking its "Add to Cart" button.
     * 
     * @param productName Product name to add to cart
     */
    public void addProductToCart(String productName) {
        List<WebElement> products = driver.findElements(PRODUCT_CARDS);
        for (WebElement product : products) {
            // Find the h3 element containing the product name
            WebElement nameElement = product.findElement(By.cssSelector("h3"));
            if (nameElement.getText().equals(productName)) {
                WebElement addToCartBtn = product.findElement(ADD_TO_CART_BUTTON);
                addToCartBtn.click();
                // Wait for cart update to complete
                try {
                    Thread.sleep(1000);  // Increased wait time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            }
        }
    }

    /**
     * Adds a product to wishlist by clicking its wishlist button.
     * 
     * @param productName Product name to add to wishlist
     */
    public void addProductToWishlist(String productName) {
        // Locate the product card first
        WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]")
        ));
        
        WebElement wishlistButton = productCard.findElement(By.className("wishlist-button"));
        wishlistButton.click();
        
        // Verify button state changed
        wait.until(ExpectedConditions.attributeContains(wishlistButton, "class", "active"));
    }

    /**
     * Convenience method to create a product in one go.
     * 
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param quantity Product quantity
     * @param category Product category
     */
    public void createProduct(String name, String description, double price, int quantity, String category) {
        clickCreateNewProductButton();
        fillProductForm(name, description, price, quantity, category);
        submitProductForm();
        verifySuccessMessage();
        // Hide form to clear space or just wait
        try {
            wait.until(ExpectedConditions.elementToBeClickable(HIDE_CREATE_FORM_BUTTON)).click();
        } catch (Exception e) {
            // Ignore if button not clickable
        }
    }
    
    /**
     * Checks if stock status badge is displayed for a product.
     * 
     * @param productName Product name to check
     * @return true if stock status badge is displayed
     */
    public boolean isStockStatusBadgeDisplayed(String productName) {
        try {
            WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]")
            ));
            
            List<WebElement> badges = productCard.findElements(STOCK_STATUS_BADGE);
            return !badges.isEmpty() && badges.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if "Add to Cart" button is enabled for a product.
     * 
     * @param productName Product name to check
     * @return true if Add to Cart button is enabled
     */
    public boolean isAddToCartButtonEnabled(String productName) {
        try {
            WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]")
            ));
            
            List<WebElement> addToCartButtons = productCard.findElements(ADD_TO_CART_BUTTON);
            if (!addToCartButtons.isEmpty()) {
                WebElement button = addToCartButtons.get(0);
                return button.isDisplayed() && button.isEnabled();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if "Notify Me When Available" button is displayed for a product.
     * 
     * @param productName Product name to check
     * @return true if Notify Me button is displayed
     */
    public boolean isNotifyMeButtonDisplayed(String productName) {
        try {
            WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]")
            ));
            
            List<WebElement> notifyButtons = productCard.findElements(NOTIFY_ME_BUTTON);
            return !notifyButtons.isEmpty() && notifyButtons.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if low stock warning is displayed for a product.
     * 
     * @param productName Product name to check
     * @return true if low stock warning is displayed
     */
    public boolean isLowStockWarningDisplayed(String productName) {
        try {
            WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]")
            ));
            
            List<WebElement> warnings = productCard.findElements(By.className("low-stock-warning"));
            return !warnings.isEmpty() && warnings.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}

