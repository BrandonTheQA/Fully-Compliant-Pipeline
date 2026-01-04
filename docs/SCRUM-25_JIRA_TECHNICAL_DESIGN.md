# SCRUM-25: Technical Design (For Jira Story)

## Technical Design

### Module Boundaries

**New Domain Module: `pricealert`**

All price alert functionality will be implemented in a new isolated domain module following the existing architectural pattern:

```
com.example.ecompoc.pricealert/
├── controller/PriceAlertController
├── dto/ (Request/Response DTOs)
├── model/ (PriceAlert, PriceHistory entities)
├── repository/ (JPA repositories)
├── service/ (Business logic)
└── scheduler/ (Background price detection)
```

**Integration Points:**
- **Product Module:** Add price change detection hook in `ProductService.createOrUpdateProduct()` (feature-toggle gated)
- **User Module:** Read-only integration for user identification (no changes required)
- **Email Infrastructure:** New `PriceAlertEmailService` following existing email service pattern

### Feature Toggle

**Toggle Name:** `FEATURE_SCRUM_25_PRICE_DROP_ALERTS`

**Configuration:**
```yaml
price-alert:
  enabled: true  # Default ON per architectural principle
  minimum-drop-percentage: 5.0  # Configurable threshold
  detection-interval-minutes: 60  # Check every hour
  alert-trigger-delay-hours: 2  # Max 2 hours from change to alert
```

**Implementation Pattern:**
- Use `@Value("${price-alert.enabled:true}")` in services
- Use `@ConditionalOnProperty` for scheduler component
- Controller endpoints return appropriate errors when disabled

### Route/Logic Strategy

**API Endpoints (Versioned):**

- `POST /api/v2/price-alerts` - Create price alert
- `GET /api/v2/price-alerts` - List user's alerts (query by email/userId)
- `GET /api/v2/price-alerts/{alertId}` - Get specific alert
- `PUT /api/v2/price-alerts/{alertId}` - Update alert (target price, frequency, status)
- `DELETE /api/v2/price-alerts/{alertId}` - Cancel/delete alert
- `GET /api/v2/price-alerts/{alertId}/history` - Get price history for product

**Note:** Using `/api/v2/` prefix for side-by-side implementation. All endpoints are feature-toggle protected.

### Database Schema

**New Tables:**

1. **`price_alerts`**
   - `alert_id` (PK), `product_id` (FK), `user_email`, `user_id` (nullable FK)
   - `target_price` (nullable), `current_price`, `notification_frequency`
   - `status` (ACTIVE, TRIGGERED, EXPIRED, CANCELLED)
   - `created_at`, `last_triggered_at`, `updated_at`
   - Indexes: `product_id`, `user_email`, `status`, `created_at`

2. **`price_history`**
   - `price_history_id` (PK), `product_id` (FK), `price`, `previous_price`
   - `change_type`, `change_percentage`, `changed_at`
   - Indexes: `product_id`, `changed_at`

### Implementation Approach

**Non-Intrusive Implementation:**
- New isolated module with clear domain boundaries
- ProductService hook is feature-toggle gated (no impact when disabled)
- Versioned API endpoints to avoid conflicts
- Follows existing patterns (scheduler, email service, repository)

**Price Change Detection:**
- Hook into `ProductService.createOrUpdateProduct()` to detect price changes
- Store price history via `PriceHistoryRepository`
- Background scheduler (`PriceDropDetectionScheduler`) runs hourly to:
  - Find products with recent price changes
  - Evaluate active alerts for those products
  - Trigger qualifying alerts (target price match + minimum drop %)

**Email Notifications:**
- Follow pattern from `AbandonedCartEmailService`
- Send confirmation email when alert created
- Send price drop email when alert triggered
- Support immediate, daily digest, and weekly digest frequencies

### Files to be Created

**Backend (Java):**
- Domain models: `PriceAlert`, `PriceHistory`, enums (`AlertStatus`, `NotificationFrequency`)
- Repositories: `PriceAlertRepository`, `PriceHistoryRepository`
- Services: `PriceAlertService`, `PriceDropDetectionService`, `PriceAlertEmailService`
- Scheduler: `PriceDropDetectionScheduler`
- Controller: `PriceAlertController`
- DTOs: Request/Response DTOs for all endpoints
- Database: Liquibase changelog for new tables
- Tests: Unit tests for all services, integration tests for controller

**Frontend (TypeScript/React):**
- Service: `priceAlertService.ts`
- Components: `PriceAlertButton.tsx`, `PriceAlertDashboard.tsx`
- Integration: Add alert button to product detail page

**Configuration:**
- Add `price-alert` section to `application.yml`

### Testing Strategy

1. **Unit Tests:** Service logic, price detection, alert evaluation
2. **Integration Tests:** Postman collection for all API endpoints
3. **E2E Tests:** Selenium tests for alert creation and price drop flow

### Clean Removal Strategy

All code is isolated in the `pricealert` module. To remove:
1. Delete `pricealert` package
2. Remove ProductService hook
3. Remove configuration
4. Remove database tables (via Liquibase rollback)

No impact on other modules when feature is disabled or removed.

