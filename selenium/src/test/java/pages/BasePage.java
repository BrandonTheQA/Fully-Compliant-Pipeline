package pages;

import config.TestConfig;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * Base page object class providing common functionality for all page objects.
 */
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = TestConfig.createWebDriverWait(driver);
        this.baseUrl = TestConfig.BASE_URL;
        // Ensure baseUrl ends with / if it doesn't already
        if (this.baseUrl != null && !this.baseUrl.endsWith("/")) {
            this.baseUrl = this.baseUrl + "/";
        }
    }

    /**
     * Navigates to the page URL.
     * 
     * @param url relative or absolute URL to navigate to
     */
    protected void navigateTo(String url) {
        String fullUrl;
        if (url.startsWith("http")) {
            fullUrl = url;
        } else {
            // Remove leading / from baseUrl if present, and ensure url starts with /
            String cleanBaseUrl = baseUrl != null ? baseUrl.replaceAll("/$", "") : "http://localhost:8084";
            String normalizedUrl = url.startsWith("/") ? url : "/" + url;
            fullUrl = cleanBaseUrl + normalizedUrl;
        }
        driver.get(fullUrl);
        waitForPageLoad();
    }
    
    /**
     * Waits for the page to be fully loaded, including React rendering.
     */
    protected void waitForPageLoad() {
        // Wait for document ready state
        try {
            WebDriverWait pageLoadWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            pageLoadWait.until(webDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                try {
                    return js.executeScript("return document.readyState").equals("complete");
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            // Document might not be ready, continue anyway
        }
        
        // Wait longer for Vite dev server to compile and serve JavaScript
        // Vite can take significant time on first load, especially in headless mode
        try {
            Thread.sleep(5000); // Increased wait for Vite compilation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Wait for React root element to exist and have content
        // Use a longer timeout for Vite dev server compilation
        try {
            WebDriverWait reactWait = new WebDriverWait(driver, Duration.ofSeconds(40));
            reactWait.until(webDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                try {
                    // Check if root exists
                    Object rootExists = js.executeScript("return document.getElementById('root') !== null");
                    if (rootExists == null || !(Boolean) rootExists) {
                        return false;
                    }
                    // Check if root has content (React has rendered)
                    // Check both children and innerHTML to catch different rendering states
                    Object hasContent = js.executeScript(
                        "var root = document.getElementById('root'); " +
                        "return root && (root.children.length > 0 || root.innerHTML.trim().length > 100);"
                    );
                    boolean rendered = hasContent != null && (Boolean) hasContent;
                    
                    // If not rendered yet, check for script loading errors
                    if (!rendered) {
                        // Check if scripts are loaded
                        Object scriptsLoaded = js.executeScript(
                            "return document.querySelectorAll('script[src*=\"main.tsx\"], script[src*=\"@vite\"]').length > 0;"
                        );
                        if (scriptsLoaded == null || !(Boolean) scriptsLoaded) {
                            return false; // Scripts not loaded yet
                        }
                    }
                    
                    return rendered;
                } catch (Exception e) {
                    // JavaScript execution error - might mean scripts aren't loaded
                    return false;
                }
            });
        } catch (Exception e) {
            // React might not be ready yet, log and continue - page-specific waits will handle it
            System.err.println("Warning: React root content check timed out after 40 seconds");
            // Print console logs for debugging
            try {
                org.openqa.selenium.logging.LogEntries logs = driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
                for (org.openqa.selenium.logging.LogEntry entry : logs) {
                    if (entry.getLevel().toString().equals("SEVERE")) {
                        System.err.println("Browser console error: " + entry.getMessage());
                    }
                }
            } catch (Exception logException) {
                // Ignore log errors
            }
        }
        
        // Additional delay to ensure React has finished initial rendering
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Takes a screenshot after a test step if screenshots are enabled.
     * Screenshots are saved to selenium/screenshots/ directory with descriptive filenames.
     * 
     * @param stepName Descriptive name of the step (e.g., "navigate-home", "create-user")
     * @param stepNumber Step number for ordering (e.g., 1, 2, 3)
     */
    public void takeScreenshot(String stepName, int stepNumber) {
        // Check if screenshots are enabled
        if (!TestConfig.isScreenshotsEnabled()) {
            return;
        }
        
        try {
            // Ensure driver supports screenshots
            if (!(driver instanceof TakesScreenshot)) {
                System.out.println("Warning: WebDriver does not support screenshots");
                return;
            }
            
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            
            // Create screenshots directory if it doesn't exist
            File screenshotDir = new File(TestConfig.SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                boolean created = screenshotDir.mkdirs();
                if (!created) {
                    System.err.println("Warning: Could not create screenshots directory: " + TestConfig.SCREENSHOT_DIR);
                    return;
                }
            }
            
            // Sanitize step name for filename (remove special characters, replace spaces with hyphens)
            String sanitizedStepName = stepName
                .toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
            
            // Generate filename: step-{number:02d}-{stepName}.png
            String filename = String.format("step-%02d-%s.png", stepNumber, sanitizedStepName);
            File screenshotFile = new File(screenshotDir, filename);
            
            // Take screenshot
            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            
            // Save to file
            try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                fos.write(screenshotBytes);
            }
            
            // Log screenshot path
            System.out.println("Screenshot saved: " + screenshotFile.getAbsolutePath());
            
        } catch (IOException e) {
            // Don't fail the test if screenshot fails
            System.err.println("Warning: Failed to take screenshot for step " + stepNumber + " (" + stepName + "): " + e.getMessage());
        } catch (Exception e) {
            // Don't fail the test if screenshot fails
            System.err.println("Warning: Unexpected error taking screenshot for step " + stepNumber + " (" + stepName + "): " + e.getMessage());
        }
    }
}

