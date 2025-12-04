package config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

/**
 * Test configuration class for Selenium E2E tests.
 * Provides WebDriver setup with headless Chrome and test data constants.
 */
public class TestConfig {
    
    // Base URL - defaults to localhost, can be overridden with BASE_URL environment variable
    public static final String BASE_URL = System.getProperty("BASE_URL", 
        System.getenv().getOrDefault("BASE_URL", "http://localhost:8084"));
    
    // Implicit and explicit wait timeouts
    public static final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);
    public static final Duration EXPLICIT_WAIT = Duration.ofSeconds(20);
    
    // Test data constants matching Postman collection variables
    public static class TestData {
        // User data
        public static final String USER_NAME = "John Doe";
        // NOTE: Test passwords are intentionally weak for automated testing purposes only.
        // These should NEVER be used in production or real-world scenarios.
        public static final String USER_PASSWORD = "SecurePassword123";
        
        // Product 1 (Laptop)
        public static final String PRODUCT1_NAME = "Laptop";
        public static final String PRODUCT1_DESCRIPTION = "High-performance laptop";
        public static final double PRODUCT1_PRICE = 999.99;
        public static final int PRODUCT1_QUANTITY = 10;
        public static final String PRODUCT1_CATEGORY = "Electronics";
        public static final int PRODUCT1_ORDER_QUANTITY = 1;
        
        // Product 2 (Mouse)
        public static final String PRODUCT2_NAME = "Mouse";
        public static final String PRODUCT2_DESCRIPTION = "Wireless mouse";
        public static final double PRODUCT2_PRICE = 29.99;
        public static final int PRODUCT2_QUANTITY = 50;
        public static final String PRODUCT2_CATEGORY = "Electronics";
        public static final int PRODUCT2_ORDER_QUANTITY = 2;
        
        // Product 3 (Keyboard)
        public static final String PRODUCT3_NAME = "Keyboard";
        public static final String PRODUCT3_DESCRIPTION = "Mechanical keyboard";
        public static final double PRODUCT3_PRICE = 79.99;
        public static final int PRODUCT3_QUANTITY = 25;
        public static final String PRODUCT3_CATEGORY = "Electronics";
        public static final int PRODUCT3_ORDER_QUANTITY = 1;
        
        // Expected total for order
        public static final double EXPECTED_ORDER_TOTAL = 
            (PRODUCT1_PRICE * PRODUCT1_ORDER_QUANTITY) +
            (PRODUCT2_PRICE * PRODUCT2_ORDER_QUANTITY) +
            (PRODUCT3_PRICE * PRODUCT3_ORDER_QUANTITY);
    }
    
    /**
     * Creates and configures a WebDriver instance with headless Chrome.
     * Uses WebDriverManager to automatically manage ChromeDriver versions.
     * 
     * @return Configured WebDriver instance
     */
    public static WebDriver createWebDriver() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");  // Modern headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().window().maximize();
        
        return driver;
    }
    
    /**
     * Creates a WebDriverWait instance with configured timeout.
     * 
     * @param driver WebDriver instance
     * @return Configured WebDriverWait instance
     */
    public static WebDriverWait createWebDriverWait(WebDriver driver) {
        return new WebDriverWait(driver, EXPLICIT_WAIT);
    }
    
    /**
     * Quits the WebDriver instance safely.
     * 
     * @param driver WebDriver instance to quit
     */
    public static void quitWebDriver(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}

