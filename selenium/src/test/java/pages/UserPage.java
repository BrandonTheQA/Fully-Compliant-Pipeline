package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object for the user creation page.
 */
public class UserPage extends BasePage {
    
    // Locators
    private static final By USER_FORM_CONTAINER = By.className("user-form-container");
    private static final By USER_FORM = By.className("user-form");
    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By SUBMIT_BUTTON = By.xpath("//button[contains(text(), 'Create User')]");
    private static final By SUCCESS_MESSAGE = By.className("success-message");
    private static final By ERROR_MESSAGE = By.className("error-message");
    private static final By USER_INFO_SECTION = By.className("user-info");
    private static final By USER_NAME_DISPLAY = By.xpath("//div[contains(@class, 'user-info')]//p[contains(., 'Name:')]");
    private static final By USER_EMAIL_DISPLAY = By.xpath("//div[contains(@class, 'user-info')]//p[contains(., 'Email:')]");
    private static final By USER_ID_DISPLAY = By.xpath("//div[contains(@class, 'user-info')]//p[contains(., 'User ID:')]");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(), 'Logout')]");

    public UserPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the user page.
     */
    public void navigateToUserPage() {
        navigateTo("/user");
        // Wait for React to render - check for any content in root or common elements
        // Try multiple strategies to handle different rendering states
        try {
            // First, wait for root to have content
            WebDriverWait rootWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            rootWait.until(webDriver -> {
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) webDriver;
                try {
                    Object result = js.executeScript(
                        "var root = document.getElementById('root'); return root && root.innerHTML.trim().length > 0;"
                    );
                    return result != null && (Boolean) result;
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            // Continue anyway - try to find elements
        }
        
        // Wait for any of the expected elements to appear
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(USER_FORM_CONTAINER),
            ExpectedConditions.presenceOfElementLocated(NAME_INPUT),
            ExpectedConditions.presenceOfElementLocated(USER_INFO_SECTION),
            ExpectedConditions.presenceOfElementLocated(USER_FORM),
            ExpectedConditions.presenceOfElementLocated(By.tagName("form")),
            ExpectedConditions.presenceOfElementLocated(By.className("page-container")),
            ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(), 'Create User') or contains(text(), 'Current User')]"))
        ));
    }

    /**
     * Fills in the user creation form.
     * 
     * @param name User's name
     * @param email User's email
     * @param password User's password
     */
    public void fillUserForm(String name, String email, String password) {
        // Wait for form to be visible and inputs to be ready
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(NAME_INPUT));
        nameInput.clear();
        nameInput.sendKeys(name);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys(email);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    /**
     * Submits the user creation form.
     */
    public void submitUserForm() {
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON));
        submitButton.click();
        // Wait a moment for the form submission to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
     * Verifies user info is displayed after successful creation.
     * Waits for the form to disappear and user-info to become visible.
     */
    public void verifyUserInfoDisplayed() {
        // First wait for the form to disappear (indicating submission started)
        // Then wait for user-info to be visible (indicating user was created)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SUBMIT_BUTTON));
        wait.until(ExpectedConditions.visibilityOfElementLocated(USER_INFO_SECTION));
    }

    /**
     * Gets the displayed user name.
     * 
     * @return User name from info section
     */
    public String getUserNameDisplayed() {
        WebElement nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(USER_NAME_DISPLAY));
        return nameElement.getText();
    }

    /**
     * Gets the displayed user email.
     * 
     * @return User email from info section
     */
    public String getUserEmailDisplayed() {
        WebElement emailElement = wait.until(ExpectedConditions.presenceOfElementLocated(USER_EMAIL_DISPLAY));
        return emailElement.getText();
    }

    /**
     * Gets the displayed user ID.
     * 
     * @return User ID from info section
     */
    public String getUserIdDisplayed() {
        WebElement userIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(USER_ID_DISPLAY));
        return userIdElement.getText();
    }

    /**
     * Checks if logout button is displayed.
     * 
     * @return true if logout button is visible
     */
    public boolean isLogoutButtonVisible() {
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.presenceOfElementLocated(LOGOUT_BUTTON));
            return logoutBtn.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}

