package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class WishlistPage extends BasePage {

    public WishlistPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToWishlist() {
        WebElement wishlistLink = driver.findElement(By.linkText("Wishlist"));
        wishlistLink.click();
        wait.until(ExpectedConditions.urlContains("/wishlist"));
    }

    public boolean isProductInWishlist(String productName) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void moveProductToCart(String productName) {
        WebElement wishlistCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));
        WebElement moveToCartButton = wishlistCard.findElement(By.xpath(".//button[contains(text(), 'Move to Cart')]"));
        moveToCartButton.click();
        // Wait a bit for the async operation to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void verifyProductRemovedFromWishlist(String productName) {
        // Wait for the element to become invisible, with a longer timeout
        // The UI may take time to update after the async removeFromWishlist call
        // Try multiple approaches: wait for invisibility, check wishlist count, or refresh
        try {
            // First, wait a bit for the async operation
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));
        } catch (org.openqa.selenium.TimeoutException e) {
            // If still visible, try refreshing the page and checking again
            driver.navigate().refresh();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            // After refresh, check if wishlist is empty or product is not there
            try {
                // Check if wishlist shows empty message
                try {
                    WebElement emptyMessage = driver.findElement(By.xpath("//div[contains(@class, 'wishlist-page') and contains(@class, 'empty')]"));
                    if (emptyMessage != null && emptyMessage.getText().contains("empty")) {
                        // Wishlist is empty, product was removed
                        return;
                    }
                } catch (org.openqa.selenium.NoSuchElementException nse2) {
                    // Empty message not found, continue checking
                }
                
                // Check if wishlist is empty by checking for cards
                java.util.List<org.openqa.selenium.WebElement> wishlistCards = driver.findElements(
                    By.xpath("//div[@class='wishlist-card']"));
                if (wishlistCards.isEmpty()) {
                    // Wishlist is empty, product was removed
                    return;
                }
                
                // Check if the specific product is still there
                java.util.List<org.openqa.selenium.WebElement> productCards = driver.findElements(
                    By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]"));
                if (productCards.isEmpty()) {
                    // Product not found, it was removed
                    return;
                }
                
                // If we get here, product is still visible - this is a failure
                throw new org.openqa.selenium.TimeoutException(
                    "Product '" + productName + "' is still in wishlist after move to cart");
            } catch (org.openqa.selenium.NoSuchElementException nse) {
                // No wishlist cards found, product was removed
                return;
            }
        }
    }
}
