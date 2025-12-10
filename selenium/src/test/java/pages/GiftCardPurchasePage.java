package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object for Gift Card Purchase page
 */
public class GiftCardPurchasePage extends BasePage {
    
    public GiftCardPurchasePage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToGiftCardPurchase() {
        navigateTo("/gift-cards/purchase");
    }
    
    public void selectFixedAmount(String amount) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement amountButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), '$" + amount + "')]")));
        amountButton.click();
    }
    
    public void enterCustomAmount(String amount) {
        WebElement customInput = driver.findElement(By.id("custom-amount"));
        customInput.clear();
        customInput.sendKeys(amount);
    }
    
    public void setQuantity(int quantity) {
        WebElement quantityInput = driver.findElement(By.className("quantity-input"));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
    }
    
    public void enterRecipientInfo(String email, String name) {
        if (email != null && !email.isEmpty()) {
            WebElement emailInput = driver.findElement(By.id("recipient-email"));
            emailInput.clear();
            emailInput.sendKeys(email);
        }
        if (name != null && !name.isEmpty()) {
            WebElement nameInput = driver.findElement(By.id("recipient-name"));
            nameInput.clear();
            nameInput.sendKeys(name);
        }
    }
    
    public void enterPersonalMessage(String message) {
        WebElement messageTextarea = driver.findElement(By.className("message-textarea"));
        messageTextarea.clear();
        messageTextarea.sendKeys(message);
    }
    
    public void submitPurchase() {
        WebElement purchaseButton = driver.findElement(By.className("purchase-btn"));
        purchaseButton.click();
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h2[contains(text(), 'Purchased Successfully')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getGiftCardCode() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement codeElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("gift-card-code")));
        return codeElement.getText();
    }
    
    public boolean isErrorDisplayed() {
        try {
            WebElement errorElement = driver.findElement(By.className("error-message"));
            return errorElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
