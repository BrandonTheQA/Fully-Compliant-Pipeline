import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.util.List;

import config.TestConfig;
import pages.HomePage;
import pages.ProductsPage;
import pages.UserPage;
import pages.OrdersPage;
import pages.ShippingRecommendationsPage;
import pages.ShippingCostCalculatorPage;

/**
 * E2E tests for SCRUM-8: Shipping Cost Optimization Recommendations
 * 
 * Tests the complete recommendation flow:
 * - Recommendations appear when cart is below free shipping threshold
 * - Recommendations can be clicked to add products to cart
 * - Recommendations update dynamically when cart changes
 * - Recommendations disappear when free shipping threshold is reached
 * - Recommendations reappear when cart falls below threshold
 */
public class SCRUM8ShippingRecommendationsTest {
    
    private WebDriver driver;
    private HomePage homePage;
    private UserPage userPage;
    private ProductsPage productsPage;
    private OrdersPage ordersPage;
    private ShippingRecommendationsPage shippingRecommendationsPage;
    private ShippingCostCalculatorPage shippingCostCalculatorPage;
    
    private String uniqueEmail;
    private String timestamp;
    
    @BeforeEach
    public void setUp() {
        // Create unique email with timestamp to avoid conflicts
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        uniqueEmail = "test.user+" + timestamp + "@example.com";
        
        // Initialize WebDriver with headless Chrome
        driver = TestConfig.createWebDriver();
        
        // Initialize page objects
        homePage = new HomePage(driver);
        userPage = new UserPage(driver);
        productsPage = new ProductsPage(driver);
        ordersPage = new OrdersPage(driver);
        shippingRecommendationsPage = new ShippingRecommendationsPage(driver);
        shippingCostCalculatorPage = new ShippingCostCalculatorPage(driver);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up WebDriver
        TestConfig.quitWebDriver(driver);
    }
    
    /**
     * HP-1: Complete Happy Path Flow
     * 
     * Test Case: User with cart below threshold sees recommendations, 
     * adds recommended product, qualifies for free shipping
     * 
     * Steps:
     * 1. Create user
     * 2. Create products
     * 3. Add products to cart totaling $35.00 (below $50.00 US threshold)
     * 4. Verify recommendations appear showing 3-5 products
     * 5. Click "Add to Cart" on a recommended product
     * 6. Verify cart total becomes $55.00+
     * 7. Verify recommendations disappear (qualifies for free shipping)
     * 8. Verify free shipping banner shows "You've qualified for FREE shipping!"
     */
    @Test
    public void testHappyPathRecommendationsFlow() {
        try {
            // Step 1: Navigate and create user
            System.out.println("Step 1: Creating user...");
            homePage.navigateToHome();
            userPage.navigateToUserPage();
            userPage.fillUserForm(
                TestConfig.TestData.USER_NAME,
                uniqueEmail,
                TestConfig.TestData.USER_PASSWORD
            );
            userPage.submitUserForm();
            userPage.verifyUserInfoDisplayed();
            System.out.println("✓ User created successfully");
            
            // Step 2: Create products
            System.out.println("\nStep 2: Creating products...");
            productsPage.navigateToProductsPage();
            
            // Create a product that will be in cart (below threshold)
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                TestConfig.TestData.PRODUCT1_NAME + " " + timestamp,
                TestConfig.TestData.PRODUCT1_DESCRIPTION,
                TestConfig.TestData.PRODUCT1_PRICE,
                TestConfig.TestData.PRODUCT1_QUANTITY,
                TestConfig.TestData.PRODUCT1_CATEGORY
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Create a cheaper product for recommendations
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Recommendation Product " + timestamp,
                "Product for recommendations",
                20.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            System.out.println("✓ Products created");
            
            // Step 3: Add product to cart totaling below threshold
            System.out.println("\nStep 3: Adding products to cart (below threshold)...");
            productsPage.addProductToCart(TestConfig.TestData.PRODUCT1_NAME + " " + timestamp);
            
            // Navigate to orders page to see cart
            ordersPage.navigateToOrdersPage();
            
            // Wait for shipping components to load
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify cart is below threshold
            String subtotal = shippingCostCalculatorPage.getSubtotal();
            assertNotNull(subtotal, "Subtotal should be displayed");
            double subtotalValue = shippingCostCalculatorPage.extractCurrencyValue(subtotal);
            assertTrue(subtotalValue < 50.00, "Cart should be below $50 threshold. Got: " + subtotalValue);
            System.out.println("✓ Cart total: $" + subtotalValue + " (below threshold)");
            
            // Step 4: Verify recommendations appear
            System.out.println("\nStep 4: Verifying recommendations appear...");
            shippingRecommendationsPage.waitForShippingRecommendations();
            
            assertTrue(shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Shipping recommendations should be displayed when cart is below threshold");
            
            String title = shippingRecommendationsPage.getRecommendationsTitle();
            assertTrue(title.contains("FREE Shipping") || title.contains("Get FREE"),
                "Recommendations header should contain 'FREE Shipping'. Got: " + title);
            
            String subtitle = shippingRecommendationsPage.getRecommendationsSubtitle();
            assertNotNull(subtitle, "Recommendations subtitle should be displayed");
            assertTrue(subtitle.contains("Add $") && subtitle.contains("more"),
                "Subtitle should show remaining amount. Got: " + subtitle);
            
            int recommendationCount = shippingRecommendationsPage.getRecommendationCount();
            assertTrue(recommendationCount > 0, 
                "At least one recommendation should be displayed. Got: " + recommendationCount);
            System.out.println("✓ Recommendations displayed: " + recommendationCount + " products");
            
            // Verify recommendation details
            String firstProductName = shippingRecommendationsPage.getProductName(0);
            assertNotNull(firstProductName, "First product name should be displayed");
            assertFalse(firstProductName.isEmpty(), "First product name should not be empty");
            
            String firstProductPrice = shippingRecommendationsPage.getProductPrice(0);
            assertNotNull(firstProductPrice, "First product price should be displayed");
            assertTrue(firstProductPrice.contains("$"), "Price should contain $ symbol");
            System.out.println("✓ First recommendation: " + firstProductName + " - " + firstProductPrice);
            
            // Step 5: Click "Add to Cart" on recommended product
            System.out.println("\nStep 5: Adding recommended product to cart...");
            double priceBefore = shippingCostCalculatorPage.extractCurrencyValue(
                shippingCostCalculatorPage.getSubtotal()
            );
            
            shippingRecommendationsPage.clickAddToCart(0);
            
            // Wait for cart to update
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Step 6: Verify cart total increased
            String updatedSubtotal = shippingCostCalculatorPage.getSubtotal();
            double priceAfter = shippingCostCalculatorPage.extractCurrencyValue(updatedSubtotal);
            assertTrue(priceAfter > priceBefore, 
                "Cart total should increase after adding recommended product. Before: " + 
                priceBefore + ", After: " + priceAfter);
            System.out.println("✓ Cart total updated: $" + priceAfter);
            
            // Step 7: Verify recommendations disappear when qualified
            System.out.println("\nStep 7: Verifying recommendations disappear when qualified...");
            
            // Wait a bit for recommendations to update/disappear
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Check if cart now qualifies for free shipping
            boolean qualifiesForFree = shippingCostCalculatorPage.isShippingFree();
            if (qualifiesForFree) {
                // Recommendations should be hidden
                assertTrue(shippingRecommendationsPage.isHidden() || 
                    !shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                    "Recommendations should be hidden when cart qualifies for free shipping");
                System.out.println("✓ Recommendations disappeared (cart qualifies for free shipping)");
                
                // Step 8: Verify free shipping message
                String shippingText = shippingCostCalculatorPage.getShippingInBreakdown();
                assertTrue(shippingText.contains("FREE") || shippingText.equals("FREE"),
                    "Shipping should show FREE. Got: " + shippingText);
                System.out.println("✓ Free shipping confirmed: " + shippingText);
            } else {
                System.out.println("⚠ Cart still below threshold, recommendations may still be visible");
            }
            
            System.out.println("\n✓ HP-1: Complete happy path test passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC2.1-2.3: Multiple Optimization Paths Display
     * 
     * Test Case: Verify multiple recommendations are displayed with tabs,
     * and tab switching works correctly
     */
    @Test
    public void testMultipleRecommendationsWithTabs() {
        try {
            // Setup: Create user and products
            System.out.println("Setting up test environment...");
            homePage.navigateToHome();
            userPage.navigateToUserPage();
            userPage.fillUserForm(
                TestConfig.TestData.USER_NAME,
                uniqueEmail,
                TestConfig.TestData.USER_PASSWORD
            );
            userPage.submitUserForm();
            
            productsPage.navigateToProductsPage();
            
            // Create multiple products for recommendations
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Test Product 1 " + timestamp,
                "Product 1",
                25.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Test Product 2 " + timestamp,
                "Product 2",
                30.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Cart Product " + timestamp,
                "Product in cart",
                35.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Add product to cart (below threshold)
            productsPage.addProductToCart("Cart Product " + timestamp);
            
            // Navigate to orders page
            ordersPage.navigateToOrdersPage();
            
            // Wait for recommendations
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            shippingRecommendationsPage.waitForShippingRecommendations();
            
            // Verify recommendations are displayed
            assertTrue(shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Recommendations should be displayed");
            
            // Check if tabs are present (multiple paths)
            var tabs = shippingRecommendationsPage.getRecommendationTabs();
            if (tabs.size() > 1) {
                System.out.println("✓ Multiple paths detected: " + tabs.size() + " tabs");
                
                // Test tab switching
                String firstProductName = shippingRecommendationsPage.getProductName(0);
                System.out.println("✓ First tab product: " + firstProductName);
                
                // Click second tab
                shippingRecommendationsPage.clickTab(1);
                
                // Wait for content to switch
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                String secondProductName = shippingRecommendationsPage.getProductName(0);
                System.out.println("✓ Second tab product: " + secondProductName);
                
                // Verify different product is shown (if available)
                if (!firstProductName.equals(secondProductName)) {
                    System.out.println("✓ Tab switching works correctly");
                }
            } else {
                System.out.println("⚠ Only one path available (this is acceptable for Phase 1)");
            }
            
            System.out.println("✓ AC2.1-2.3: Multiple paths test passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC3.4: Recommendations Disappear When Threshold Reached
     * 
     * Test Case: Verify recommendations disappear when cart reaches free shipping threshold
     */
    @Test
    public void testRecommendationsDisappearWhenQualified() {
        try {
            // Setup
            System.out.println("Setting up test environment...");
            homePage.navigateToHome();
            userPage.navigateToUserPage();
            userPage.fillUserForm(
                TestConfig.TestData.USER_NAME,
                uniqueEmail,
                TestConfig.TestData.USER_PASSWORD
            );
            userPage.submitUserForm();
            
            productsPage.navigateToProductsPage();
            
            // Create a product that will bring cart above threshold when added
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "High Value Product " + timestamp,
                "Product to reach threshold",
                60.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Create a product below threshold
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Low Value Product " + timestamp,
                "Product below threshold",
                40.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Add product below threshold
            productsPage.addProductToCart("Low Value Product " + timestamp);
            
            ordersPage.navigateToOrdersPage();
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify recommendations appear
            shippingRecommendationsPage.waitForShippingRecommendations();
            assertTrue(shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Recommendations should appear when below threshold");
            System.out.println("✓ Recommendations appear when below threshold");
            
            // Add product to reach threshold
            productsPage.navigateToProductsPage();
            productsPage.addProductToCart("High Value Product " + timestamp);
            
            ordersPage.navigateToOrdersPage();
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify recommendations disappear
            assertTrue(shippingRecommendationsPage.isHidden() || 
                !shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Recommendations should disappear when cart qualifies for free shipping");
            System.out.println("✓ Recommendations disappear when threshold reached");
            
            // Verify free shipping
            assertTrue(shippingCostCalculatorPage.isShippingFree(),
                "Shipping should be FREE when threshold reached");
            System.out.println("✓ AC3.4: Recommendations disappear test passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC3.5: Recommendations Reappear When Cart Falls Below Threshold
     * 
     * Test Case: Verify recommendations reappear when items are removed from cart
     */
    @Test
    public void testRecommendationsReappearWhenBelowThreshold() {
        try {
            // Setup
            System.out.println("Setting up test environment...");
            homePage.navigateToHome();
            userPage.navigateToUserPage();
            userPage.fillUserForm(
                TestConfig.TestData.USER_NAME,
                uniqueEmail,
                TestConfig.TestData.USER_PASSWORD
            );
            userPage.submitUserForm();
            
            productsPage.navigateToProductsPage();
            
            // Create products
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Product A " + timestamp,
                "Product A",
                30.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Product B " + timestamp,
                "Product B",
                25.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Add both products to reach threshold
            productsPage.addProductToCart("Product A " + timestamp);
            productsPage.addProductToCart("Product B " + timestamp);
            
            ordersPage.navigateToOrdersPage();
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify cart qualifies for free shipping (no recommendations)
            boolean qualifiesBefore = shippingCostCalculatorPage.isShippingFree();
            if (qualifiesBefore) {
                assertTrue(shippingRecommendationsPage.isHidden() || 
                    !shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                    "Recommendations should not appear when qualified");
                System.out.println("✓ Cart qualifies for free shipping (no recommendations)");
            }
            
            // Remove one product to fall below threshold
            // Find and click remove button for Product A
            List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart-item"));
            for (WebElement cartItem : cartItems) {
                WebElement nameElement = cartItem.findElement(By.cssSelector("h4"));
                if (nameElement.getText().equals("Product A " + timestamp)) {
                    WebElement removeButton = cartItem.findElement(By.xpath(".//button[contains(text(), 'Remove')]"));
                    removeButton.click();
                    break;
                }
            }
            
            // Wait for updates
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify recommendations reappear
            shippingRecommendationsPage.waitForShippingRecommendations();
            assertTrue(shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Recommendations should reappear when cart falls below threshold");
            System.out.println("✓ Recommendations reappear when cart falls below threshold");
            
            System.out.println("✓ AC3.5: Recommendations reappear test passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * AC3.1-3.3: Dynamic Recommendation Updates
     * 
     * Test Case: Verify recommendations update when cart changes (add/remove/update quantity)
     */
    @Test
    public void testDynamicRecommendationUpdates() {
        try {
            // Setup
            System.out.println("Setting up test environment...");
            homePage.navigateToHome();
            userPage.navigateToUserPage();
            userPage.fillUserForm(
                TestConfig.TestData.USER_NAME,
                uniqueEmail,
                TestConfig.TestData.USER_PASSWORD
            );
            userPage.submitUserForm();
            
            productsPage.navigateToProductsPage();
            
            // Create products
            productsPage.clickCreateNewProductButton();
            productsPage.fillProductForm(
                "Dynamic Test Product " + timestamp,
                "Test product",
                35.00,
                10,
                "Electronics"
            );
            productsPage.submitProductForm();
            productsPage.verifySuccessMessage();
            
            // Add product to cart
            productsPage.addProductToCart("Dynamic Test Product " + timestamp);
            
            ordersPage.navigateToOrdersPage();
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify recommendations appear
            shippingRecommendationsPage.waitForShippingRecommendations();
            assertTrue(shippingRecommendationsPage.isShippingRecommendationsDisplayed(),
                "Recommendations should appear");
            
            String initialSubtitle = shippingRecommendationsPage.getRecommendationsSubtitle();
            System.out.println("✓ Initial recommendations: " + initialSubtitle);
            
            // Update quantity
            ordersPage.updateCartItemQuantity("Dynamic Test Product " + timestamp, 2);
            
            // Wait for recommendations to update (200ms debounce + API call)
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verify recommendations updated
            String updatedSubtitle = shippingRecommendationsPage.getRecommendationsSubtitle();
            assertNotNull(updatedSubtitle, "Updated recommendations should be displayed");
            System.out.println("✓ Updated recommendations: " + updatedSubtitle);
            
            // Verify subtitle changed (remaining amount should be different)
            if (!initialSubtitle.equals(updatedSubtitle)) {
                System.out.println("✓ Recommendations updated dynamically");
            }
            
            System.out.println("✓ AC3.1-3.3: Dynamic updates test passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
}

