import config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class SCRUM14WishlistTest extends TestConfig {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl = TestConfig.BASE_URL;

    private HomePage homePage;
    private UserPage userPage;
    private ProductsPage productsPage;

    @BeforeEach
    public void setUp() {
        driver = TestConfig.createWebDriver();
        wait = TestConfig.createWebDriverWait(driver);
        
        homePage = new HomePage(driver);
        userPage = new UserPage(driver);
        productsPage = new ProductsPage(driver);
    }
    
    @AfterEach
    public void tearDown() {
        TestConfig.quitWebDriver(driver);
    }

    @Test
    public void testWishlistFlow() {
        // 1. Register/Login a user
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String username = "wishlist_user_" + uniqueId;
        String email = "wishlist_" + uniqueId + "@example.com";

        userPage.navigateToUserPage();
        userPage.fillUserForm(username, email, "password123");
        userPage.submitUserForm();
        
        // Verify user is created/logged in
        userPage.verifyUserInfoDisplayed();

        // 2. Create a product
        String productName = "Wishlist Product " + uniqueId;
        productsPage.navigateToProductsPage();
        productsPage.createProduct(productName, "Description", 100.0, 10, "Electronics");

        // 3. Add product to wishlist from Product List
        // Wait for product to appear and find the wishlist button
        // Note: The wishlist button is inside the product card
        WebElement productCard = driver.findElement(By.xpath("//div[contains(@class, 'product-card') and .//h3[text()='" + productName + "']]"));
        WebElement wishlistButton = productCard.findElement(By.className("wishlist-button"));
        wishlistButton.click();
        
        // Verify button state changed (active class or heart icon)
        wait.until(ExpectedConditions.attributeContains(wishlistButton, "class", "active"));

        // 4. Navigate to Wishlist Page
        driver.findElement(By.linkText("Wishlist")).click();
        wait.until(ExpectedConditions.urlContains("/wishlist"));

        // 5. Verify product is in wishlist
        WebElement wishlistCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));
        assertNotNull(wishlistCard, "Product should be in wishlist");

        // 6. Move to Cart
        WebElement moveToCartButton = wishlistCard.findElement(By.xpath(".//button[contains(text(), 'Move to Cart')]"));
        moveToCartButton.click();

        // 7. Verify product removed from wishlist
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));

        // 8. Verify product in Cart (check cart badge or navigate to orders page/home)
        // Simple check: cart badge should be visible
        WebElement cartBadge = driver.findElement(By.className("cart-badge"));
        assertTrue(cartBadge.isDisplayed(), "Cart badge should be displayed");
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item");
    }
}
