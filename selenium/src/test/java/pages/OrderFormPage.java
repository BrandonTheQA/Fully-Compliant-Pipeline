package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page object for Order Form (checkout) page
 */
public class OrderFormPage extends BasePage {
    
    public OrderFormPage(WebDriver driver) {
        super(driver);
    }
    
    public void enterGiftCardCode(String code) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement codeInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("gift-card-code-input")));
        codeInput.clear();
        codeInput.sendKeys(code);
    }
    
    public void clickApplyGiftCard() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Find the Apply button near the gift card input
        WebElement applyButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[contains(@class, 'gift-card-section')]//button[contains(text(), 'Apply')]")));
        applyButton.click();
    }
    
    public boolean isGiftCardApplied(String code) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            // Wait for applied gift cards section to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("applied-gift-cards")));
            // Wait a bit for the UI to update
            Thread.sleep(1000);
            WebElement appliedCards = driver.findElement(By.className("applied-gift-cards"));
            // Check if the code appears in the applied cards section
            String appliedCardsText = appliedCards.getText();
            // The code might be displayed partially, so check for a substring match
            return appliedCardsText.contains(code) || 
                   appliedCardsText.contains(code.substring(0, Math.min(8, code.length())));
        } catch (Exception e) {
            return false;
        }
    }
    
    public void removeGiftCard(String code) {
        try {
            List<WebElement> removeButtons = driver.findElements(
                By.xpath("//button[contains(@class, 'btn-danger')]"));
            for (WebElement button : removeButtons) {
                if (button.isDisplayed()) {
                    button.click();
                    break;
                }
            }
        } catch (Exception e) {
            // Remove button might not be found
        }
    }
    
    public String getTotalAmount() {
        try {
            WebElement totalElement = driver.findElement(By.className("total-amount"));
            return totalElement.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isGiftCardDiscountDisplayed() {
        try {
            WebElement discountElement = driver.findElement(
                By.xpath("//div[contains(@class, 'summary-row-discount')]//span[contains(text(), 'Gift Card')]"));
            return discountElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
