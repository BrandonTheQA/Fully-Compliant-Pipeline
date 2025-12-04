# Selenium E2E UI Tests

End-to-end Selenium WebDriver tests for the Fully Compliant Pipeline E-Commerce Platform.

## Overview

This project contains comprehensive end-to-end tests that verify the complete e-commerce workflow through the UI. The tests replicate the Postman integration test workflow:

1. Create User
2. Verify User creation
3. Create Product 1 (Laptop)
4. Create Product 2 (Mouse)
5. Create Product 3 (Keyboard)
6. Verify products appear in list
7. Add products to cart and create order
8. Verify order was created with correct details

## Technology Stack

- **Java 17**: Programming language
- **Selenium WebDriver 4.x**: Browser automation framework
- **WebDriverManager 5.x**: Automatic ChromeDriver management
- **JUnit 5**: Test framework
- **Maven**: Build tool
- **Chrome Headless**: Browser for running tests

## Project Structure

```
selenium/
├── pom.xml                          # Maven project configuration
├── run-selenium-tests.sh            # Local test execution script
├── README.md                        # This file
└── src/
    └── test/
        └── java/
            ├── E2EWorkflowTest.java # Main test class
            ├── config/
            │   └── TestConfig.java  # Test configuration and constants
            └── pages/               # Page Object Model classes
                ├── BasePage.java
                ├── HomePage.java
                ├── UserPage.java
                ├── ProductsPage.java
                └── OrdersPage.java
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+ 
- Chrome browser (automatically managed by WebDriverManager)
- Node.js and npm (for running UI service locally)
- Services running on ports 8081-8084 (user, product, order, ui)

## Running Tests Locally

### Option 1: Manual Two-Terminal Approach (Recommended)

**Terminal 1**: Start services:
```bash
./scripts/run-local-e2e.sh
```

**Terminal 2**: Wait for services to be ready, then run tests:
```bash
cd selenium
./run-selenium-tests.sh
```

The run script will check if services are running and provide helpful instructions if they're not.

### Option 2: Manual Maven Execution

If services are already running:

```bash
cd selenium
BASE_URL=http://localhost:8084 mvn clean test
```

### Option 3: Running Tests Against Azure Environments

To run tests against the deployed dev or QA environment, set `BASE_URL` to the corresponding App Service URL. For example:

```bash
cd selenium
BASE_URL="https://app-ecompoc-dev-ui.azurewebsites.net" mvn clean test
```

## Running in CI/CD

The tests are automatically executed in GitHub Actions after the `deploy-dev` job completes. See `.github/workflows/ci-cd.yml` for configuration details.

## Test Configuration

### Base URL

The test base URL can be configured via:
1. Environment variable: `BASE_URL`
2. System property: `-DBASE_URL=...`
3. Default: `http://localhost:8084`

### Browser Options

Tests run in headless Chrome mode with the following options:
- `--headless=new`: Modern headless mode
- `--disable-gpu`: Disable GPU hardware acceleration
- `--no-sandbox`: Disable sandboxing
- `--disable-dev-shm-usage`: Overcome limited resource problems
- `--window-size=1920,1080`: Standard window size

### Wait Times

- **Implicit Wait**: 10 seconds
- **Explicit Wait**: 20 seconds

## Test Data

Test data constants are defined in `TestConfig.java`:

- **User**: John Doe, unique email with timestamp, SecurePassword123
- **Product 1**: Laptop, $999.99, Electronics, Qty: 10
- **Product 2**: Mouse, $29.99, Electronics, Qty: 50
- **Product 3**: Keyboard, $79.99, Electronics, Qty: 25
- **Expected Order Total**: $1,089.97

**⚠️ Security Note:** Test passwords (e.g., `SecurePassword123`) are intentionally weak and are **ONLY** for automated testing purposes. These should never be used in production or real-world scenarios. See [SECURITY.md](../SECURITY.md) for more information.

## Page Object Model

The tests use the Page Object Model pattern for maintainability:

- **BasePage**: Common functionality and navigation
- **HomePage**: Home page navigation and welcome message
- **UserPage**: User creation form and user info display
- **ProductsPage**: Product list and product creation form
- **OrdersPage**: Order creation, cart, and order details

## Screenshots on Failure

When a test fails, a screenshot is automatically captured to aid in debugging. In CI/CD, screenshots are uploaded as artifacts.

## Troubleshooting

### Tests Fail with "Connection Refused"

**Solution**: Ensure all services are running on the correct ports.

```bash
# Start services using the e2e script
./scripts/run-local-e2e.sh
```

### ChromeDriver Version Issues

**Solution**: WebDriverManager handles this automatically. If issues persist, update to the latest version:

```bash
cd selenium
mvn clean install -DskipTests
```

### Tests Timeout

**Solution**: Check that services are healthy and responding. Verify network connectivity to the base URL.

```bash
# Check UI service
curl http://localhost:8084

# Check API services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Port Already in Use

**Solution**: Kill any existing processes on the ports or use different ports:

```bash
# Kill processes on ports 8081-8084
for p in 8081 8082 8083 8084; do
  lsof -ti tcp:$p | xargs kill -9 || true
done
```

## Contributing

When adding new tests:
1. Follow the Page Object Model pattern
2. Use meaningful locators (prefer IDs over XPath)
3. Add explicit waits for dynamic content
4. Include appropriate assertions
5. Update this README if needed

## License

Part of the Fully Compliant Pipeline project.

