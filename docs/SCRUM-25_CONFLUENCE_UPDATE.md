# SCRUM-25: Confluence Update Proposal

## Target Page
**Architectural Overview:** https://ecompoc.atlassian.net/wiki/spaces/EcomPOC/pages/819202/Architectural+Overview

---

## Update 1: Add Price Alert Module to Backend Architecture Section

### Location
In the "Backend Architecture" section, add a new domain module entry.

### Content to Add

**Price Alert Module (`pricealert`)**

**Purpose:** Price drop alert system for re-engaging price-sensitive customers who are waiting for price reductions.

**Key Components:**
- `PriceAlertService` - Alert creation, management, and retrieval
- `PriceDropDetectionService` - Price change detection and alert evaluation logic
- `PriceAlertEmailService` - Email notifications for price drops and confirmations
- `PriceDropDetectionScheduler` - Background job that runs hourly to detect price changes and trigger alerts
- `PriceAlertController` - REST API endpoints for alert management

**Database Tables:**
- `price_alerts` - Stores user price alerts (product, email, target price, notification preferences)
- `price_history` - Tracks product price changes over time for analytics and alert evaluation

**Feature Toggle:** `price-alert.enabled` (default: `true`)

**API Endpoints:** `/api/v2/price-alerts/*`

**Integration Points:**
- **Product Module:** Hooks into `ProductService.createOrUpdateProduct()` to detect price changes (feature-toggle gated)
- **User Module:** Uses existing user authentication/identification (read-only)
- **Email Infrastructure:** Follows existing email service pattern (`AbandonedCartEmailService`, `OrderEmailService`)

**Business Value:**
- Recover 12-18% of price-related abandoned carts
- Increase conversion rate by 2-3 percentage points
- Generate ~$54,180 annually in incremental revenue
- Improve email engagement (35-45% open rates, 15-25% click-through rates)

---

## Update 2: Add to Feature Details Page

### Location
In the "Feature Details" page (https://ecompoc.atlassian.net/wiki/spaces/EcomPOC/pages/16023553), add a new feature section.

### Content to Add

### Price Drop Alerts

**Status:** ✅ Implemented (SCRUM-25)

**Description:**
Automated price drop alert system that allows customers to receive email notifications when products they're watching drop in price. Supports both logged-in and guest users, configurable target prices, and multiple notification frequency options (immediate, daily digest, weekly digest).

**Key Features:**
- Create price alerts for any product (with optional target price)
- Support for both logged-in users and guest users (email-based)
- Price change detection via background scheduler (runs hourly)
- Email notifications with product details, savings amount, and call-to-action
- Alert management dashboard for viewing, editing, and canceling alerts
- Price history tracking for analytics and user reference
- Configurable minimum price drop threshold (default: 5%)

**Business Value:**
- **Cart Recovery:** Recover 12-18% of price-related abandoned carts
- **Conversion Rate:** Increase by 2-3 percentage points
- **Revenue Impact:** ~$54,180 annually in incremental revenue
- **Email Engagement:** 35-45% open rates, 15-25% click-through rates
- **Customer Lifetime Value:** Re-engage price-sensitive customers who might otherwise never return

**Technical Implementation:**
- **Domain Module:** `pricealert` (isolated module with clear boundaries)
- **Database:** `price_alerts` and `price_history` tables
- **Background Processing:** `PriceDropDetectionScheduler` (hourly price change detection)
- **Email Integration:** `PriceAlertEmailService` (follows existing email service pattern)
- **Feature Toggle:** `price-alert.enabled` (default: `true`)
- **API Versioning:** `/api/v2/price-alerts/*` endpoints

**API Endpoints:**
- `POST /api/v2/price-alerts` - Create price alert
- `GET /api/v2/price-alerts` - List user's alerts (query by email/userId)
- `GET /api/v2/price-alerts/{alertId}` - Get specific alert
- `PUT /api/v2/price-alerts/{alertId}` - Update alert (target price, frequency, status)
- `DELETE /api/v2/price-alerts/{alertId}` - Cancel/delete alert
- `GET /api/v2/price-alerts/{alertId}/history` - Get price history for product

**Database Schema:**
- **`price_alerts`:** Stores user price alerts with product ID, email, target price, notification preferences, and status
- **`price_history`:** Tracks all product price changes with timestamps, change type, and percentage

**Related Features:**
- **Abandoned Cart Recovery:** Complements by targeting price-sensitive customers specifically
- **Product Catalog:** Integrates with product price updates for change detection
- **Email Infrastructure:** Uses existing email service patterns for notifications
- **Wishlist:** Future enhancement could allow automatic alert creation for wishlist items

**Configuration:**
```yaml
price-alert:
  enabled: true
  minimum-drop-percentage: 5.0
  detection-interval-minutes: 60
  alert-trigger-delay-hours: 2
```

**Future Enhancements:**
- Browser push notifications as alternative to email
- Automatic alert creation for wishlist items
- Price increase alerts (notify before prices go up)
- Competitor price match alerts
- A/B testing for email templates and messaging

---

## Update 3: Update Technology Stack Summary (Optional)

### Location
In the "Technology Stack Summary" section, add to the Backend dependencies list.

### Content to Add

**Additional Dependencies:**
- Price tracking and alert system (price drop detection, alert management)
- Email notification system (price drop alerts, confirmations)

---

## Update 4: Add to Data Flows Page (Optional)

### Location
In the "Data Flows" page (https://ecompoc.atlassian.net/wiki/spaces/EcomPOC/pages/15925250), add a new flow diagram.

### Content to Add

### Price Drop Alert Flow

**Flow Description:**
1. Customer creates price alert (via product page or dashboard)
2. System stores alert in `price_alerts` table
3. Background scheduler runs hourly to detect price changes
4. When product price changes, system records in `price_history`
5. System evaluates all active alerts for the product
6. If alert qualifies (target price match + minimum drop %), trigger alert
7. Email service sends price drop notification
8. Customer receives email with product details and call-to-action
9. Customer clicks link and purchases product

**Key Components:**
- `PriceAlertController` - API endpoint for alert creation
- `PriceAlertService` - Business logic for alert management
- `ProductService` - Price change detection hook
- `PriceDropDetectionScheduler` - Background price change detection
- `PriceDropDetectionService` - Alert evaluation logic
- `PriceAlertEmailService` - Email notification sending

---

## Summary of Changes

1. **Backend Architecture Section:** Add `pricealert` module description
2. **Feature Details Page:** Add comprehensive Price Drop Alerts feature section
3. **Technology Stack (Optional):** Add price tracking and email notification dependencies
4. **Data Flows (Optional):** Add price drop alert flow diagram

All updates maintain consistency with existing documentation style and structure.

