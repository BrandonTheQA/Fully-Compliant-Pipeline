# SCRUM-25: Price Drop Alerts - Architectural Analysis & Implementation Design

## Story Summary
**Title:** Price Drop Alerts to Re-engage Price-Sensitive Customers and Increase Conversions  
**Priority:** High  
**Story Points:** 13

**User Story:**
> As a price-sensitive customer who is interested in a product but waiting for a better price  
> I want to receive automated email notifications when products I'm watching drop in price  
> So that I can purchase at the best price and the business can convert price-sensitive customers who might otherwise abandon or wait indefinitely

---

## Phase 1: Impact Analysis

### Affected Domain Modules

1. **New Module: `pricealert`** (to be created)
   - **Purpose:** Core domain for price alert functionality
   - **Components:**
     - `model/`: PriceAlert, PriceHistory entities
     - `repository/`: PriceAlertRepository, PriceHistoryRepository
     - `service/`: PriceAlertService, PriceAlertEmailService, PriceDropDetectionService
     - `controller/`: PriceAlertController
     - `dto/`: Request/Response DTOs
     - `scheduler/`: PriceDropDetectionScheduler

2. **Product Module** (modification)
   - **Impact:** Add price change tracking hook in `ProductService.createOrUpdateProduct()`
   - **Change Type:** Non-breaking (side-by-side implementation via feature toggle)
   - **Files:**
     - `product/service/ProductService.java` - Add price change detection hook

3. **User Module** (integration)
   - **Impact:** Use existing user authentication/identification for logged-in users
   - **Change Type:** No changes required (read-only integration)

4. **Email Infrastructure** (reuse pattern)
   - **Impact:** Follow existing email service pattern (AbandonedCartEmailService, OrderEmailService)
   - **Change Type:** New service following established pattern

### Database Schema Changes

**New Tables:**
1. `price_alerts` table:
   - `alert_id` (VARCHAR(255), PK)
   - `product_id` (VARCHAR(255), FK to products)
   - `user_email` (NVARCHAR(255), NOT NULL)
   - `user_id` (VARCHAR(255), nullable, FK to users)
   - `target_price` (DECIMAL(10,2), nullable)
   - `current_price` (DECIMAL(10,2), NOT NULL) - price when alert created
   - `notification_frequency` (NVARCHAR(50)) - IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST
   - `status` (NVARCHAR(50)) - ACTIVE, TRIGGERED, EXPIRED, CANCELLED
   - `created_at` (DATETIME2, NOT NULL)
   - `last_triggered_at` (DATETIME2, nullable)
   - `updated_at` (DATETIME2, NOT NULL)

2. `price_history` table:
   - `price_history_id` (VARCHAR(255), PK)
   - `product_id` (VARCHAR(255), FK to products)
   - `price` (DECIMAL(10,2), NOT NULL)
   - `previous_price` (DECIMAL(10,2), nullable)
   - `change_type` (NVARCHAR(50)) - INCREASE, DECREASE, NO_CHANGE
   - `change_percentage` (DECIMAL(5,2), nullable)
   - `changed_at` (DATETIME2, NOT NULL)

**Indexes:**
- `idx_price_alerts_product_id` on `price_alerts(product_id)`
- `idx_price_alerts_user_email` on `price_alerts(user_email)`
- `idx_price_alerts_status` on `price_alerts(status)`
- `idx_price_alerts_created_at` on `price_alerts(created_at)`
- `idx_price_history_product_id` on `price_history(product_id)`
- `idx_price_history_changed_at` on `price_history(changed_at)`

### Breaking Changes Assessment

**No Breaking Changes:**
- New module with isolated domain boundaries
- ProductService modification is feature-toggle protected
- New API endpoints (versioned if needed: `/api/v2/price-alerts`)
- Database changes are additive only

### Integration Points

1. **Product Price Updates:**
   - Hook into `ProductService.createOrUpdateProduct()` to detect price changes
   - Store price history via `PriceHistoryRepository`
   - Trigger alert evaluation via `PriceDropDetectionService`

2. **Email Service:**
   - Follow pattern from `AbandonedCartEmailService`
   - Use existing email infrastructure (currently logging, TODO for actual email provider)

3. **User Authentication:**
   - Support both logged-in users (via `user_id`) and guest users (via `user_email`)
   - Similar pattern to `AbandonedCart` model

4. **Scheduling:**
   - Use Spring `@Scheduled` annotation (already enabled in `EcompocApplication`)
   - Follow pattern from `AbandonedCartScheduler`

---

## Phase 2: Implementation Design (Developer Brief)

### Module Boundaries

**New Module Structure:**
```
api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/
├── controller/
│   └── PriceAlertController.java
├── dto/
│   ├── CreatePriceAlertRequest.java
│   ├── PriceAlertResponse.java
│   ├── PriceAlertListResponse.java
│   ├── UpdatePriceAlertRequest.java
│   └── PriceHistoryResponse.java
├── model/
│   ├── PriceAlert.java
│   ├── PriceHistory.java
│   ├── AlertStatus.java (enum: ACTIVE, TRIGGERED, EXPIRED, CANCELLED)
│   └── NotificationFrequency.java (enum: IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST)
├── repository/
│   ├── PriceAlertRepository.java
│   └── PriceHistoryRepository.java
├── service/
│   ├── PriceAlertService.java
│   ├── PriceAlertEmailService.java
│   └── PriceDropDetectionService.java
└── scheduler/
    └── PriceDropDetectionScheduler.java
```

### Feature Toggle

**Toggle Name:** `FEATURE_SCRUM_25_PRICE_DROP_ALERTS`

**Configuration:**
```yaml
price-alert:
  enabled: true  # Default ON per architectural principle
  minimum-drop-percentage: 5.0  # Configurable threshold (default 5%)
  detection-interval-minutes: 60  # Check for price changes every hour
  alert-trigger-delay-hours: 2  # Max 2 hours from price change to alert
```

**Implementation Pattern:**
- Use `@Value("${price-alert.enabled:true}")` in services
- Use `@ConditionalOnProperty` for scheduler component
- Follow pattern from `AbandonedCartScheduler` and `AbandonedCartService`

### Route/Logic Strategy

**API Endpoints (New Versioned Routes):**

1. **POST `/api/v2/price-alerts`** - Create price alert
   - Request: `CreatePriceAlertRequest` (productId, email, targetPrice?, notificationFrequency?)
   - Response: `PriceAlertResponse`
   - Feature toggle check: Return 503 if disabled

2. **GET `/api/v2/price-alerts`** - List user's price alerts
   - Query params: `email` (required), `userId` (optional)
   - Response: `PriceAlertListResponse`
   - Feature toggle check: Return empty list if disabled

3. **GET `/api/v2/price-alerts/{alertId}`** - Get specific alert
   - Response: `PriceAlertResponse`
   - Feature toggle check: Return 404 if disabled

4. **PUT `/api/v2/price-alerts/{alertId}`** - Update alert (target price, frequency, status)
   - Request: `UpdatePriceAlertRequest`
   - Response: `PriceAlertResponse`

5. **DELETE `/api/v2/price-alerts/{alertId}`** - Cancel/delete alert
   - Response: 204 No Content

6. **GET `/api/v2/price-alerts/{alertId}/history`** - Get price history for alert's product
   - Response: `PriceHistoryResponse[]`

**Note:** Using `/api/v2/` prefix to follow side-by-side implementation pattern. If versioning is not desired, use `/api/price-alerts` but ensure feature toggle prevents access when disabled.

### Implementation Steps

#### Step 1: Database Schema (Liquibase)
1. Create `db/changelog/pricealert/create-price-alert-tables.xml`
   - Define `price_alerts` table with all columns
   - Define `price_history` table with all columns
   - Create all indexes
2. Add include to `db.changelog-master.xml`:
   ```xml
   <include file="db/changelog/pricealert/create-price-alert-tables.xml"/>
   ```

#### Step 2: Domain Models
1. Create `PriceAlert.java` entity
   - Map to `price_alerts` table
   - Include JPA annotations
   - Follow pattern from `AbandonedCart.java`
2. Create `PriceHistory.java` entity
   - Map to `price_history` table
3. Create enums: `AlertStatus.java`, `NotificationFrequency.java`

#### Step 3: Repositories
1. Create `PriceAlertRepository.java`
   - Extend `JpaRepository<PriceAlert, String>`
   - Add custom queries:
     - `findByProductIdAndStatus(String productId, String status)`
     - `findByUserEmail(String email)`
     - `findByUserId(String userId)`
     - `findActiveAlertsForProduct(String productId)`
2. Create `PriceHistoryRepository.java`
   - Extend `JpaRepository<PriceHistory, String>`
   - Add custom queries:
     - `findByProductIdOrderByChangedAtDesc(String productId)`
     - `findLatestByProductId(String productId)`

#### Step 4: Core Services
1. **PriceAlertService.java**
   - `createPriceAlert(CreatePriceAlertRequest)` - Create alert, validate email, prevent duplicates
   - `getPriceAlerts(String email, String userId)` - List user's alerts
   - `getPriceAlert(String alertId)` - Get specific alert
   - `updatePriceAlert(String alertId, UpdatePriceAlertRequest)` - Update alert
   - `deletePriceAlert(String alertId)` - Cancel alert
   - `getPriceHistory(String productId)` - Get price history
   - Feature toggle check in all methods

2. **PriceDropDetectionService.java**
   - `detectPriceChange(Product product, BigDecimal oldPrice, BigDecimal newPrice)` - Detect and record price changes
   - `evaluateAlertsForProduct(String productId)` - Evaluate all active alerts for a product
   - `shouldTriggerAlert(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice)` - Check if alert should trigger (target price, minimum drop %)
   - `triggerAlert(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice)` - Mark alert as triggered, call email service

3. **PriceAlertEmailService.java**
   - Follow pattern from `AbandonedCartEmailService`
   - `sendPriceDropEmail(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice)` - Send price drop notification
   - `sendConfirmationEmail(PriceAlert alert)` - Send confirmation when alert created
   - Feature toggle check
   - TODO: Integrate with actual email service provider

#### Step 5: Scheduler
1. **PriceDropDetectionScheduler.java**
   - `@Scheduled(fixedRate = 3600000)` - Run every hour
   - `@ConditionalOnProperty(name = "price-alert.enabled", havingValue = "true")`
   - Query products updated in last hour
   - For each product with price change:
     - Record price history
     - Evaluate active alerts
     - Trigger qualifying alerts

#### Step 6: ProductService Integration
1. Modify `ProductService.createOrUpdateProduct()`
   - Before saving, check if price changed (compare old vs new)
   - If price changed AND feature toggle enabled:
     - Call `PriceDropDetectionService.detectPriceChange()`
   - Use feature toggle to gate this logic

#### Step 7: Controller
1. **PriceAlertController.java**
   - `@RequestMapping("/api/v2/price-alerts")`
   - Implement all 6 endpoints
   - Feature toggle check in each method (return appropriate error if disabled)
   - Use `@RestController`, `@Valid` for request validation
   - Follow pattern from `AbandonedCartController`

#### Step 8: DTOs
1. Create all request/response DTOs
2. Include validation annotations (`@NotNull`, `@Email`, `@Min`, etc.)
3. Use `@JsonInclude(JsonInclude.Include.NON_NULL)` for optional fields

#### Step 9: Configuration
1. Add to `application.yml`:
   ```yaml
   price-alert:
     enabled: true
     minimum-drop-percentage: 5.0
     detection-interval-minutes: 60
     alert-trigger-delay-hours: 2
   ```

#### Step 10: Frontend Integration (UI)
1. Create `PriceAlertService.ts` in `ui/src/services/`
   - Methods: `createAlert()`, `getAlerts()`, `updateAlert()`, `deleteAlert()`, `getPriceHistory()`
2. Create `PriceAlertButton.tsx` component
   - Display on product detail page
   - Show "Notify me when price drops" button
   - Modal/form for email input and target price (optional)
3. Create `PriceAlertDashboard.tsx` component
   - Display user's active alerts
   - Allow editing/deleting alerts
   - Show price history
4. Add route in `App.tsx` for alert management page

### Testing Strategy

1. **Unit Tests:**
   - `PriceAlertServiceTest` - All service methods
   - `PriceDropDetectionServiceTest` - Price change detection, alert evaluation logic
   - `PriceAlertEmailServiceTest` - Email sending logic
   - `PriceDropDetectionSchedulerTest` - Scheduler logic

2. **Integration Tests:**
   - Postman collection for all API endpoints
   - Test price change detection flow
   - Test alert triggering flow

3. **E2E Tests:**
   - Selenium test: Create alert, change price, verify email sent
   - Selenium test: Alert management dashboard

---

## Phase 3: Documentation Maintenance

### Confluence Update: Architectural Overview

**Page:** https://ecompoc.atlassian.net/wiki/spaces/EcomPOC/pages/819202/Architectural+Overview

**Recommended Updates:**

#### 1. Add to Domain Modules List

In the "Backend Architecture" section, add:

**Price Alert Module (`pricealert`)**
- **Purpose:** Price drop alert system for re-engaging price-sensitive customers
- **Key Components:**
  - `PriceAlertService` - Alert creation and management
  - `PriceDropDetectionService` - Price change detection and alert evaluation
  - `PriceAlertEmailService` - Email notifications for price drops
  - `PriceDropDetectionScheduler` - Background job for processing price changes
- **Database Tables:** `price_alerts`, `price_history`
- **Feature Toggle:** `price-alert.enabled` (default: true)
- **API Endpoints:** `/api/v2/price-alerts/*`

#### 2. Update Technology Stack Summary

Add to dependencies:
- Price tracking and alert system
- Email notification system (price drop alerts)

#### 3. Add to Feature Details Page

**New Feature: Price Drop Alerts**

**Description:**
Automated price drop alert system that allows customers to receive email notifications when products they're watching drop in price. Supports both logged-in and guest users, configurable target prices, and multiple notification frequency options.

**Business Value:**
- Recover 12-18% of price-related abandoned carts
- Increase conversion rate by 2-3 percentage points
- Generate ~$54,180 annually in incremental revenue
- Improve email engagement (35-45% open rates, 15-25% click-through rates)

**Technical Implementation:**
- New domain module: `pricealert`
- Price history tracking for all products
- Background scheduler for price change detection
- Email notification system integration
- Feature toggle: `price-alert.enabled`

**API Endpoints:**
- `POST /api/v2/price-alerts` - Create alert
- `GET /api/v2/price-alerts` - List alerts
- `PUT /api/v2/price-alerts/{id}` - Update alert
- `DELETE /api/v2/price-alerts/{id}` - Delete alert
- `GET /api/v2/price-alerts/{id}/history` - Get price history

**Database Schema:**
- `price_alerts` - Stores user price alerts
- `price_history` - Tracks product price changes over time

**Related Features:**
- Complements abandoned cart recovery (targets price-sensitive customers)
- Integrates with product catalog (price change detection)
- Uses email infrastructure (price drop notifications)

---

## Files to be Created/Modified

### New Files (Backend)

1. **Database:**
   - `api/services/ecompoc/src/main/resources/db/changelog/pricealert/create-price-alert-tables.xml`

2. **Domain Models:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/model/PriceAlert.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/model/PriceHistory.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/model/AlertStatus.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/model/NotificationFrequency.java`

3. **Repositories:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/repository/PriceAlertRepository.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/repository/PriceHistoryRepository.java`

4. **Services:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/service/PriceAlertService.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/service/PriceDropDetectionService.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/service/PriceAlertEmailService.java`

5. **Scheduler:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/scheduler/PriceDropDetectionScheduler.java`

6. **Controller:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/controller/PriceAlertController.java`

7. **DTOs:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/dto/CreatePriceAlertRequest.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/dto/PriceAlertResponse.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/dto/PriceAlertListResponse.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/dto/UpdatePriceAlertRequest.java`
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/pricealert/dto/PriceHistoryResponse.java`

8. **Tests:**
   - `api/services/ecompoc/src/test/java/com/example/ecompoc/pricealert/service/PriceAlertServiceTest.java`
   - `api/services/ecompoc/src/test/java/com/example/ecompoc/pricealert/service/PriceDropDetectionServiceTest.java`
   - `api/services/ecompoc/src/test/java/com/example/ecompoc/pricealert/service/PriceAlertEmailServiceTest.java`
   - `api/services/ecompoc/src/test/java/com/example/ecompoc/pricealert/scheduler/PriceDropDetectionSchedulerTest.java`
   - `api/services/ecompoc/src/test/java/com/example/ecompoc/pricealert/controller/PriceAlertControllerTest.java`

### Modified Files (Backend)

1. **Database Changelog:**
   - `api/services/ecompoc/src/main/resources/db/changelog/db.changelog-master.xml` - Add include for price alert tables

2. **Product Service:**
   - `api/services/ecompoc/src/main/java/com/example/ecompoc/product/service/ProductService.java` - Add price change detection hook

3. **Configuration:**
   - `api/services/ecompoc/src/main/resources/application.yml` - Add price-alert configuration

### New Files (Frontend)

1. **Services:**
   - `ui/src/services/priceAlertService.ts`

2. **Components:**
   - `ui/src/components/PriceAlertButton.tsx`
   - `ui/src/components/PriceAlertDashboard.tsx`
   - `ui/src/components/PriceAlertButton.css` (if needed)
   - `ui/src/components/PriceAlertDashboard.css` (if needed)

3. **Pages:**
   - `ui/src/pages/PriceAlertsPage.tsx` (if separate page needed)
   - `ui/src/pages/PriceAlertsPage.css` (if needed)

### Modified Files (Frontend)

1. **Product Detail Page:**
   - `ui/src/pages/ProductDetailPage.tsx` - Add PriceAlertButton component

2. **App Routing:**
   - `ui/src/App.tsx` - Add route for price alerts management (if separate page)

3. **Context (if needed):**
   - `ui/src/context/AppContext.tsx` - Add price alert state management (optional)

---

## Implementation Notes

### Feature Toggle Strategy

**Default State:** ON (`enabled: true`)

**Toggle Removal:**
- All price alert code is isolated in `pricealert` module
- ProductService integration is gated by feature toggle
- Controller endpoints return appropriate errors when disabled
- Scheduler is conditionally loaded
- Easy to remove: Delete `pricealert` module, remove ProductService hook, remove config

### Non-Intrusive Implementation

1. **ProductService Hook:**
   - Use feature toggle check before calling price detection
   - No changes to core product update logic
   - Side-by-side implementation

2. **API Versioning:**
   - Use `/api/v2/price-alerts` to avoid conflicts
   - Can be changed to `/api/price-alerts` if versioning not desired

3. **Email Service:**
   - Follows existing pattern (logging + TODO for actual email provider)
   - No changes to existing email infrastructure

### Performance Considerations

1. **Price History:**
   - Index on `product_id` and `changed_at` for fast queries
   - Consider archiving old history (>1 year) if needed

2. **Alert Evaluation:**
   - Batch processing in scheduler
   - Only evaluate alerts for products with recent price changes
   - Cache product prices if needed

3. **Email Sending:**
   - Batch email sending in scheduler
   - Rate limiting to prevent email spam

### Security Considerations

1. **Email Validation:**
   - Validate email format in `CreatePriceAlertRequest`
   - Prevent duplicate alerts (same product + email)

2. **Access Control:**
   - Users can only view/edit their own alerts (by email or userId)
   - Validate ownership before update/delete operations

3. **Rate Limiting:**
   - Limit alert creation per email (e.g., max 50 alerts per email)
   - Prevent abuse/spam

---

## Summary

This implementation follows the architectural principles:
- **Modular Monolith:** New isolated `pricealert` domain module with clear boundaries
- **KISS:** Simple implementation using existing patterns (scheduler, email service, repository)
- **Feature Toggle:** `FEATURE_SCRUM_25_PRICE_DROP_ALERTS` (default ON)
- **Non-Intrusive:** Side-by-side implementation, versioned API endpoints, feature-toggle gated ProductService hook
- **Clean Removal:** All code isolated in single module, easy to remove if needed

The implementation leverages existing infrastructure (scheduling, email patterns, database) while maintaining strict domain boundaries and feature toggle compliance.

