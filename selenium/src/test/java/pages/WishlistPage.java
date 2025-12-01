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
    }

    public void verifyProductRemovedFromWishlist(String productName) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[@class='wishlist-card' and .//h3[text()='" + productName + "']]")));
    }
}
