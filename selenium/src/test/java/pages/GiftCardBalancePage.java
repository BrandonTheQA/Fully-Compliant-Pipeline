package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object for Gift Card Balance Inquiry page
 */
public class GiftCardBalancePage extends BasePage {
    
    public GiftCardBalancePage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToBalancePage() {
        navigateTo("/gift-cards/balance");
    }
    
    public void enterGiftCardCode(String code) {
        WebElement codeInput = driver.findElement(By.id("gift-card-code"));
        codeInput.clear();
        codeInput.sendKeys(code);
    }
    
    public void clickCheckBalance() {
        WebElement checkButton = driver.findElement(By.className("check-balance-btn"));
        checkButton.click();
    }
    
    public boolean isBalanceDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("balance-results")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getBalance() {
        try {
            WebElement balanceElement = driver.findElement(By.className("balance-value"));
            return balanceElement.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getStatus() {
        try {
            WebElement statusElement = driver.findElement(By.className("status"));
            return statusElement.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isErrorDisplayed() {
        try {
            WebElement errorElement = driver.findElement(By.className("error-message"));
            return errorElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getErrorMessage() {
        try {
            WebElement errorElement = driver.findElement(By.className("error-message"));
            return errorElement.getText();
        } catch (Exception e) {
            return null;
        }
    }
}
