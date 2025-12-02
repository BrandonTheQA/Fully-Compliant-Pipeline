# Test Plan: SCRUM-10 - Automated Abandoned Cart Email Recovery with Personalized Shipping Discounts

## 1. 📝 Story Summary & Core Objective

**Story:** Automated Abandoned Cart Email Recovery with Personalized Shipping Discounts to Recover Lost Revenue

**Core Objective:** Automatically detect abandoned carts, send personalized recovery emails with shipping discounts, and enable users to restore their carts with discounts applied, recovering 10-15% of abandoned carts and generating incremental revenue.

**User Problem Solved:** Users who abandon carts due to shipping costs currently receive no follow-up communication, resulting in permanent loss of potential sales. This feature automatically re-engages abandoned cart users through personalized email campaigns with shipping discounts, reducing cart abandonment and recovering lost revenue.

---

## 2. ✅ Acceptance Criteria (AC) Test Cases

### **AC 1: Abandoned Cart Detection and Email Trigger**
**Given** a user adds items to their cart  
**When** the user leaves the site without completing checkout for more than 30 minutes  
**Then** the system must detect the abandoned cart  
**And** store the abandoned cart data including: cart items, cart total, shipping region, user email (if logged in), timestamp  
**And** automatically send a recovery email within 1 hour of abandonment (if email is available)

**Measurement:** 100% of carts abandoned for >30 minutes are detected and tracked, with emails sent within 1 hour ± 5 minutes

#### Test Cases:

* **Test Case 1.1:** Cart abandoned for 30+ minutes is detected
  * **Description:** User adds items to cart, leaves site for 35 minutes - system detects cart as abandoned
  * **Expected Result:** Cart marked as "ABANDONED" status in database, `abandoned_at` timestamp recorded
  * **Automated Test Type:** Unit test (Scheduler), Integration test (Postman - verify database state)

* **Test Case 1.2:** Cart data stored correctly
  * **Description:** Abandoned cart contains 3 items totaling $45.00 - all data stored accurately
  * **Expected Result:** `abandoned_carts` table contains correct cartItems (JSON), cartTotal ($45.00), shippingRegion, email, abandonedAt timestamp
  * **Automated Test Type:** Unit test (Service - cart persistence), Integration test (Postman - verify database)

* **Test Case 1.3:** Email sent within 1 hour of abandonment
  * **Description:** Cart abandoned at 10:00 AM - first recovery email sent by 11:00 AM (± 5 minutes)
  * **Expected Result:** Email sent within 60-65 minutes of abandonment, `abandoned_cart_emails` table records email with `email_type=FIRST`
  * **Automated Test Type:** Unit test (Scheduler - timing), Integration test (Postman - verify email sent)

* **Test Case 1.4:** Email not sent if user email unavailable
  * **Description:** Guest user (no email) abandons cart - cart stored but no email sent
  * **Expected Result:** Cart stored with `email=null`, no email record in `abandoned_cart_emails` table
  * **Automated Test Type:** Unit test (Service - email validation), Integration test (Postman)

* **Test Case 1.5:** Cart not detected if user completes checkout within 30 minutes
  * **Description:** User adds items at 10:00 AM, completes checkout at 10:25 AM - cart not marked as abandoned
  * **Expected Result:** Cart not in `abandoned_carts` table, or status is "RECOVERED" if previously abandoned
  * **Automated Test Type:** Unit test (Scheduler - exclusion logic), Integration test (Postman)

* **Test Case 1.6:** Multiple abandoned carts for same user handled correctly
  * **Description:** User abandons Cart A at 10:00 AM, abandons Cart B at 2:00 PM - both stored separately
  * **Expected Result:** Two separate records in `abandoned_carts` table with different IDs and timestamps
  * **Automated Test Type:** Unit test (Service - cart ID generation), Integration test (Postman)

---

### **AC 2: Personalized Shipping Discount Calculation**
**Given** an abandoned cart is detected  
**When** calculating the shipping discount offer  
**Then** the system must apply personalized discount tiers based on cart value:  
* Cart value < $25: Offer 50% off shipping (or $2.99 discount, whichever is greater)  
* Cart value $25-$49: Offer free shipping (up to $5.99 value)  
* Cart value $50-$99: Offer free shipping + 5% off cart total  
* Cart value ≥ $100: Offer free shipping + 10% off cart total  
**And** the discount must be calculated based on the user's shipping region  
**And** returning customers (2+ previous orders) receive one tier higher discount  
**And** the discount must be clearly displayed in the email

**Measurement:** Discount calculation accuracy of 100%, verified through unit tests covering all cart value ranges and user segments

#### Test Cases:

* **Test Case 2.1:** Cart < $25 calculates 50% off shipping or $2.99 minimum
  * **Description:** Cart value $20.00, shipping cost $9.99 - discount should be max(50% of $9.99=$4.99, $2.99) = $4.99
  * **Expected Result:** Discount calculated as $4.99 (50% off shipping), discountType="SHIPPING_PERCENT", discountValue=50.0
  * **Automated Test Type:** Unit test (Service - discount calculation)

* **Test Case 2.2:** Cart < $25 with low shipping cost uses $2.99 minimum
  * **Description:** Cart value $15.00, shipping cost $3.00 - discount should be max(50% of $3.00=$1.50, $2.99) = $2.99
  * **Expected Result:** Discount calculated as $2.99 (minimum), discountType="SHIPPING_FIXED", discountValue=2.99
  * **Automated Test Type:** Unit test (Service - discount calculation)

* **Test Case 2.3:** Cart $25-$49 offers free shipping
  * **Description:** Cart value $35.00 - discount should be free shipping (up to $5.99 value)
  * **Expected Result:** Discount calculated as free shipping, discountType="FREE_SHIPPING", discountValue=5.99
  * **Automated Test Type:** Unit test (Service - discount calculation)

* **Test Case 2.4:** Cart $50-$99 offers free shipping + 5% off cart
  * **Description:** Cart value $75.00 - discount should be free shipping + 5% off ($3.75)
  * **Expected Result:** Discount includes free shipping + cart discount, discountType="CART_PERCENT", discountValue=5.0
  * **Automated Test Type:** Unit test (Service - discount calculation)

* **Test Case 2.5:** Cart ≥ $100 offers free shipping + 10% off cart
  * **Description:** Cart value $150.00 - discount should be free shipping + 10% off ($15.00)
  * **Expected Result:** Discount includes free shipping + cart discount, discountType="CART_PERCENT", discountValue=10.0
  * **Automated Test Type:** Unit test (Service - discount calculation)

* **Test Case 2.6:** Returning customer (2+ orders) receives one tier higher discount
  * **Description:** Cart value $35.00 (normally free shipping tier) for returning customer - should get $50-$99 tier (free shipping + 5% off)
  * **Expected Result:** Discount calculated as free shipping + 5% off cart, not just free shipping
  * **Automated Test Type:** Unit test (Service - returning customer logic), Integration test (Postman - verify user order count)

* **Test Case 2.7:** Discount calculated based on shipping region
  * **Description:** Same cart value $35.00 - US region gets free shipping (up to $5.99), CA region gets free shipping (up to $7.99)
  * **Expected Result:** Discount value matches region-specific shipping cost cap
  * **Automated Test Type:** Unit test (Service - region-specific discount), Integration test (Postman)

* **Test Case 2.8:** Discount code generated and stored
  * **Description:** Discount calculated - unique discount code generated and stored in `abandoned_carts` table
  * **Expected Result:** `discount_code` field populated with unique UUID-based code, `expires_at` set to 48 hours from now
  * **Automated Test Type:** Unit test (Service - discount code generation)

---

### **AC 3: Email Content and Cart Restoration**
**Given** an abandoned cart recovery email is generated  
**When** the email is sent to the user  
**Then** the email must include:
* Personalized greeting with user's name (if available)
* List of abandoned cart items with images and prices
* Cart total and calculated shipping cost
* Personalized shipping discount offer with clear value
* Prominent "Complete Your Purchase" call-to-action button
* Discount code or automatic application mechanism
* Expiration time for the offer (e.g., "Offer expires in 48 hours")  
**And** when the user clicks the "Complete Purchase" link, the abandoned cart must be automatically restored with all original items  
**And** the personalized shipping discount must be automatically applied  
**And** the user must be able to complete checkout with the discounted pricing

**Measurement:** 100% of recovery email clicks successfully restore cart and apply discount, verified through end-to-end testing. Email deliverability rate >95%, open rate >40%, click-through rate >18%

#### Test Cases:

* **Test Case 3.1:** Email contains personalized greeting
  * **Description:** Email sent to user "John Doe" - greeting says "Hi John" or "Hello John Doe"
  * **Expected Result:** Email template includes user's name in greeting section
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.2:** Email contains cart items list with images and prices
  * **Description:** Abandoned cart has 3 items - email displays all 3 items with product images and prices
  * **Expected Result:** Email HTML contains product images (img tags), product names, and prices for all cart items
  * **Automated Test Type:** Unit test (Email service - template generation), Integration test (Postman - verify email content)

* **Test Case 3.3:** Email contains cart total and shipping cost
  * **Description:** Cart total $45.00, shipping $9.99 - email displays both values
  * **Expected Result:** Email shows "Cart Total: $45.00" and "Shipping: $9.99"
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.4:** Email contains personalized discount offer
  * **Description:** Discount calculated as free shipping + 5% off - email clearly displays "FREE Shipping + 5% Off Your Order"
  * **Expected Result:** Email shows discount offer with clear value and savings amount
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.5:** Email contains "Complete Your Purchase" CTA button
  * **Description:** Email has prominent button with text "Complete Your Purchase" or similar
  * **Expected Result:** Email HTML contains button/link with CTA text, links to cart restoration URL
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.6:** Email contains discount code
  * **Description:** Discount code "ABC123" generated - email displays code clearly
  * **Expected Result:** Email shows discount code (e.g., "Use code ABC123 at checkout")
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.7:** Email contains expiration time
  * **Description:** Discount expires in 48 hours - email shows "Offer expires in 48 hours" or expiration date
  * **Expected Result:** Email displays expiration message with countdown or date
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 3.8:** Clicking recovery link restores cart
  * **Description:** User clicks "Complete Purchase" link - cart restored with all original items
  * **Expected Result:** Cart items restored, cart total matches original, user redirected to order form
  * **Automated Test Type:** Integration test (Postman - cart restoration), E2E test (Selenium)

* **Test Case 3.9:** Discount automatically applied when cart restored
  * **Description:** User clicks recovery link - discount code automatically applied to restored cart
  * **Expected Result:** Cart shows discounted total, shipping cost reflects discount (e.g., $0.00 for free shipping)
  * **Automated Test Type:** Integration test (Postman - discount application), E2E test (Selenium)

* **Test Case 3.10:** User can complete checkout with discount
  * **Description:** Cart restored with discount - user can proceed through checkout and complete order
  * **Expected Result:** Order created successfully with discounted total, discount code marked as used
  * **Automated Test Type:** E2E test (Selenium) - **Primary E2E test**

* **Test Case 3.11:** Email tracking pixel records open
  * **Description:** Email opened - tracking pixel (`/api/abandoned-carts/email/{emailId}/track/open`) called
  * **Expected Result:** `abandoned_cart_emails.opened_at` timestamp recorded
  * **Automated Test Type:** Integration test (Postman - verify tracking endpoint)

* **Test Case 3.12:** Email click tracking records click
  * **Description:** User clicks recovery link - click tracking endpoint called before redirect
  * **Expected Result:** `abandoned_cart_emails.clicked_at` timestamp recorded
  * **Automated Test Type:** Integration test (Postman - verify tracking endpoint)

---

### **AC 4: Follow-Up Email Sequence**
**Given** a user receives the first abandoned cart recovery email  
**When** the user does not complete the purchase within 24 hours  
**Then** a second follow-up email must be sent with:
* Reminder of abandoned items
* Increased discount offer (e.g., if first email offered free shipping, second offers free shipping + 5% off)
* Urgency messaging ("Last chance - items may sell out")
* Same cart restoration functionality  
**And** if the user still doesn't complete purchase within 72 hours, a final follow-up email must be sent with maximum discount offer (free shipping + 10% off, regardless of cart value)

**Measurement:** Follow-up emails sent at correct intervals (24h ± 1h, 72h ± 2h) with 100% accuracy

#### Test Cases:

* **Test Case 4.1:** Second email sent 24 hours after first email
  * **Description:** First email sent at 10:00 AM - second email sent at 10:00 AM next day (± 1 hour)
  * **Expected Result:** Follow-up email sent 24 hours after first email, `email_type=FOLLOWUP_24H` recorded
  * **Automated Test Type:** Unit test (Scheduler - timing), Integration test (Postman - verify email sent)

* **Test Case 4.2:** Second email contains increased discount
  * **Description:** First email offered free shipping - second email offers free shipping + 5% off
  * **Expected Result:** Second email discount is one tier higher than first email
  * **Automated Test Type:** Unit test (Service - follow-up discount calculation), Integration test (Postman)

* **Test Case 4.3:** Second email contains urgency messaging
  * **Description:** Second email includes text like "Last chance - items may sell out" or similar urgency message
  * **Expected Result:** Email template includes urgency messaging in second follow-up
  * **Automated Test Type:** Unit test (Email service - template generation)

* **Test Case 4.4:** Third email sent 72 hours after first email
  * **Description:** First email sent at 10:00 AM Monday - third email sent at 10:00 AM Thursday (± 2 hours)
  * **Expected Result:** Final follow-up email sent 72 hours after first email, `email_type=FOLLOWUP_72H` recorded
  * **Automated Test Type:** Unit test (Scheduler - timing), Integration test (Postman)

* **Test Case 4.5:** Third email contains maximum discount
  * **Description:** Third email offers free shipping + 10% off, regardless of original cart value
  * **Expected Result:** Discount is maximum tier (free shipping + 10% off) even if cart was < $25
  * **Automated Test Type:** Unit test (Service - maximum discount logic), Integration test (Postman)

* **Test Case 4.6:** No follow-up emails sent if user completes purchase
  * **Description:** User receives first email, completes purchase within 24 hours - no second email sent
  * **Expected Result:** Cart status is "RECOVERED", no follow-up emails sent
  * **Automated Test Type:** Unit test (Scheduler - exclusion logic), Integration test (Postman)

* **Test Case 4.7:** Follow-up emails use same cart restoration functionality
  * **Description:** User clicks recovery link in second or third email - cart restored same as first email
  * **Expected Result:** Cart restoration works identically for all email types
  * **Automated Test Type:** Integration test (Postman), E2E test (Selenium)

---

## 3. 👍 Positive Test Cases ("Happy Path")

### HP-1: Complete Happy Path Flow
**Description:** User abandons cart, receives email, clicks link, restores cart with discount, completes purchase
1. User adds 3 items to cart totaling $45.00 (logged in with email)
2. User leaves site without completing checkout
3. After 35 minutes, cart detected as abandoned
4. Within 1 hour, first recovery email sent with free shipping offer
5. User clicks "Complete Purchase" link in email
6. Cart restored with all 3 items, discount automatically applied
7. User completes checkout with discounted pricing
8. Order created successfully, cart marked as "RECOVERED"
**Automated Test Type:** E2E test (Selenium) - **Primary E2E test**

### HP-2: Returning Customer Receives Better Discount
**Description:** Returning customer (2+ orders) receives one tier higher discount
1. Returning customer abandons cart worth $35.00
2. System calculates discount as free shipping + 5% off (one tier higher than normal)
3. Email sent with enhanced discount offer
4. User restores cart and completes purchase
**Automated Test Type:** Unit test (Service - returning customer logic), E2E test (Selenium)

### HP-3: Follow-Up Email Sequence
**Description:** User receives all three emails in sequence with increasing discounts
1. User receives first email (free shipping offer)
2. User doesn't complete purchase within 24 hours
3. Second email sent with free shipping + 5% off
4. User still doesn't complete purchase within 72 hours
5. Third email sent with free shipping + 10% off (maximum)
6. User clicks third email link and completes purchase
**Automated Test Type:** Unit test (Scheduler - email sequence), E2E test (Selenium)

### HP-4: High-Value Cart Receives Maximum Discount
**Description:** Cart worth $150 receives maximum discount tier from first email
1. User abandons cart worth $150.00
2. System calculates discount as free shipping + 10% off (maximum tier)
3. Email sent with maximum discount offer
4. User restores cart and completes purchase
**Automated Test Type:** Unit test (Service - discount calculation), E2E test (Selenium)

---

## 4. 👎 Negative Test Cases ("Sad Path")

### NP-1: Guest User (No Email) - Cart Stored But No Email Sent
**Description:** Guest user abandons cart without providing email - cart stored but no email sent
**Expected Result:** Cart stored in database with `email=null`, no email record created, no errors thrown
**Automated Test Type:** Unit test (Service - email validation), Integration test (Postman)

### NP-2: Invalid Email Address Handled Gracefully
**Description:** Cart stored with invalid email format (e.g., "notanemail") - email sending should fail gracefully
**Expected Result:** Email send attempt logged as failed, cart still stored, no system crash
**Automated Test Type:** Unit test (Email service - validation), Integration test (Postman)

### NP-3: Email Service Unavailable Handled Gracefully
**Description:** Email service provider (SMTP/API) unavailable - system should retry with exponential backoff
**Expected Result:** Email send retried up to 3 times, failure logged, cart remains in "ABANDONED" status
**Automated Test Type:** Unit test (Email service - retry logic), Integration test (Postman - mock service failure)

### NP-4: Expired Discount Code Rejected
**Description:** User clicks recovery link after discount expiration (48+ hours) - discount code should be invalid
**Expected Result:** Cart restored but discount not applied, message shown that discount expired
**Automated Test Type:** Unit test (Service - discount expiration), Integration test (Postman), E2E test (Selenium)

### NP-5: Already Used Discount Code Rejected
**Description:** User tries to use same discount code twice - second attempt should be rejected
**Expected Result:** Discount code marked as used after first order, second attempt fails with appropriate message
**Automated Test Type:** Unit test (Service - discount code validation), Integration test (Postman)

### NP-6: Cart Restoration Fails If Cart Already Recovered
**Description:** User clicks recovery link but cart was already recovered/expired - should handle gracefully
**Expected Result:** Appropriate error message shown, user redirected to product page or empty cart
**Automated Test Type:** Unit test (Service - cart status validation), Integration test (Postman), E2E test (Selenium)

### NP-7: Invalid Cart ID in Recovery Link Handled Gracefully
**Description:** User clicks recovery link with invalid/non-existent cart ID - should not crash
**Expected Result:** 404 error or appropriate error message, no system crash
**Automated Test Type:** Unit test (Controller - input validation), Integration test (Postman)

### NP-8: Email Sending Rate Limit Handled
**Description:** System attempts to send 1,000+ emails simultaneously - should queue and process without errors
**Expected Result:** Emails queued and sent in batches, no system overload, all emails eventually sent
**Automated Test Type:** Load test (JMeter/Gatling), Integration test (Postman)

---

## 5. 边界 Edge Cases

### EC-1: Cart Abandoned Exactly at 30 Minutes
**Description:** Cart abandoned for exactly 30 minutes (boundary condition) - should be detected
**Expected Result:** Cart detected as abandoned, email sent
**Automated Test Type:** Unit test (Scheduler - boundary timing), Integration test (Postman)

### EC-2: Cart Abandoned at 29 Minutes (Not Detected)
**Description:** Cart abandoned for 29 minutes - should NOT be detected yet
**Expected Result:** Cart not marked as abandoned, no email sent
**Automated Test Type:** Unit test (Scheduler - boundary timing)

### EC-3: Cart Value Exactly at Discount Tier Boundary
**Description:** Cart value exactly $25.00 (boundary between <$25 and $25-$49 tiers) - should use $25-$49 tier
**Expected Result:** Discount calculated as free shipping (not 50% off shipping)
**Automated Test Type:** Unit test (Service - boundary discount calculation)

### EC-4: Cart Value $0.01 (Minimum)
**Description:** Cart with single item worth $0.01 - discount should still calculate
**Expected Result:** Discount calculated for minimum cart value (50% off shipping or $2.99)
**Automated Test Type:** Unit test (Service - minimum cart value)

### EC-5: Cart Value Very High ($10,000+)
**Description:** Cart worth $10,000 - should receive maximum discount tier
**Expected Result:** Discount calculated as free shipping + 10% off (maximum tier)
**Automated Test Type:** Unit test (Service - large cart value)

### EC-6: Empty Cart (No Items) Handled
**Description:** User creates empty cart and abandons - should not trigger email
**Expected Result:** Empty cart not stored as abandoned, or stored but no email sent
**Automated Test Type:** Unit test (Service - empty cart validation)

### EC-7: Cart with Null/Invalid Items
**Description:** Cart contains invalid item data (null product ID, negative quantity) - should handle gracefully
**Expected Result:** Invalid items filtered out or cart not stored, no errors thrown
**Automated Test Type:** Unit test (Service - data validation)

### EC-8: Multiple Rapid Cart Abandonments
**Description:** User abandons cart, immediately adds new items and abandons again - both carts stored separately
**Expected Result:** Two separate abandoned cart records with different timestamps
**Automated Test Type:** Unit test (Service - cart ID uniqueness), Integration test (Postman)

### EC-9: Discount Code Collision (Unlikely but Possible)
**Description:** Two discount codes generated with same UUID (extremely unlikely) - should handle collision
**Expected Result:** System generates new code if collision detected, no duplicate codes in database
**Automated Test Type:** Unit test (Service - code generation uniqueness)

### EC-10: Email Sent Exactly at 1 Hour Boundary
**Description:** Cart abandoned at 10:00:00 AM - email should be sent by 11:00:00 AM (± 5 minutes)
**Expected Result:** Email sent within 60-65 minutes, not before 60 minutes
**Automated Test Type:** Unit test (Scheduler - timing boundary)

### EC-11: Follow-Up Email Timing Edge Cases
**Description:** First email sent at 11:59 PM - second email should be sent next day at 11:59 PM (± 1 hour)
**Expected Result:** Follow-up emails sent at correct intervals even across day boundaries
**Automated Test Type:** Unit test (Scheduler - day boundary handling)

### EC-12: User Completes Purchase Between Email Sends
**Description:** User receives first email, completes purchase 23 hours later - second email should not be sent
**Expected Result:** Cart status updated to "RECOVERED", follow-up email cancelled
**Automated Test Type:** Unit test (Scheduler - status check), Integration test (Postman)

---

## 6. 🔄 Regression Risks

### Risk 1: Order Service Integration
**Area:** OrderService and order creation logic  
**Risk:** Discount code application might interfere with existing order creation or pricing calculations  
**Test Cases:**
- Verify order creation still works correctly without discount codes
- Verify discount codes apply correctly during order creation
- Verify order totals calculate correctly with discounts
- Verify shipping cost calculations respect discount codes
**Automated Test Type:** Unit test (OrderService), Integration test (Postman), E2E test (Selenium)

### Risk 2: Cart State Management (AppContext)
**Area:** AppContext state management for cart items and totals  
**Risk:** Cart restoration might cause cart state inconsistencies or conflicts with existing cart  
**Test Cases:**
- Verify cart restoration doesn't conflict with existing cart items
- Verify cart total calculations remain accurate after restoration
- Verify cart items list remains synchronized
- Verify adding new items after restoration works correctly
**Automated Test Type:** Unit test (React context), E2E test (Selenium)

### Risk 3: Shipping Cost Calculator
**Area:** Shipping Cost Calculator component and `/api/shipping/cost` endpoint  
**Risk:** Discount application might interfere with shipping cost calculations  
**Test Cases:**
- Verify shipping cost calculator still works correctly with discount codes
- Verify shipping costs calculate correctly when discount applied
- Verify free shipping threshold logic works with discounts
- Verify both components can coexist without conflicts
**Automated Test Type:** Integration test (Postman), E2E test (Selenium)

### Risk 4: User Service Integration
**Area:** UserService for user data and order history  
**Risk:** Returning customer detection might cause performance issues or incorrect user data  
**Test Cases:**
- Verify UserService.getUserOrderCount() still works correctly
- Verify user order count queries don't slow down when abandoned cart service is active
- Verify user data remains accurate
- Verify returning customer status calculated correctly
**Automated Test Type:** Integration test (Postman), Performance test

### Risk 5: Product Service Integration
**Area:** ProductService for product details in email templates  
**Risk:** Email template generation might cause performance issues with product queries  
**Test Cases:**
- Verify ProductService.getProductById() still works correctly
- Verify product queries don't slow down when generating email templates
- Verify product images and details remain accurate
- Verify product catalog API performance not impacted
**Automated Test Type:** Integration test (Postman), Performance test

### Risk 6: Scheduled Job System
**Area:** Spring scheduling system and other scheduled jobs  
**Risk:** Abandoned cart scheduler might interfere with other scheduled jobs or cause resource contention  
**Test Cases:**
- Verify other scheduled jobs still run correctly
- Verify scheduler doesn't cause database connection pool exhaustion
- Verify scheduler handles errors gracefully without affecting other jobs
- Verify scheduler performance doesn't degrade system
**Automated Test Type:** Integration test (Postman), Performance test

---

## 7. 📊 Automated Test Strategy (Test Pyramid)

### Unit Tests (Foundation - 70% coverage target)

#### Backend Unit Tests:
- ❌ **AbandonedCartServiceTest** - **CREATE NEW** - Test cart detection, storage, discount calculation
  - Test cases: AC1.1, AC1.2, AC1.4, AC1.5, AC1.6, AC2.1-AC2.8, AC4.2, AC4.5, HP2, HP4, NP1, NP4, NP5, NP6, EC1, EC2, EC3, EC4, EC5, EC6, EC7, EC8, EC9
- ❌ **AbandonedCartEmailServiceTest** - **CREATE NEW** - Test email generation and tracking
  - Test cases: AC3.1-AC3.7, AC3.11, AC3.12, AC4.3, NP2, NP3
- ❌ **AbandonedCartSchedulerTest** - **CREATE NEW** - Test scheduled job logic
  - Test cases: AC1.3, AC4.1, AC4.4, AC4.6, EC1, EC2, EC10, EC11, EC12
- ❌ **AbandonedCartControllerTest** - **CREATE NEW** - Test API endpoints
  - Test cases: AC3.8, AC3.9, NP6, NP7, Risk 1

#### Frontend Unit Tests:
- ❌ **CartRestorationHandler.test.tsx** - **CREATE NEW** - Test cart restoration component
  - Test cases: AC3.8, AC3.9, HP1, NP6, Risk 2
- ❌ **abandonedCartService.test.ts** - **CREATE NEW** - Test API service layer
  - Test cases: AC3.8, AC3.9, NP7

**Current Status:**
- ❌ Backend unit tests: **MISSING** (all need to be created)
- ❌ Frontend unit tests: **MISSING** (all need to be created)

---

### Integration Tests (Middle Layer - 20% coverage)

#### Postman/API Tests:
- ❌ **POST /api/abandoned-carts** - **CREATE NEW** - Save abandoned cart
  - Test cases: AC1.1, AC1.2, AC1.4, AC1.6, NP1, EC6, EC7, EC8
- ❌ **GET /api/abandoned-carts/{id}** - **CREATE NEW** - Get abandoned cart
  - Test cases: AC3.8, NP6, NP7
- ❌ **POST /api/abandoned-carts/{id}/restore** - **CREATE NEW** - Restore cart and apply discount
  - Test cases: AC3.8, AC3.9, HP1, NP4, NP5, NP6, Risk 1, Risk 2
- ❌ **GET /api/abandoned-carts/email/{emailId}/track/open** - **CREATE NEW** - Track email open
  - Test cases: AC3.11
- ❌ **GET /api/abandoned-carts/email/{emailId}/track/click** - **CREATE NEW** - Track email click and redirect
  - Test cases: AC3.12, AC3.8
- ✅ **Integration with OrderService** - Verify discount application
  - Test cases: AC3.9, AC3.10, HP1, Risk 1
- ✅ **Integration with ShippingRuleService** - Verify shipping cost calculations
  - Test cases: AC2.7, Risk 3
- ✅ **Integration with UserService** - Verify returning customer detection
  - Test cases: AC2.6, HP2, Risk 4
- ✅ **Integration with ProductService** - Verify product data in emails
  - Test cases: AC3.2, Risk 5

**Current Status:**
- ❌ Postman tests: **MISSING** (all need to be created)

---

### E2E Tests (Top Layer - 10% coverage)

#### Selenium E2E Tests:
- ❌ **SCRUM10AbandonedCartRecoveryTest.java** - **CREATE NEW** - Happy path E2E test
  - Test case: **HP-1** (Primary happy path: abandon cart → receive email → restore cart → complete purchase)
  - Test cases: AC1.3, AC3.8, AC3.9, AC3.10, AC4.1, AC4.7, HP2, HP3, HP4, NP4, NP6, Risk 1, Risk 2, Risk 3

**Current Status:**
- ❌ E2E tests: **MISSING** (need to create dedicated test class)

---

## 8. 📋 Test Execution Checklist

### Phase 1: Unit Tests (Foundation)
- [ ] Backend: AbandonedCartServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: AbandonedCartEmailServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: AbandonedCartSchedulerTest - **CREATE NEW** - All test cases pass
- [ ] Backend: AbandonedCartControllerTest - **CREATE NEW** - All test cases pass
- [ ] Frontend: CartRestorationHandler.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: abandonedCartService.test.ts - **CREATE NEW** - Service tests

### Phase 2: Integration Tests
- [ ] Postman: Add abandoned cart endpoint test collection
- [ ] Postman: Verify API contract and response format
- [ ] Postman: Test cart detection and storage (AC1)
- [ ] Postman: Test discount calculation (AC2)
- [ ] Postman: Test cart restoration (AC3.8, AC3.9)
- [ ] Postman: Test email tracking endpoints (AC3.11, AC3.12)
- [ ] Postman: Test error scenarios (NP1-NP8)
- [ ] Postman: Test edge cases (EC1-EC12)
- [ ] Postman: Test regression risks (Risk 1-6)

### Phase 3: E2E Tests
- [ ] Selenium: Create SCRUM10AbandonedCartRecoveryTest.java
- [ ] Selenium: Happy path test (HP-1)
- [ ] Selenium: Returning customer test (HP-2)
- [ ] Selenium: Follow-up email sequence test (HP-3)
- [ ] Selenium: High-value cart test (HP-4)
- [ ] Selenium: Discount expiration test (NP-4)
- [ ] Selenium: Regression tests (Risk 1-6)

### Phase 4: Email Testing
- [ ] Email deliverability: Test email sending to multiple providers (Gmail, Outlook, Yahoo)
- [ ] Email template: Test responsive design on mobile and desktop
- [ ] Email content: Verify all required elements present (AC3.1-AC3.7)
- [ ] Email tracking: Verify open and click tracking works (AC3.11, AC3.12)
- [ ] Email timing: Verify emails sent at correct intervals (AC1.3, AC4.1, AC4.4)

### Phase 5: Performance Tests
- [ ] Load test: System handles 1,000+ abandoned carts per hour (AC10)
- [ ] Load test: Email sending doesn't degrade system performance
- [ ] Load test: Scheduler processes carts within 30 minutes
- [ ] Load test: Cart restoration API responds within 500ms

### Phase 6: Manual Testing
- [ ] Email rendering: Test across email clients (Gmail, Outlook, Apple Mail, etc.)
- [ ] Mobile responsiveness: Test email templates on mobile devices
- [ ] User experience: Manual walkthrough of complete flow
- [ ] Accessibility: Verify email templates are accessible

---

## 9. 🎯 Priority Test Cases for Immediate Implementation

### High Priority (Must Have Before Production):
1. **HP-1:** Happy path E2E test (Selenium) - Abandon cart → email → restore → complete purchase
2. **AC1.1-AC1.3:** Cart detection and email sending (Unit + Integration)
3. **AC2.1-AC2.5:** Discount calculation for all tiers (Unit test)
4. **AC3.8-AC3.10:** Cart restoration and discount application (Integration + E2E)
5. **AC6.5:** Graceful degradation when email service unavailable (Unit + Integration)

### Medium Priority (Should Have):
1. **AC2.6:** Returning customer discount logic (Unit + Integration)
2. **AC3.1-AC3.7:** Email content verification (Unit + Manual)
3. **AC4.1-AC4.5:** Follow-up email sequence (Unit + Integration)
4. **NP1-NP8:** Error handling scenarios (Unit + Integration)
5. **Risk 1-3:** Regression tests (E2E)

### Low Priority (Nice to Have):
1. **AC3.11-AC3.12:** Email tracking (Integration)
2. **EC1-EC12:** Additional edge cases (Unit + Integration)
3. **Risk 4-6:** Additional regression tests
4. **Performance tests:** Load testing for high volume

---

## 10. 📝 Test Implementation Notes

### Missing Test Coverage Identified:
1. ❌ **All Backend Unit Tests:** Need to create AbandonedCartServiceTest, AbandonedCartEmailServiceTest, AbandonedCartSchedulerTest, AbandonedCartControllerTest
2. ❌ **All Frontend Unit Tests:** Need to create CartRestorationHandler.test.tsx, abandonedCartService.test.ts
3. ❌ **All Integration Tests:** Need to add abandoned cart endpoints to Postman collection
4. ❌ **Dedicated E2E Test Class:** Need `SCRUM10AbandonedCartRecoveryTest.java`
5. ⚠️ **Email Testing:** Need email deliverability and template testing setup

### Test Data Requirements:
- Test users: New users, returning customers (2+ orders), guest users
- Test carts: Various cart values (<$25, $25-$49, $50-$99, ≥$100), empty carts, invalid carts
- Test regions: US, CA, invalid regions
- Test discount codes: Valid codes, expired codes, used codes, invalid codes
- Test email addresses: Valid emails, invalid emails, unsubscribed emails

### Test Environment Setup:
- Mock EmailService for unit tests (don't send real emails)
- Mock UserService for returning customer detection
- Test database with known abandoned cart data
- Selenium test environment with UI running
- Email testing environment (test email accounts)
- Scheduled job testing setup (mock time for timing tests)

### Component Structure (Expected):
- **New Backend Module:** `abandonedcart/` - Domain module with service, controller, repository
- **New Database Tables:** `abandoned_carts`, `abandoned_cart_emails`
- **New Scheduled Job:** `AbandonedCartScheduler` - Detects and sends emails
- **New Frontend Service:** `abandonedCartService.ts` - API calls for cart restoration
- **New Frontend Component:** `CartRestorationHandler.tsx` - Handles cart restoration from email links
- **Email Templates:** HTML templates for first email, 24h follow-up, 72h follow-up

### Critical Test Scenarios:
1. **Cart Detection Timing (AC1):** Critical - must detect carts accurately within 30 minutes
2. **Discount Calculation Accuracy (AC2):** Critical - must calculate discounts correctly for all tiers
3. **Cart Restoration (AC3.8-AC3.10):** Critical - users must be able to restore and complete purchase
4. **Email Deliverability (AC3):** Critical - emails must be delivered and trackable
5. **Follow-Up Sequence (AC4):** Important - follow-up emails must be sent at correct intervals

---

## 11. 🔍 Test Coverage Analysis

### Current Implementation Status:
Based on the JIRA story description, the feature is **NOT YET IMPLEMENTED**. This test plan should be used to:
1. Guide development to ensure testability
2. Create tests as implementation progresses
3. Validate implementation against acceptance criteria

### Test Pyramid Distribution:
- **Unit Tests:** ~70% of test effort (Services, controllers, schedulers, email generation)
- **Integration Tests:** ~20% of test effort (API contracts, service integrations, email sending)
- **E2E Tests:** ~10% of test effort (Happy path, critical user flows)

### Critical Test Scenarios:
1. **Cart Detection and Email Sending (AC1):** Most critical - system must detect and send emails reliably
2. **Discount Calculation (AC2):** Critical - discounts must be calculated accurately for revenue impact
3. **Cart Restoration (AC3.8-AC3.10):** Critical - users must be able to complete purchase after clicking email
4. **Email Deliverability (AC3):** Critical - emails must reach users for feature to work
5. **Follow-Up Sequence (AC4):** Important - follow-up emails increase recovery rates

---

**Test Plan Created:** 2025-01-22  
**Test Plan Version:** 1.0  
**Story Status:** Analysis (Not Yet Implemented)  
**Next Steps:** 
1. Review test plan with development team
2. Create test cases as implementation begins
3. Execute tests as features are completed
4. Update JIRA story with test plan





