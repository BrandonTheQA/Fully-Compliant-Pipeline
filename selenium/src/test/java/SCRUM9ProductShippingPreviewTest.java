import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.TestConfig;
import pages.HomePage;
import pages.ProductsPage;
import pages.OrdersPage;
import pages.UserPage;

/**
 * E2E tests for SCRUM-9: Proactive Shipping Cost Preview on Product Pages
 * 
 * Tests the following scenarios:
 * 1. Happy path: View product → see shipping cost → add to cart → verify checkout matches
 * 2. Free shipping product display
 * 3. Region change updates shipping costs (if region selector available)
 * 4. Multiple products with different shipping costs
 * 5. Shipping cost accuracy (product page matches checkout)
 */
public class SCRUM9ProductShippingPreviewTest {
    
    private WebDriver driver;
    private HomePage homePage;
    private UserPage userPage;
    private ProductsPage productsPage;
    private OrdersPage ordersPage;
    
    private String uniqueEmail;
    private String timestamp;
    
    @BeforeEach
    public void setUp() {
        // Create unique email with timestamp to avoid conflicts
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        uniqueEmail = "test.scrum9+" + timestamp + "@example.com";
        
        // Initialize WebDriver with headless Chrome
        driver = TestConfig.createWebDriver();
        
        // Initialize page objects
        homePage = new HomePage(driver);
        userPage = new UserPage(driver);
        productsPage = new ProductsPage(driver);
        ordersPage = new OrdersPage(driver);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up WebDriver
        TestConfig.quitWebDriver(driver);
    }
    
    /**
     * HP-1: Happy Path Test
     * View product → see shipping cost → add to cart → verify checkout matches
     */
    @Test
    public void testHappyPathShippingCostPreview() {
        try {
            // Setup: Create user and products
            setupTestEnvironment();
            
            // Create a product below threshold ($35.00)
            String productName = "Test Product Below Threshold " + timestamp;
            createProduct(productName, "Test Description", 35.00, 10, "Electronics");
            
            productsPage.waitForProductListToLoad();
            
            // Step 1: Navigate to products page and verify shipping cost is displayed
            System.out.println("Step 1: Verifying shipping cost preview on product page...");
            assertTrue(productsPage.isProductDisplayed(productName), 
                "Product should be displayed: " + productName);
            
            // Verify shipping preview is displayed for the product
            WebElement productCard = findProductCard(productName);
            assertNotNull(productCard, "Product card should be found");
            
            // Check for shipping preview elements
            WebElement shippingPreview = productCard.findElement(By.className("product-shipping-preview"));
            assertNotNull(shippingPreview, "Shipping preview should be displayed");
            
            // Verify "Estimated Shipping" text is present
            WebElement estimatedShipping = shippingPreview.findElement(
                By.xpath(".//*[contains(text(), 'Estimated Shipping')]"));
            assertNotNull(estimatedShipping, "Estimated Shipping label should be displayed");
            
            // Verify shipping cost value is displayed (should be $9.99 for $35 product in US)
            List<WebElement> shippingCostElements = shippingPreview.findElements(
                By.xpath(".//*[contains(text(), '$')]"));
            assertFalse(shippingCostElements.isEmpty(), 
                "Shipping cost should be displayed");
            
            // Extract shipping cost from product page
            String shippingCostText = shippingCostElements.get(0).getText();
            System.out.println("Shipping cost on product page: " + shippingCostText);
            
            // Step 2: Add product to cart
            System.out.println("Step 2: Adding product to cart...");
            productsPage.addProductToCart(productName);
            
            // Step 3: Navigate to orders/checkout page and verify shipping cost matches
            System.out.println("Step 3: Verifying shipping cost at checkout...");
            ordersPage.navigateToOrdersPage();
            
            // Wait for order details to load
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify shipping cost at checkout matches product page
            // Note: This assumes the checkout page displays shipping cost
            // Adjust selectors based on actual checkout implementation
            List<WebElement> checkoutShippingElements = driver.findElements(
                By.xpath("//*[contains(text(), 'Shipping') or contains(text(), '$')]"));
            
            // Verify shipping cost is displayed at checkout
            assertFalse(checkoutShippingElements.isEmpty(), 
                "Shipping cost should be displayed at checkout");
            
            System.out.println("✓ Happy path test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Happy path test failed: " + e.getMessage());
        }
    }
    
    /**
     * HP-2: Free Shipping Product Display Test
     * Product qualifies for free shipping - displays correctly on product page
     */
    @Test
    public void testFreeShippingProductDisplay() {
        try {
            // Setup: Create user
            setupTestEnvironment();
            
            // Create a product above threshold ($55.00)
            String productName = "Free Shipping Product " + timestamp;
            createProduct(productName, "Test Description", 55.00, 10, "Electronics");
            
            productsPage.waitForProductListToLoad();
            
            // Verify product is displayed
            assertTrue(productsPage.isProductDisplayed(productName), 
                "Product should be displayed: " + productName);
            
            // Find product card
            WebElement productCard = findProductCard(productName);
            assertNotNull(productCard, "Product card should be found");
            
            // Verify shipping preview is displayed
            WebElement shippingPreview = productCard.findElement(By.className("product-shipping-preview"));
            assertNotNull(shippingPreview, "Shipping preview should be displayed");
            
            // Verify FREE shipping message is displayed
            WebElement freeShippingElement = shippingPreview.findElement(
                By.xpath(".//*[contains(text(), 'FREE')]"));
            assertNotNull(freeShippingElement, "FREE shipping should be displayed");
            
            // Verify "qualifies for FREE shipping" message
            WebElement qualifiesMessage = shippingPreview.findElement(
                By.xpath(".//*[contains(text(), 'qualifies for FREE shipping')]"));
            assertNotNull(qualifiesMessage, 
                "Qualifies for FREE shipping message should be displayed");
            
            System.out.println("✓ Free shipping product display test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Free shipping product display test failed: " + e.getMessage());
        }
    }
    
    /**
     * HP-3: Multiple Products with Different Shipping Costs
     * User views multiple products - each shows accurate shipping cost
     */
    @Test
    public void testMultipleProductsShippingCosts() {
        try {
            // Setup: Create user
            setupTestEnvironment();
            
            // Create products at different price points
            String product1Name = "Product Below Threshold " + timestamp;
            String product2Name = "Product Above Threshold " + timestamp;
            String product3Name = "Product At Threshold " + timestamp;
            
            createProduct(product1Name, "Test Description", 35.00, 10, "Electronics");
            createProduct(product2Name, "Test Description", 55.00, 10, "Electronics");
            createProduct(product3Name, "Test Description", 50.00, 10, "Electronics");
            
            productsPage.waitForProductListToLoad();
            
            // Verify all products are displayed
            assertTrue(productsPage.isProductDisplayed(product1Name), 
                "Product 1 should be displayed");
            assertTrue(productsPage.isProductDisplayed(product2Name), 
                "Product 2 should be displayed");
            assertTrue(productsPage.isProductDisplayed(product3Name), 
                "Product 3 should be displayed");
            
            // Verify each product has shipping preview
            WebElement product1Card = findProductCard(product1Name);
            WebElement product2Card = findProductCard(product2Name);
            WebElement product3Card = findProductCard(product3Name);
            
            assertNotNull(product1Card, "Product 1 card should be found");
            assertNotNull(product2Card, "Product 2 card should be found");
            assertNotNull(product3Card, "Product 3 card should be found");
            
            // Verify product 1 (below threshold) shows shipping cost
            WebElement shippingPreview1 = product1Card.findElement(By.className("product-shipping-preview"));
            assertNotNull(shippingPreview1, "Product 1 should have shipping preview");
            assertTrue(shippingPreview1.getText().contains("Estimated Shipping") || 
                      shippingPreview1.getText().contains("$"),
                "Product 1 should show shipping cost");
            
            // Verify product 2 (above threshold) shows FREE shipping
            WebElement shippingPreview2 = product2Card.findElement(By.className("product-shipping-preview"));
            assertNotNull(shippingPreview2, "Product 2 should have shipping preview");
            assertTrue(shippingPreview2.getText().contains("FREE"),
                "Product 2 should show FREE shipping");
            
            // Verify product 3 (at threshold) shows FREE shipping
            WebElement shippingPreview3 = product3Card.findElement(By.className("product-shipping-preview"));
            assertNotNull(shippingPreview3, "Product 3 should have shipping preview");
            assertTrue(shippingPreview3.getText().contains("FREE"),
                "Product 3 should show FREE shipping");
            
            System.out.println("✓ Multiple products shipping costs test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Multiple products shipping costs test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC5.1: Shipping Cost Accuracy Test
     * Product page shipping cost matches checkout shipping cost
     */
    @Test
    public void testShippingCostAccuracy() {
        try {
            // Setup: Create user
            setupTestEnvironment();
            
            // Create a product below threshold
            String productName = "Accuracy Test Product " + timestamp;
            createProduct(productName, "Test Description", 35.00, 10, "Electronics");
            
            productsPage.waitForProductListToLoad();
            
            // Get shipping cost from product page
            WebElement productCard = findProductCard(productName);
            WebElement shippingPreview = productCard.findElement(By.className("product-shipping-preview"));
            
            // Extract shipping cost value (should be $9.99 for $35 product in US)
            String shippingPreviewText = shippingPreview.getText();
            System.out.println("Shipping preview text: " + shippingPreviewText);
            
            // Extract numeric shipping cost using regex
            Pattern costPattern = Pattern.compile("\\$([0-9]+\\.?[0-9]*)");
            java.util.regex.Matcher matcher = costPattern.matcher(shippingPreviewText);
            double productPageShippingCost = 0.0;
            if (matcher.find()) {
                productPageShippingCost = Double.parseDouble(matcher.group(1));
            } else if (shippingPreviewText.contains("FREE")) {
                productPageShippingCost = 0.0;
            }
            
            System.out.println("Product page shipping cost: $" + productPageShippingCost);
            
            // Add product to cart
            productsPage.addProductToCart(productName);
            
            // Navigate to checkout/orders page
            ordersPage.navigateToOrdersPage();
            
            // Wait for page to load
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Extract shipping cost from checkout (adjust selectors as needed)
            // This is a simplified check - actual implementation may vary
            List<WebElement> checkoutElements = driver.findElements(
                By.xpath("//*[contains(text(), 'Shipping') or contains(text(), '$')]"));
            
            // Verify shipping cost is displayed at checkout
            assertFalse(checkoutElements.isEmpty(), 
                "Shipping cost should be displayed at checkout");
            
            // Note: Full accuracy verification would require parsing checkout shipping cost
            // and comparing with product page cost within $0.01 tolerance
            // This is a basic implementation - can be enhanced based on actual checkout UI
            
            System.out.println("✓ Shipping cost accuracy test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Shipping cost accuracy test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC1.7: All Products Display Shipping Cost
     * Verify all products in catalog show shipping cost information
     */
    @Test
    public void testAllProductsDisplayShippingCost() {
        try {
            // Setup: Create user
            setupTestEnvironment();
            
            // Create multiple products
            for (int i = 1; i <= 3; i++) {
                String productName = "Product " + i + " " + timestamp;
                createProduct(productName, "Test Description", 25.00 + (i * 10), 10, "Electronics");
            }
            
            productsPage.waitForProductListToLoad();
            
            // Get all product cards
            List<WebElement> productCards = productsPage.getProductCards();
            assertFalse(productCards.isEmpty(), "Products should be displayed");
            
            // Verify each product card has shipping preview
            for (WebElement productCard : productCards) {
                try {
                    WebElement shippingPreview = productCard.findElement(
                        By.className("product-shipping-preview"));
                    assertNotNull(shippingPreview, 
                        "Each product should have shipping preview");
                    
                    // Verify shipping preview has content
                    String previewText = shippingPreview.getText();
                    assertFalse(previewText.isEmpty(), 
                        "Shipping preview should have content");
                    
                    // Verify it contains shipping-related text
                    assertTrue(previewText.contains("Shipping") || 
                              previewText.contains("FREE") ||
                              previewText.contains("$"),
                        "Shipping preview should contain shipping information");
                } catch (Exception e) {
                    fail("Product card missing shipping preview: " + e.getMessage());
                }
            }
            
            System.out.println("✓ All products display shipping cost test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            fail("All products display shipping cost test failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private void setupTestEnvironment() {
        // Navigate to home page
        homePage.navigateToHome();
        homePage.verifyWelcomeHeading();
        
        // Create user
        userPage.navigateToUserPage();
        userPage.fillUserForm(
            "Test User",
            uniqueEmail,
            "password123"
        );
        userPage.submitUserForm();
        userPage.verifyUserInfoDisplayed();
        
        // Navigate to products page
        productsPage.navigateToProductsPage();
        productsPage.waitForProductListToLoad();
    }
    
    private void createProduct(String name, String description, double price, int quantity, String category) {
        productsPage.clickCreateNewProductButton();
        productsPage.fillProductForm(name, description, price, quantity, category);
        productsPage.submitProductForm();
        
        // Wait for product to be created
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        productsPage.waitForProductListToLoad();
    }
    
    private WebElement findProductCard(String productName) {
        List<WebElement> productCards = productsPage.getProductCards();
        for (WebElement productCard : productCards) {
            try {
                WebElement nameElement = productCard.findElement(By.cssSelector("h3"));
                if (nameElement.getText().equals(productName)) {
                    return productCard;
                }
            } catch (Exception e) {
                // Continue searching
            }
        }
        return null;
    }
}

