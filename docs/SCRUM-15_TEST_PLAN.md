# Test Plan: SCRUM-15 - Customer Loyalty and Rewards Program

## 1. 📝 Story Summary & Core Objective

**Story:** Customer Loyalty and Rewards Program with Points, Tiers, and Redemption to Increase Customer Retention, Repeat Purchase Rates, and Lifetime Value

**Core Objective:** Implement a comprehensive loyalty program that rewards customers with points for purchases and engagement activities, enables point redemption for discounts, provides tiered membership levels with increasing benefits, and integrates seamlessly with existing platform features to increase customer retention, repeat purchase rates, and lifetime value.

**User Problem Solved:** Currently, customers have no incentive to return or engage beyond purchases, leading to high churn rates and low customer lifetime value. This loyalty program rewards customers for their loyalty, incentivizes repeat purchases, and provides ongoing value through points, tiers, and redemption options, creating a competitive advantage and increasing customer retention.

---

## 2. ✅ Acceptance Criteria (AC) Test Cases

### **AC 1: Points Earning System**
**Given** a customer is enrolled in the loyalty program  
**When** they complete eligible activities  
**Then** the system must award points based on configured rates

#### Test Cases:

* **Test Case 1.1:** Purchase points awarded correctly (1 point per $1 spent)
  * **Description:** Customer makes $75.00 purchase - system awards 75 points
  * **Expected Result:** `LoyaltyTransaction` created with `transaction_type=EARNED`, `points=75`, `activity_type=PURCHASE`, `related_order_id` populated
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman - verify after order completion)

* **Test Case 1.2:** Review submission awards 50 points
  * **Description:** Customer submits product review - system awards 50 points
  * **Expected Result:** Transaction created with `points=50`, `activity_type=REVIEW`, `related_review_id` populated
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman)

* **Test Case 1.3:** Referral awards 100 points when referred customer makes first purchase
  * **Description:** Referred customer completes first purchase - referrer receives 100 points
  * **Expected Result:** Transaction created for referrer with `points=100`, `activity_type=REFERRAL`, `related_referral_id` populated
  * **Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman), E2E test (Selenium)

* **Test Case 1.4:** Social sharing awards 25 points
  * **Description:** Customer shares product on social media - system awards 25 points
  * **Expected Result:** Transaction created with `points=25`, `activity_type=SOCIAL_SHARE`
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman)

* **Test Case 1.5:** Account creation awards 100 welcome points
  * **Description:** New customer creates account - system awards 100 welcome points
  * **Expected Result:** Transaction created with `points=100`, `activity_type=WELCOME`, `loyalty_account` created with `current_points=100`
  * **Automated Test Type:** Unit test (LoyaltyService - enrollment), Integration test (Postman)

* **Test Case 1.6:** Birthday bonus awards 50 points in birthday month
  * **Description:** Customer's birthday month is March - system awards 50 points on March 1st (or first activity)
  * **Expected Result:** Transaction created with `points=50`, `activity_type=BIRTHDAY`
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - birthday logic), Integration test (Postman)

* **Test Case 1.7:** Anniversary bonus awards 100 points on account anniversary
  * **Description:** Customer enrolled on Jan 15, 2024 - system awards 100 points on Jan 15, 2025
  * **Expected Result:** Transaction created with `points=100`, `activity_type=ANNIVERSARY`
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - anniversary logic), Integration test (Postman)

* **Test Case 1.8:** Points calculated immediately upon activity completion
  * **Description:** Order completed at 10:00:00 AM - points transaction created at 10:00:00 AM (same timestamp)
  * **Expected Result:** Transaction `created_at` timestamp matches order completion time
  * **Automated Test Type:** Unit test (LoyaltyPointsService - timing)

* **Test Case 1.9:** Point balance updated in real-time
  * **Description:** Customer has 100 points, earns 50 points - balance immediately shows 150 points
  * **Expected Result:** `LoyaltyAccount.current_points` updated to 150, API returns updated balance
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman)

* **Test Case 1.10:** Point earning history tracked with timestamps
  * **Description:** Customer earns points from multiple activities - all transactions recorded with timestamps
  * **Expected Result:** `LoyaltyTransaction` records show all activities with correct `created_at` timestamps
  * **Automated Test Type:** Unit test (LoyaltyTransactionRepository), Integration test (Postman)

* **Test Case 1.11:** Duplicate point awards prevented for same activity
  * **Description:** Customer submits same review twice - only first submission awards points
  * **Expected Result:** Second review submission does not create duplicate transaction, returns error or no-op
  * **Automated Test Type:** Unit test (LoyaltyPointsService - duplicate prevention), Integration test (Postman)

* **Test Case 1.12:** Tier multipliers applied to purchase points
  * **Description:** Gold tier customer (1.5x multiplier) makes $100 purchase - receives 150 points (not 100)
  * **Expected Result:** Transaction shows `points=150`, multiplier applied correctly
  * **Automated Test Type:** Unit test (LoyaltyPointsService - tier multiplier), Integration test (Postman)

* **Test Case 1.13:** Configurable point rates supported
  * **Description:** Admin changes `loyalty.points.purchase-rate` from 1 to 2 - new purchases award 2 points per $1
  * **Expected Result:** Configuration change reflected in point calculations
  * **Automated Test Type:** Unit test (LoyaltyPointsService - configurable rates)

---

### **AC 2: Points Redemption System**
**Given** a customer has accumulated loyalty points  
**When** they want to redeem points for rewards  
**Then** the system must allow redemption with proper validation

#### Test Cases:

* **Test Case 2.1:** Points redeemed for discount (100 points = $1 discount)
  * **Description:** Customer redeems 500 points - receives $5.00 discount
  * **Expected Result:** Transaction created with `points=-500`, `activity_type=REDEMPTION`, discount amount calculated as $5.00
  * **Automated Test Type:** Unit test (LoyaltyPointsService - redemption), Integration test (Postman)

* **Test Case 2.2:** Minimum redemption threshold enforced (500 points minimum)
  * **Description:** Customer with 400 points attempts redemption - system rejects with error
  * **Expected Result:** API returns error "Minimum redemption is 500 points", no transaction created
  * **Automated Test Type:** Unit test (LoyaltyPointsService - validation), Integration test (Postman)

* **Test Case 2.3:** Partial redemption allowed
  * **Description:** Customer has 1000 points, redeems 200 points - receives $2.00 discount, 800 points remaining
  * **Expected Result:** Transaction created with `points=-200`, balance updated to 800, discount $2.00
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman)

* **Test Case 2.4:** Redemption at checkout applies discount
  * **Description:** Customer redeems 500 points during checkout - discount applied to order total
  * **Expected Result:** Order total reduced by $5.00, points deducted, discount code or automatic application
  * **Automated Test Type:** Integration test (Postman - checkout flow), E2E test (Selenium)

* **Test Case 2.5:** Point balance displayed prominently on cart and checkout
  * **Description:** Customer views cart page - current point balance displayed prominently
  * **Expected Result:** UI shows "You have X points available" on cart and checkout pages
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 2.6:** Redemption history displayed
  * **Description:** Customer views account page - all redemptions shown with dates and amounts
  * **Expected Result:** Redemption transactions displayed in history with `transaction_type=REDEEMED`
  * **Automated Test Type:** Unit test (LoyaltyService - history), Integration test (Postman), E2E test (Selenium)

* **Test Case 2.7:** Insufficient points prevents redemption
  * **Description:** Customer with 300 points attempts to redeem 500 points - system rejects
  * **Expected Result:** API returns error "Insufficient points", no transaction created
  * **Automated Test Type:** Unit test (LoyaltyPointsService - validation), Integration test (Postman)

* **Test Case 2.8:** Maximum redemption limit enforced (max 50% of order value)
  * **Description:** Order total $100, customer attempts to redeem 6000 points ($60) - system limits to $50 (50%)
  * **Expected Result:** Redemption capped at $50, only 5000 points deducted
  * **Automated Test Type:** Unit test (LoyaltyPointsService - max redemption), Integration test (Postman)

* **Test Case 2.9:** Remaining balance displayed after redemption
  * **Description:** Customer has 1000 points, redeems 200 points - UI shows "800 points remaining"
  * **Expected Result:** Balance updated and displayed immediately after redemption
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 2.10:** Points deducted immediately upon redemption
  * **Description:** Customer redeems points - balance updated immediately, not after order completion
  * **Expected Result:** `LoyaltyAccount.current_points` updated immediately, transaction created
  * **Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman)

---

### **AC 3: Tiered Membership Levels**
**Given** customers accumulate points over time  
**When** they reach tier thresholds  
**Then** the system must automatically upgrade customers

#### Test Cases:

* **Test Case 3.1:** Bronze tier is default tier (0-999 points)
  * **Description:** New customer enrolled - automatically assigned Bronze tier
  * **Expected Result:** `LoyaltyAccount.current_tier=BRONZE`, `highest_tier_achieved=BRONZE`
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.2:** Automatic upgrade to Silver tier at 1,000 points
  * **Description:** Customer reaches 1,000 points - automatically upgraded to Silver tier
  * **Expected Result:** `current_tier=SILVER`, `highest_tier_achieved=SILVER`, tier upgrade notification sent
  * **Automated Test Type:** Unit test (LoyaltyTierService - upgrade logic), Integration test (Postman)

* **Test Case 3.3:** Silver tier benefits applied (1.25x points, free shipping on $25+)
  * **Description:** Silver tier customer makes purchase - earns 1.25x points, qualifies for free shipping on $25+ orders
  * **Expected Result:** Points calculated with 1.25x multiplier, shipping rule checks tier for free shipping
  * **Automated Test Type:** Unit test (LoyaltyTierService - benefits), Integration test (Postman)

* **Test Case 3.4:** Automatic upgrade to Gold tier at 2,500 points
  * **Description:** Customer reaches 2,500 points - automatically upgraded to Gold tier
  * **Expected Result:** `current_tier=GOLD`, tier upgrade notification sent
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.5:** Gold tier benefits applied (1.5x points, free shipping on all orders, early access)
  * **Description:** Gold tier customer - earns 1.5x points, free shipping on all orders, early access to sales
  * **Expected Result:** All Gold tier benefits active and applied correctly
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.6:** Automatic upgrade to Platinum tier at 5,000 points
  * **Description:** Customer reaches 5,000 points - automatically upgraded to Platinum tier
  * **Expected Result:** `current_tier=PLATINUM`, tier upgrade notification sent
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.7:** Platinum tier benefits applied (2x points, free shipping, early access, exclusive products, dedicated support)
  * **Description:** Platinum tier customer - all Platinum benefits active
  * **Expected Result:** 2x multiplier, free shipping, early access, exclusive products visible, dedicated support contact
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.8:** Tier upgrade notification email sent
  * **Description:** Customer upgraded to Silver tier - email sent with tier benefits information
  * **Expected Result:** Email sent via `LoyaltyEmailService.sendTierUpgradeEmail()`, email record created
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

* **Test Case 3.9:** Current tier displayed prominently on account page
  * **Description:** Customer views account page - tier badge displayed with current tier
  * **Expected Result:** UI shows tier badge (Bronze/Silver/Gold/Platinum) prominently
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 3.10:** Progress to next tier displayed
  * **Description:** Customer has 750 points (Bronze tier) - UI shows "750/1,000 points to Silver"
  * **Expected Result:** Progress bar or text shows points needed for next tier
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 3.11:** Tier benefits applied immediately upon upgrade
  * **Description:** Customer upgraded to Gold tier - next purchase immediately earns 1.5x points
  * **Expected Result:** Benefits active immediately, no delay or manual activation needed
  * **Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman)

* **Test Case 3.12:** Tier downgrade prevented (customers maintain highest tier achieved)
  * **Description:** Platinum customer redeems points, balance drops to 4,000 - tier remains Platinum
  * **Expected Result:** `current_tier=PLATINUM`, tier not downgraded based on point balance
  * **Automated Test Type:** Unit test (LoyaltyTierService - no downgrade), Integration test (Postman)

* **Test Case 3.13:** Tier benefits displayed clearly on account and checkout pages
  * **Description:** Customer views checkout page - tier benefits displayed (e.g., "Gold Member: Free Shipping")
  * **Expected Result:** Tier benefits visible on account and checkout pages
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

---

### **AC 4: Points Balance and History Display**
**Given** a customer is enrolled in the loyalty program  
**When** they view their account or loyalty dashboard  
**Then** the system must display comprehensive loyalty information

#### Test Cases:

* **Test Case 4.1:** Current point balance displayed prominently
  * **Description:** Customer views loyalty dashboard - current balance shown prominently
  * **Expected Result:** UI displays "X points" prominently at top of dashboard
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 4.2:** Current tier level displayed with badge/indicator
  * **Description:** Customer views dashboard - tier badge displayed (Bronze/Silver/Gold/Platinum)
  * **Expected Result:** Visual tier indicator displayed with appropriate styling
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 4.3:** Points needed to reach next tier displayed
  * **Description:** Bronze customer with 750 points - UI shows "250 points to Silver"
  * **Expected Result:** Progress indicator shows points remaining for next tier
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 4.4:** Recent point earning activities displayed (last 10 transactions)
  * **Description:** Customer views history - last 10 earning transactions displayed
  * **Expected Result:** API returns last 10 `EARNED` transactions, UI displays them chronologically
  * **Automated Test Type:** Unit test (LoyaltyService - history query), Integration test (Postman), E2E test (Selenium)

* **Test Case 4.5:** Recent point redemptions displayed (last 10 redemptions)
  * **Description:** Customer views history - last 10 redemption transactions displayed
  * **Expected Result:** API returns last 10 `REDEEMED` transactions, UI displays them
  * **Automated Test Type:** Unit test (LoyaltyService), Integration test (Postman), E2E test (Selenium)

* **Test Case 4.6:** Total lifetime points earned displayed
  * **Description:** Customer views dashboard - lifetime points earned shown
  * **Expected Result:** UI displays `lifetime_points_earned` value from `LoyaltyAccount`
  * **Automated Test Type:** Unit test (React component), Integration test (Postman)

* **Test Case 4.7:** Total lifetime points redeemed displayed
  * **Description:** Customer views dashboard - lifetime points redeemed shown
  * **Expected Result:** UI displays `lifetime_points_redeemed` value
  * **Automated Test Type:** Unit test (React component), Integration test (Postman)

* **Test Case 4.8:** Points expiration information displayed (if applicable)
  * **Description:** Customer has points expiring in 30 days - expiration warning displayed
  * **Expected Result:** UI shows "X points expiring in Y days" warning
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - expiration query), Integration test (Postman), E2E test (Selenium)

* **Test Case 4.9:** Upcoming point earning opportunities displayed
  * **Description:** Customer views dashboard - suggestions shown (e.g., "Submit a review to earn 50 points")
  * **Expected Result:** UI displays actionable opportunities to earn more points
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 4.10:** Display mobile-optimized and responsive
  * **Description:** Customer views dashboard on mobile device - layout responsive, no horizontal scrolling
  * **Expected Result:** Dashboard displays correctly on mobile screens, touch-friendly
  * **Automated Test Type:** Manual test (device), E2E test (Selenium - mobile viewport)

* **Test Case 4.11:** Display accessible from account page, cart page, and checkout page
  * **Description:** Customer can access loyalty information from multiple pages
  * **Expected Result:** Point balance visible on account, cart, and checkout pages
  * **Automated Test Type:** E2E test (Selenium)

* **Test Case 4.12:** Display updates in real-time when points earned or redeemed
  * **Description:** Customer earns points - dashboard updates immediately without refresh
  * **Expected Result:** Balance and history update in real-time via API polling or WebSocket
  * **Automated Test Type:** Unit test (React component - state updates), E2E test (Selenium)

---

### **AC 5: Enrollment and Account Integration**
**Given** a customer creates an account or has an existing account  
**When** they want to join the loyalty program  
**Then** the system must handle enrollment appropriately

#### Test Cases:

* **Test Case 5.1:** Automatic enrollment upon account creation
  * **Description:** New customer creates account - automatically enrolled in loyalty program
  * **Expected Result:** `LoyaltyAccount` created with `enrollment_source=AUTO`, welcome points awarded
  * **Automated Test Type:** Unit test (LoyaltyService - enrollment), Integration test (Postman)

* **Test Case 5.2:** Opt-out option available during enrollment
  * **Description:** New customer creates account - option to opt-out of loyalty program presented
  * **Expected Result:** UI shows opt-out checkbox, if checked, no `LoyaltyAccount` created
  * **Automated Test Type:** Unit test (UserService - enrollment), E2E test (Selenium)

* **Test Case 5.3:** Enrollment invitation displayed on account page for existing customers
  * **Description:** Existing customer (not enrolled) views account page - enrollment invitation shown
  * **Expected Result:** UI displays "Join our loyalty program" invitation with benefits
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 5.4:** Welcome message and initial points displayed upon enrollment
  * **Description:** Customer enrolls - welcome message shown with "You've earned 100 welcome points!"
  * **Expected Result:** UI displays welcome message and initial points notification
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 5.5:** Loyalty status integrated into account dashboard
  * **Description:** Customer views account dashboard - loyalty status section displayed
  * **Expected Result:** Account dashboard includes loyalty program section with balance and tier
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 5.6:** Customers can opt-out of loyalty program (with confirmation)
  * **Description:** Enrolled customer opts out - confirmation dialog shown, opt-out processed
  * **Expected Result:** `LoyaltyAccount.is_active=false`, confirmation message displayed
  * **Automated Test Type:** Unit test (LoyaltyService - opt-out), Integration test (Postman), E2E test (Selenium)

* **Test Case 5.7:** Customers can re-enroll after opting out
  * **Description:** Previously opted-out customer re-enrolls - new `LoyaltyAccount` created or existing reactivated
  * **Expected Result:** Customer can re-enroll, welcome points awarded again
  * **Automated Test Type:** Unit test (LoyaltyService - re-enrollment), Integration test (Postman), E2E test (Selenium)

* **Test Case 5.8:** Loyalty program benefits and terms displayed clearly
  * **Description:** Customer views enrollment page - benefits and terms displayed
  * **Expected Result:** UI shows program benefits, point rates, tier thresholds, terms and conditions
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 5.9:** Account creation required for enrollment (guests cannot enroll)
  * **Description:** Guest user attempts to enroll - system requires account creation first
  * **Expected Result:** Error message "Please create an account to join the loyalty program"
  * **Automated Test Type:** Unit test (LoyaltyService - validation), Integration test (Postman), E2E test (Selenium)

* **Test Case 5.10:** Enrollment date and status stored
  * **Description:** Customer enrolls on Jan 15, 2024 - `enrollment_date` stored correctly
  * **Expected Result:** `LoyaltyAccount.enrollment_date` populated with enrollment timestamp
  * **Automated Test Type:** Unit test (LoyaltyService), Integration test (Postman)

* **Test Case 5.11:** Enrollment source tracked (AUTO, MANUAL, REFERRAL)
  * **Description:** Customer enrolls via referral link - `enrollment_source=REFERRAL` stored
  * **Expected Result:** Enrollment source correctly tracked in database
  * **Automated Test Type:** Unit test (LoyaltyService), Integration test (Postman)

* **Test Case 5.12:** Welcome email sent with program details
  * **Description:** Customer enrolls - welcome email sent with program overview and benefits
  * **Expected Result:** Email sent via `LoyaltyEmailService.sendWelcomeEmail()`, email contains program details
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

---

### **AC 6: Points Expiration and Management**
**Given** customers accumulate points over time  
**When** points are earned  
**Then** the system must manage point expiration appropriately

#### Test Cases:

* **Test Case 6.1:** Expiration dates set for points (12 months of inactivity)
  * **Description:** Customer earns 100 points on Jan 15, 2024 - expiration date set to Jan 15, 2025
  * **Expected Result:** Transaction `expiration_date` populated with date 12 months from earning date
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - expiration date calculation)

* **Test Case 6.2:** Expiration warnings displayed (30 days, 7 days, 1 day before)
  * **Description:** Points expiring in 30 days - warning displayed on account page
  * **Expected Result:** UI shows "500 points expiring in 30 days" warning
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - warning query), Integration test (Postman), E2E test (Selenium)

* **Test Case 6.3:** Email notifications sent before expiration (30 days, 7 days, 1 day)
  * **Description:** Points expiring in 30 days - email warning sent
  * **Expected Result:** Email sent via `LoyaltyEmailService.sendPointExpirationWarning()`, email record created
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

* **Test Case 6.4:** FIFO (First In, First Out) method used for expiration
  * **Description:** Customer has points from Jan, Feb, Mar - oldest points (Jan) expire first
  * **Expected Result:** Expired points are oldest first, FIFO order maintained
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - FIFO logic)

* **Test Case 6.5:** Activity extends expiration (earning or redeeming points resets expiration)
  * **Description:** Customer earns points on Jan 15, expiration Jan 15, 2025 - earns more points on Feb 1, expiration extended
  * **Expected Result:** Activity resets expiration clock, expiration date recalculated
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - expiration reset)

* **Test Case 6.6:** Expiration information displayed clearly on account page
  * **Description:** Customer views account page - expiration warnings and dates displayed
  * **Expected Result:** UI shows expiration information clearly
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 6.7:** Points used in pending redemptions do not expire
  * **Description:** Customer redeems 500 points, order pending - those points not expired even if expiration date reached
  * **Expected Result:** Points in pending redemption excluded from expiration processing
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - pending redemption exclusion)

* **Test Case 6.8:** Expired points automatically removed from balance
  * **Description:** Points expire on Jan 15, 2025 - scheduled job removes them on Jan 16
  * **Expected Result:** Expired points removed from `current_points`, expiration transaction created
  * **Automated Test Type:** Unit test (LoyaltyExpirationService - expiration processing), Integration test (Postman)

* **Test Case 6.9:** Expired points logged in point history
  * **Description:** Points expire - transaction created with `transaction_type=EXPIRED`, `points=-X`
  * **Expected Result:** Expiration transaction recorded in history
  * **Automated Test Type:** Unit test (LoyaltyExpirationService), Integration test (Postman)

* **Test Case 6.10:** Expiration notification sent to customer
  * **Description:** Points expire - notification email sent to customer
  * **Expected Result:** Email sent informing customer of expired points
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

* **Test Case 6.11:** Expiration does not affect tier status (tiers based on lifetime points)
  * **Description:** Platinum customer's points expire, balance drops to 0 - tier remains Platinum
  * **Expected Result:** `current_tier` unchanged, tier based on `lifetime_points_earned`, not `current_points`
  * **Automated Test Type:** Unit test (LoyaltyTierService - tier not affected by expiration)

* **Test Case 6.12:** Scheduled job runs daily to check expirations
  * **Description:** Scheduled job runs daily at midnight - checks for expiring points and sends warnings
  * **Expected Result:** Job executes daily, processes expirations and warnings
  * **Automated Test Type:** Unit test (LoyaltyExpirationScheduler - scheduling), Integration test (Postman)

---

### **AC 7: Referral Program Integration**
**Given** a customer is enrolled in the loyalty program  
**When** they refer a new customer  
**Then** the system must track referrals and award points

#### Test Cases:

* **Test Case 7.1:** Unique referral code/link generated for each customer
  * **Description:** Customer enrolled - unique referral code generated (e.g., "ABC12345")
  * **Expected Result:** `LoyaltyAccount.referral_code` populated with unique code, code stored in database
  * **Automated Test Type:** Unit test (LoyaltyReferralService - code generation), Integration test (Postman)

* **Test Case 7.2:** Referrals tracked and referred customer purchases monitored
  * **Description:** Referred customer makes purchase - referral tracked in `loyalty_referrals` table
  * **Expected Result:** `LoyaltyReferral` record created with `status=PENDING` or `COMPLETED`
  * **Automated Test Type:** Unit test (LoyaltyReferralService - tracking), Integration test (Postman)

* **Test Case 7.3:** Referral points awarded when referred customer makes first purchase
  * **Description:** Referred customer completes first purchase - referrer receives 100 points
  * **Expected Result:** Referrer receives points, `LoyaltyReferral.points_awarded=true`, `status=COMPLETED`
  * **Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman), E2E test (Selenium)

* **Test Case 7.4:** Bonus points awarded to referred customer (50 welcome bonus points)
  * **Description:** New customer enrolls via referral link - receives 50 bonus points in addition to 100 welcome points
  * **Expected Result:** Referred customer receives 150 total points (100 welcome + 50 referral bonus)
  * **Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman), E2E test (Selenium)

* **Test Case 7.5:** Referral link/code displayed on account page
  * **Description:** Customer views account page - referral code and link displayed
  * **Expected Result:** UI shows referral code and shareable link
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 7.6:** Referral statistics displayed (number of referrals, successful referrals, points earned)
  * **Description:** Customer has referred 5 customers, 3 completed purchases - statistics shown
  * **Expected Result:** UI displays "5 referrals, 3 successful, 300 points earned"
  * **Automated Test Type:** Unit test (LoyaltyReferralService - statistics), Integration test (Postman), E2E test (Selenium)

* **Test Case 7.7:** Notification sent when referral makes purchase
  * **Description:** Referred customer completes first purchase - referrer receives notification
  * **Expected Result:** Email or in-app notification sent to referrer
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

* **Test Case 7.8:** Multiple referral methods supported (email, link, code)
  * **Description:** Customer can share referral via email link, direct link, or code - all methods tracked
  * **Expected Result:** All referral methods work correctly, tracking identifies source method
  * **Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman), E2E test (Selenium)

* **Test Case 7.9:** Self-referrals prevented
  * **Description:** Customer attempts to use own referral code - system rejects
  * **Expected Result:** Error message "Cannot refer yourself", no referral created
  * **Automated Test Type:** Unit test (LoyaltyReferralService - validation), Integration test (Postman)

* **Test Case 7.10:** Duplicate referrals prevented (same customer referred multiple times)
  * **Description:** Customer attempts to refer same user twice - second attempt rejected
  * **Expected Result:** Error message or no-op, only one referral record created
  * **Automated Test Type:** Unit test (LoyaltyReferralService - duplicate prevention), Integration test (Postman)

* **Test Case 7.11:** Referral source tracked accurately
  * **Description:** Customer uses referral link vs code - source tracked correctly
  * **Expected Result:** `LoyaltyReferral` record shows correct referral method/source
  * **Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman)

* **Test Case 7.12:** Referral success rate displayed
  * **Description:** Customer has 10 referrals, 5 successful - success rate shown as 50%
  * **Expected Result:** UI displays referral success rate percentage
  * **Automated Test Type:** Unit test (LoyaltyReferralService - statistics), Integration test (Postman), E2E test (Selenium)

---

### **AC 8: Mobile-Optimized Loyalty Experience**
**Given** a customer accesses loyalty features on a mobile device  
**When** viewing points balance, redeeming points, or checking tier status  
**Then** the interface must be mobile-friendly

#### Test Cases:

* **Test Case 8.1:** Interface fully responsive and optimized for mobile screens
  * **Description:** Customer views loyalty dashboard on iPhone - layout responsive, no horizontal scrolling
  * **Expected Result:** Dashboard displays correctly on mobile screens, content fits viewport
  * **Automated Test Type:** Manual test (iOS device), E2E test (Selenium - mobile viewport)

* **Test Case 8.2:** Touch-friendly buttons and controls (44x44px minimum)
  * **Description:** Point redemption buttons meet minimum touch target size
  * **Expected Result:** All interactive elements have minimum 44x44px touch target
  * **Automated Test Type:** Manual test (device), Visual regression test (CSS verification)

* **Test Case 8.3:** Easy to read without horizontal scrolling
  * **Description:** Customer views point balance on mobile - all text readable without scrolling horizontally
  * **Expected Result:** Content wraps appropriately, no horizontal scroll required
  * **Automated Test Type:** Manual test (device), E2E test (Selenium - mobile viewport)

* **Test Case 8.4:** Fast-loading (<2 seconds on 4G connection)
  * **Description:** Loyalty dashboard loads on 4G connection - page load time <2 seconds
  * **Expected Result:** Dashboard loads within 2 seconds on 4G network
  * **Automated Test Type:** Performance test (Lighthouse mobile, network throttling)

* **Test Case 8.5:** Accessible via mobile-optimized account page
  * **Description:** Customer accesses account page on mobile - loyalty section accessible
  * **Expected Result:** Loyalty features accessible from mobile account page
  * **Automated Test Type:** Manual test (device), E2E test (Selenium)

* **Test Case 8.6:** Point redemption supported during mobile checkout
  * **Description:** Customer redeems points during mobile checkout - redemption works smoothly
  * **Expected Result:** Point redemption functional on mobile checkout flow
  * **Automated Test Type:** E2E test (Selenium - mobile checkout)

* **Test Case 8.7:** Point balance displayed prominently on mobile cart/checkout
  * **Description:** Customer views mobile cart - point balance visible prominently
  * **Expected Result:** Balance displayed clearly on mobile cart and checkout pages
  * **Automated Test Type:** Manual test (device), E2E test (Selenium)

* **Test Case 8.8:** One-tap point redemption actions
  * **Description:** Customer redeems points on mobile - one tap to redeem, no complex forms
  * **Expected Result:** Redemption process simplified for mobile, minimal taps required
  * **Automated Test Type:** Manual test (device), E2E test (Selenium)

* **Test Case 8.9:** Tier progress displayed clearly on mobile screens
  * **Description:** Customer views tier progress on mobile - progress bar and text clearly visible
  * **Expected Result:** Tier progress displayed clearly on mobile screens
  * **Automated Test Type:** Manual test (device), E2E test (Selenium)

* **Test Case 8.10:** Mobile push notifications for point earnings and tier upgrades
  * **Description:** Customer earns points or upgrades tier - push notification sent to mobile device
  * **Expected Result:** Push notifications delivered for point earnings and tier upgrades
  * **Automated Test Type:** Integration test (Postman - push notification service), Manual test (device)

---

### **AC 9: Admin Dashboard and Management**
**Given** administrators need to manage the loyalty program  
**When** they access the admin dashboard  
**Then** the system must provide comprehensive management tools

#### Test Cases:

* **Test Case 9.1:** Total enrolled members count displayed
  * **Description:** Admin views dashboard - total enrolled members count shown
  * **Expected Result:** Dashboard shows count of active `LoyaltyAccount` records
  * **Automated Test Type:** Unit test (Admin service - statistics), Integration test (Postman)

* **Test Case 9.2:** Total points issued and redeemed displayed
  * **Description:** Admin views dashboard - total points issued and redeemed shown
  * **Expected Result:** Dashboard shows sum of all `EARNED` transactions and `REDEEMED` transactions
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.3:** Tier distribution displayed (Bronze, Silver, Gold, Platinum counts)
  * **Description:** Admin views dashboard - tier distribution chart or counts shown
  * **Expected Result:** Dashboard shows count of members in each tier
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.4:** Point earning activity by type displayed (purchases, reviews, referrals)
  * **Description:** Admin views analytics - breakdown of point earning by activity type shown
  * **Expected Result:** Dashboard shows points earned by `activity_type` (PURCHASE, REVIEW, REFERRAL, etc.)
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.5:** Redemption rate and average redemption amount displayed
  * **Description:** Admin views analytics - redemption statistics shown
  * **Expected Result:** Dashboard shows redemption rate (redemptions/total members) and average redemption amount
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.6:** Customer lifetime value by tier displayed
  * **Description:** Admin views analytics - average customer lifetime value by tier shown
  * **Expected Result:** Dashboard shows CLV metrics grouped by tier
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.7:** Points expiration statistics displayed
  * **Description:** Admin views analytics - points expiration statistics shown
  * **Expected Result:** Dashboard shows points expiring soon, expired points count, expiration trends
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.8:** Program performance metrics displayed (enrollment rate, engagement rate, retention rate)
  * **Description:** Admin views dashboard - key performance metrics shown
  * **Expected Result:** Dashboard displays enrollment rate, engagement rate, retention rate
  * **Automated Test Type:** Unit test (Admin service), Integration test (Postman)

* **Test Case 9.9:** Manual point balance adjustment with audit log
  * **Description:** Admin adjusts customer's point balance - adjustment logged in audit trail
  * **Expected Result:** Point adjustment creates `ADMIN_ADJUSTMENT` transaction, audit log entry created
  * **Automated Test Type:** Unit test (Admin service - adjustment), Integration test (Postman)

* **Test Case 9.10:** Point rates and tier thresholds configurable
  * **Description:** Admin changes `loyalty.points.purchase-rate` from 1 to 2 - configuration updated
  * **Expected Result:** Configuration changes saved and applied to new point calculations
  * **Automated Test Type:** Unit test (Admin service - configuration), Integration test (Postman)

* **Test Case 9.11:** Promotional point earning events creatable
  * **Description:** Admin creates "Double Points Weekend" promotion - system applies 2x multiplier
  * **Expected Result:** Promotional events created and applied to point calculations
  * **Automated Test Type:** Unit test (Admin service - promotions), Integration test (Postman)

* **Test Case 9.12:** Bulk operations for point adjustments supported
  * **Description:** Admin adjusts points for 100 customers - bulk operation processes all
  * **Expected Result:** All adjustments processed, audit log entries created for each
  * **Automated Test Type:** Unit test (Admin service - bulk operations), Integration test (Postman)

* **Test Case 9.13:** Audit logs maintained for all point transactions
  * **Description:** Admin views audit log - all point transactions logged with admin ID
  * **Expected Result:** Audit log shows all transactions with timestamps and admin identifiers
  * **Automated Test Type:** Unit test (Admin service - audit logging), Integration test (Postman)

* **Test Case 9.14:** Reports exportable (CSV/Excel)
  * **Description:** Admin exports loyalty program report - CSV file generated
  * **Expected Result:** Report exported in CSV/Excel format with all data
  * **Automated Test Type:** Unit test (Admin service - export), Integration test (Postman)

* **Test Case 9.15:** Trends and analytics charts displayed
  * **Description:** Admin views dashboard - charts showing trends over time displayed
  * **Expected Result:** Dashboard includes visual charts for enrollment, redemption, tier distribution trends
  * **Automated Test Type:** Unit test (Admin service - analytics), Integration test (Postman)

---

### **AC 10: Integration with Existing Features**
**Given** the loyalty program is operational  
**When** customers interact with existing platform features  
**Then** the system must integrate seamlessly

#### Test Cases:

* **Test Case 10.1:** Order System integration - points awarded automatically upon order completion
  * **Description:** Customer completes order - points awarded automatically via `OrderService` integration
  * **Expected Result:** Order completion triggers `LoyaltyPointsService.awardPurchasePoints()`, points awarded
  * **Automated Test Type:** Unit test (OrderService integration), Integration test (Postman), E2E test (Selenium)

* **Test Case 10.2:** Order cancellation reverses points
  * **Description:** Customer cancels order - points awarded for that order reversed
  * **Expected Result:** Negative transaction created to reverse points, balance updated
  * **Automated Test Type:** Unit test (OrderService - cancellation), Integration test (Postman)

* **Test Case 10.3:** Order refund reverses points proportionally
  * **Description:** Customer receives partial refund - points reversed proportionally
  * **Expected Result:** Points reversed based on refund percentage
  * **Automated Test Type:** Unit test (OrderService - refund), Integration test (Postman)

* **Test Case 10.4:** Review System integration - points awarded when reviews submitted
  * **Description:** Customer submits product review - points awarded via review system integration
  * **Expected Result:** Review submission triggers `LoyaltyPointsService.awardReviewPoints()`, 50 points awarded
  * **Automated Test Type:** Unit test (ReviewService integration), Integration test (Postman), E2E test (Selenium)

* **Test Case 10.5:** Account System integration - loyalty status displayed on account page
  * **Description:** Customer views account page - loyalty status section displayed
  * **Expected Result:** Account page includes loyalty dashboard section
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 10.6:** Cart/Checkout integration - point balance displayed and redemption allowed
  * **Description:** Customer views cart - point balance displayed, redemption option available
  * **Expected Result:** Cart and checkout pages show point balance and redemption form
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 10.7:** Email System integration - notifications sent for point earnings, redemptions, tier upgrades, expirations
  * **Description:** Customer earns points - email notification sent via email system
  * **Expected Result:** Email sent via `LoyaltyEmailService`, email delivery confirmed
  * **Automated Test Type:** Unit test (LoyaltyEmailService), Integration test (Postman)

* **Test Case 10.8:** Analytics integration - loyalty program impact tracked (retention, AOV, lifetime value)
  * **Description:** Analytics system tracks loyalty program metrics
  * **Expected Result:** Analytics dashboard shows loyalty program impact on retention, AOV, CLV
  * **Automated Test Type:** Integration test (Postman - analytics API), Manual test (analytics dashboard)

* **Test Case 10.9:** Shipping Service integration - tier benefits applied to shipping costs
  * **Description:** Silver tier customer - free shipping on $25+ orders applied via shipping service
  * **Expected Result:** `ShippingRuleService` checks customer tier, applies free shipping when eligible
  * **Automated Test Type:** Unit test (ShippingRuleService - tier integration), Integration test (Postman), E2E test (Selenium)

* **Test Case 10.10:** Integration works seamlessly without disrupting existing workflows
  * **Description:** Customer completes order - loyalty points awarded without affecting order creation flow
  * **Expected Result:** Order creation completes normally, points awarded asynchronously or synchronously without errors
  * **Automated Test Type:** Integration test (Postman), E2E test (Selenium)

* **Test Case 10.11:** Data consistency maintained across systems
  * **Description:** Points awarded for order - order and loyalty data remain consistent
  * **Expected Result:** No data inconsistencies between order system and loyalty system
  * **Automated Test Type:** Integration test (Postman - data verification), E2E test (Selenium)

* **Test Case 10.12:** Rollback support if point transactions fail
  * **Description:** Order created but point transaction fails - system supports rollback
  * **Expected Result:** Failed point transaction doesn't prevent order completion, error logged
  * **Automated Test Type:** Unit test (LoyaltyPointsService - error handling), Integration test (Postman)

* **Test Case 10.13:** Edge cases handled (cancelled orders, returned items, refunds)
  * **Description:** Order cancelled, returned, or refunded - points handled correctly
  * **Expected Result:** Points reversed appropriately for cancellations, returns, refunds
  * **Automated Test Type:** Unit test (OrderService - edge cases), Integration test (Postman)

---

## 3. 👍 Positive Test Cases ("Happy Path")

### HP-1: Complete Happy Path Flow - Enrollment to Redemption
**Description:** New customer enrolls, earns points through purchases, upgrades tiers, and redeems points
1. New customer creates account - automatically enrolled, receives 100 welcome points
2. Customer makes $100 purchase - earns 100 points (Bronze tier, 1x multiplier)
3. Customer reaches 1,000 points - automatically upgraded to Silver tier
4. Customer makes $50 purchase - earns 62.5 points (Silver tier, 1.25x multiplier)
5. Customer views loyalty dashboard - sees balance, tier, progress to Gold
6. Customer redeems 500 points at checkout - receives $5.00 discount
7. Order completed with discount applied, points deducted
**Automated Test Type:** E2E test (Selenium) - **Primary E2E test**

### HP-2: Tier Upgrade Journey
**Description:** Customer progresses through all tiers with benefits applied
1. New customer enrolled (Bronze tier)
2. Customer makes purchases, reaches 1,000 points - upgraded to Silver
3. Silver tier benefits applied (1.25x points, free shipping on $25+)
4. Customer continues purchasing, reaches 2,500 points - upgraded to Gold
5. Gold tier benefits applied (1.5x points, free shipping on all orders)
6. Customer reaches 5,000 points - upgraded to Platinum
7. Platinum tier benefits applied (2x points, all benefits)
**Automated Test Type:** Unit test (LoyaltyTierService), Integration test (Postman), E2E test (Selenium)

### HP-3: Referral Program Success
**Description:** Customer refers friend, both earn points
1. Customer A shares referral code with Customer B
2. Customer B enrolls using referral code - receives 150 points (100 welcome + 50 referral bonus)
3. Customer B makes first purchase - Customer A receives 100 referral points
4. Both customers see referral statistics updated
**Automated Test Type:** Unit test (LoyaltyReferralService), Integration test (Postman), E2E test (Selenium)

### HP-4: Point Redemption at Checkout
**Description:** Customer redeems points during checkout process
1. Customer has 1,000 points, cart total $100
2. Customer proceeds to checkout
3. Customer sees point balance and redemption option
4. Customer redeems 500 points - receives $5.00 discount
5. Order total updated to $95.00
6. Customer completes order with discount applied
**Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman), E2E test (Selenium)

### HP-5: Multiple Activity Types Earn Points
**Description:** Customer earns points from various activities
1. Customer makes purchase - earns purchase points
2. Customer submits review - earns 50 review points
3. Customer shares product on social media - earns 25 social share points
4. Customer refers friend who makes purchase - earns 100 referral points
5. All activities tracked in point history
**Automated Test Type:** Unit test (LoyaltyPointsService), Integration test (Postman), E2E test (Selenium)

---

## 4. 👎 Negative Test Cases ("Sad Path")

### NP-1: Insufficient Points for Redemption
**Description:** Customer attempts to redeem more points than available
**Expected Result:** API returns error "Insufficient points", redemption rejected, no transaction created
**Automated Test Type:** Unit test (LoyaltyPointsService - validation), Integration test (Postman), E2E test (Selenium)

### NP-2: Redemption Below Minimum Threshold
**Description:** Customer attempts to redeem 200 points (below 500 minimum)
**Expected Result:** API returns error "Minimum redemption is 500 points", redemption rejected
**Automated Test Type:** Unit test (LoyaltyPointsService - validation), Integration test (Postman), E2E test (Selenium)

### NP-3: Duplicate Point Award Prevention
**Description:** Customer attempts to earn points for same activity twice (e.g., same review)
**Expected Result:** Second attempt rejected, no duplicate transaction created, error logged
**Automated Test Type:** Unit test (LoyaltyPointsService - duplicate prevention), Integration test (Postman)

### NP-4: Self-Referral Prevention
**Description:** Customer attempts to use own referral code
**Expected Result:** System rejects self-referral, error message displayed
**Automated Test Type:** Unit test (LoyaltyReferralService - validation), Integration test (Postman), E2E test (Selenium)

### NP-5: Invalid Referral Code
**Description:** Customer attempts to enroll with invalid/non-existent referral code
**Expected Result:** Enrollment proceeds without referral bonus, error logged
**Automated Test Type:** Unit test (LoyaltyReferralService - validation), Integration test (Postman)

### NP-6: Feature Toggle Disabled
**Description:** Loyalty program feature toggle disabled - system handles gracefully
**Expected Result:** API returns null/empty responses, no errors thrown, feature disabled message shown
**Automated Test Type:** Unit test (LoyaltyService - feature toggle), Integration test (Postman)

### NP-7: Order Cancellation After Points Awarded
**Description:** Customer cancels order after points already awarded
**Expected Result:** Points reversed via negative transaction, balance updated correctly
**Automated Test Type:** Unit test (OrderService - cancellation), Integration test (Postman)

### NP-8: Expired Points Attempted Redemption
**Description:** Customer attempts to redeem expired points
**Expected Result:** Expired points excluded from available balance, redemption uses only valid points
**Automated Test Type:** Unit test (LoyaltyExpirationService), Integration test (Postman)

### NP-9: Invalid Point Adjustment by Admin
**Description:** Admin attempts to adjust points with invalid amount (negative beyond balance)
**Expected Result:** Adjustment rejected, error message, audit log entry created
**Automated Test Type:** Unit test (Admin service - validation), Integration test (Postman)

### NP-10: Concurrent Point Transactions
**Description:** Multiple simultaneous point transactions for same customer
**Expected Result:** Database transactions prevent race conditions, all transactions processed correctly
**Automated Test Type:** Unit test (LoyaltyPointsService - concurrency), Integration test (Postman)

---

## 5. 边界 Edge Cases

### EC-1: Customer at Exact Tier Threshold (1,000 points)
**Description:** Customer has exactly 1,000 points - should be Silver tier
**Expected Result:** Tier calculated correctly, customer upgraded to Silver
**Automated Test Type:** Unit test (LoyaltyTierService - boundary), Integration test (Postman)

### EC-2: Customer at 999 Points (Just Below Threshold)
**Description:** Customer has 999 points - should remain Bronze tier
**Expected Result:** Tier remains Bronze, no upgrade triggered
**Automated Test Type:** Unit test (LoyaltyTierService - boundary), Integration test (Postman)

### EC-3: Zero Point Balance
**Description:** Customer has 0 points - system handles gracefully
**Expected Result:** Balance displays as 0, redemption disabled, no errors
**Automated Test Type:** Unit test (LoyaltyService), Integration test (Postman), E2E test (Selenium)

### EC-4: Very Large Point Balance (100,000+ points)
**Description:** Customer accumulates 100,000+ points - system handles large numbers
**Expected Result:** Balance displays correctly, calculations accurate, no overflow errors
**Automated Test Type:** Unit test (LoyaltyService - large numbers), Integration test (Postman)

### EC-5: Points Expiring Exactly on Expiration Date
**Description:** Points expire exactly on expiration date - scheduled job processes correctly
**Expected Result:** Points expired on correct date, expiration transaction created
**Automated Test Type:** Unit test (LoyaltyExpirationService - boundary timing)

### EC-6: Multiple Points Expiring Simultaneously
**Description:** Customer has multiple point batches expiring on same date - all expired correctly
**Expected Result:** All expiring points processed, FIFO order maintained
**Automated Test Type:** Unit test (LoyaltyExpirationService - batch expiration)

### EC-7: Referral Code Collision (Extremely Unlikely)
**Description:** Two referral codes generated with same UUID (collision)
**Expected Result:** System generates new code if collision detected, no duplicate codes
**Automated Test Type:** Unit test (LoyaltyReferralService - code uniqueness)

### EC-8: Customer Redeems Maximum Allowed (50% of Order)
**Description:** Order $100, customer redeems 6,000 points ($60) - capped at $50
**Expected Result:** Redemption limited to $50, only 5,000 points deducted
**Automated Test Type:** Unit test (LoyaltyPointsService - max redemption), Integration test (Postman)

### EC-9: Points Earned During Redemption Transaction
**Description:** Customer earns points while redeeming points simultaneously
**Expected Result:** Both transactions processed correctly, no race conditions
**Automated Test Type:** Unit test (LoyaltyPointsService - concurrency), Integration test (Postman)

### EC-10: Customer Opts Out and Re-enrolls
**Description:** Customer opts out, then re-enrolls - welcome points awarded again
**Expected Result:** Re-enrollment creates new account or reactivates existing, welcome points awarded
**Automated Test Type:** Unit test (LoyaltyService - re-enrollment), Integration test (Postman), E2E test (Selenium)

### EC-11: Birthday Bonus on Month Boundary
**Description:** Customer's birthday is January 31, system awards bonus on February 1 (next month)
**Expected Result:** Birthday bonus awarded correctly, month boundary handled
**Automated Test Type:** Unit test (LoyaltyExpirationService - birthday logic)

### EC-12: Anniversary Bonus on Leap Year
**Description:** Customer enrolled on February 29, 2024 - anniversary on February 28, 2025
**Expected Result:** Anniversary bonus awarded correctly, leap year handled
**Automated Test Type:** Unit test (LoyaltyExpirationService - anniversary logic)

---

## 6. 🔄 Regression Risks

### Risk 1: Order Service Integration
**Area:** OrderService and order creation/completion logic  
**Risk:** Loyalty point awarding might interfere with existing order creation or cause performance issues  
**Test Cases:**
- Verify order creation still works correctly without loyalty program
- Verify points awarded correctly after order completion
- Verify order cancellation reverses points correctly
- Verify order refund reverses points proportionally
- Verify order creation performance not degraded by loyalty integration
**Automated Test Type:** Unit test (OrderService), Integration test (Postman), E2E test (Selenium)

### Risk 2: Shipping Service Integration
**Area:** ShippingRuleService and shipping cost calculations  
**Risk:** Tier-based free shipping might interfere with existing shipping rules or calculations  
**Test Cases:**
- Verify shipping cost calculations still work correctly for non-loyalty customers
- Verify tier-based free shipping applied correctly (Silver: $25+, Gold/Platinum: all orders)
- Verify shipping rules remain consistent with loyalty tier benefits
- Verify shipping cost calculator component displays correctly with tier benefits
**Automated Test Type:** Unit test (ShippingRuleService), Integration test (Postman), E2E test (Selenium)

### Risk 3: Cart State Management (AppContext)
**Area:** AppContext state management for cart items and totals  
**Risk:** Point redemption might cause cart state inconsistencies or calculation errors  
**Test Cases:**
- Verify cart total calculations remain accurate with point redemption
- Verify point redemption updates cart state correctly
- Verify cart items remain synchronized after redemption
- Verify adding/removing items after redemption works correctly
**Automated Test Type:** Unit test (React context), E2E test (Selenium)

### Risk 4: User Service Integration
**Area:** UserService for user creation and account management  
**Risk:** Automatic enrollment might interfere with user creation flow or cause errors  
**Test Cases:**
- Verify user creation still works correctly without loyalty program
- Verify automatic enrollment doesn't cause user creation failures
- Verify user account page displays correctly with loyalty integration
- Verify user deletion handles loyalty account cleanup
**Automated Test Type:** Unit test (UserService), Integration test (Postman), E2E test (Selenium)

### Risk 5: Review System Integration (Future)
**Area:** Review system for product reviews  
**Risk:** Review point awarding might interfere with review submission or cause duplicate awards  
**Test Cases:**
- Verify review submission still works correctly
- Verify points awarded only once per review
- Verify review deletion handles point reversal (if applicable)
**Automated Test Type:** Unit test (ReviewService integration), Integration test (Postman), E2E test (Selenium)

### Risk 6: Email Service Integration
**Area:** EmailService for sending notifications  
**Risk:** Loyalty email notifications might interfere with existing email sending or cause performance issues  
**Test Cases:**
- Verify existing email sending still works correctly
- Verify loyalty emails sent correctly (tier upgrades, expiration warnings, welcome)
- Verify email service performance not degraded by loyalty emails
- Verify email templates render correctly
**Automated Test Type:** Unit test (EmailService), Integration test (Postman)

### Risk 7: Scheduled Job System
**Area:** Spring scheduling system and other scheduled jobs  
**Risk:** Loyalty expiration scheduler might interfere with other scheduled jobs or cause resource contention  
**Test Cases:**
- Verify other scheduled jobs still run correctly
- Verify loyalty expiration scheduler doesn't cause database connection pool exhaustion
- Verify scheduler handles errors gracefully without affecting other jobs
**Automated Test Type:** Integration test (Postman), Performance test

---

## 7. 📊 Automated Test Strategy (Test Pyramid)

### Unit Tests (Foundation - 70% coverage target)

#### Backend Unit Tests:
- ❌ **LoyaltyServiceTest** - **CREATE NEW** - Test enrollment, opt-out, tier calculations
  - Test cases: AC3.1-AC3.13, AC5.1-AC5.12, HP2, EC1, EC2, EC3, EC10
- ❌ **LoyaltyPointsServiceTest** - **CREATE NEW** - Test point earning and redemption logic
  - Test cases: AC1.1-AC1.13, AC2.1-AC2.10, HP1, HP4, HP5, NP1, NP2, NP3, NP7, NP8, EC3, EC4, EC8, EC9
- ❌ **LoyaltyTierServiceTest** - **CREATE NEW** - Test tier upgrade logic and tier benefits
  - Test cases: AC3.1-AC3.13, HP2, EC1, EC2
- ❌ **LoyaltyReferralServiceTest** - **CREATE NEW** - Test referral code generation and tracking
  - Test cases: AC7.1-AC7.12, HP3, NP4, NP5, EC7
- ❌ **LoyaltyExpirationServiceTest** - **CREATE NEW** - Test expiration logic and FIFO method
  - Test cases: AC6.1-AC6.12, EC5, EC6, EC11, EC12
- ❌ **LoyaltyEmailServiceTest** - **CREATE NEW** - Test email generation
  - Test cases: AC3.8, AC5.12, AC6.3, AC6.10, AC7.7
- ❌ **LoyaltyControllerTest** - **CREATE NEW** - Test API endpoints
  - Test cases: AC4.1-AC4.12, AC5.1-AC5.12, AC7.1-AC7.12, NP1, NP2, NP6

#### Frontend Unit Tests:
- ❌ **LoyaltyDashboard.test.tsx** - **CREATE NEW** - Test component rendering and interactions
  - Test cases: AC4.1-AC4.12, HP1, HP2
- ❌ **LoyaltyBalance.test.tsx** - **CREATE NEW** - Test balance display component
  - Test cases: AC2.5, AC4.1, AC4.11, HP1, HP4
- ❌ **LoyaltyTierBadge.test.tsx** - **CREATE NEW** - Test tier badge component
  - Test cases: AC3.9, AC3.10, AC4.2, HP2
- ❌ **PointRedemptionForm.test.tsx** - **CREATE NEW** - Test redemption form logic
  - Test cases: AC2.1-AC2.10, HP4, NP1, NP2
- ❌ **ReferralSection.test.tsx** - **CREATE NEW** - Test referral section component
  - Test cases: AC7.5, AC7.6, AC7.12, HP3
- ❌ **loyaltyService.test.ts** - **CREATE NEW** - Test API service layer
  - Test cases: AC4.1-AC4.12, NP6, EC3

**Current Status:**
- ❌ Backend unit tests: **MISSING** (all need to be created)
- ❌ Frontend unit tests: **MISSING** (all need to be created)

---

### Integration Tests (Middle Layer - 20% coverage)

#### Postman/API Tests:
- ❌ **GET /api/loyalty/balance** - **CREATE NEW** - Get current point balance and tier
  - Test cases: AC4.1, AC4.2, AC4.3, HP1, EC3
- ❌ **GET /api/loyalty/history** - **CREATE NEW** - Get point transaction history
  - Test cases: AC4.4, AC4.5, AC4.6, AC4.7, HP5
- ❌ **POST /api/loyalty/redeem** - **CREATE NEW** - Redeem points for discount
  - Test cases: AC2.1-AC2.10, HP4, NP1, NP2, EC8
- ❌ **GET /api/loyalty/referral-code** - **CREATE NEW** - Get user's referral code
  - Test cases: AC7.1, AC7.5, HP3
- ❌ **POST /api/loyalty/enroll** - **CREATE NEW** - Manual enrollment
  - Test cases: AC5.3, AC5.7, AC5.11, EC10
- ❌ **POST /api/loyalty/opt-out** - **CREATE NEW** - Opt out of loyalty program
  - Test cases: AC5.6, EC10
- ❌ **GET /api/loyalty/tier-benefits** - **CREATE NEW** - Get tier benefits information
  - Test cases: AC3.13, HP2
- ✅ **Integration with OrderService** - Verify points awarded on order completion
  - Test cases: AC1.1, AC10.1, AC10.2, AC10.3, HP1, NP7, Risk 1
- ✅ **Integration with ShippingRuleService** - Verify tier-based free shipping
  - Test cases: AC3.3, AC3.5, AC3.7, AC10.9, Risk 2
- ✅ **Integration with UserService** - Verify automatic enrollment
  - Test cases: AC5.1, AC5.9, AC10.5, Risk 4
- ✅ **Integration with EmailService** - Verify email notifications
  - Test cases: AC3.8, AC5.12, AC6.3, AC6.10, AC7.7, AC10.7, Risk 6

**Current Status:**
- ❌ Postman tests: **MISSING** (all need to be created)

---

### E2E Tests (Top Layer - 10% coverage)

#### Selenium E2E Tests:
- ❌ **SCRUM15LoyaltyEnrollmentTest.java** - **CREATE NEW** - Test enrollment flow
  - Test cases: AC5.1-AC5.12, HP1 (enrollment part), EC10
- ❌ **SCRUM15PointsEarningTest.java** - **CREATE NEW** - Test points earned on purchase
  - Test cases: AC1.1, AC1.8, AC1.9, AC10.1, HP1, HP5
- ❌ **SCRUM15PointRedemptionTest.java** - **CREATE NEW** - Test point redemption at checkout
  - Test cases: AC2.1-AC2.10, AC10.6, HP4, NP1, NP2, Risk 3
- ❌ **SCRUM15TierUpgradeTest.java** - **CREATE NEW** - Test tier upgrade flow
  - Test cases: AC3.1-AC3.13, AC10.9, HP2, EC1, EC2, Risk 2
- ❌ **SCRUM15ReferralTest.java** - **CREATE NEW** - Test referral code and points
  - Test cases: AC7.1-AC7.12, HP3, NP4, NP5
- ❌ **SCRUM15MobileLoyaltyTest.java** - **CREATE NEW** - Test mobile responsiveness
  - Test cases: AC8.1-AC8.10, AC4.10, Risk 3

**Current Status:**
- ❌ E2E tests: **MISSING** (all need to be created)

---

## 8. 📋 Test Execution Checklist

### Phase 1: Unit Tests (Foundation)
- [ ] Backend: LoyaltyServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyPointsServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyTierServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyReferralServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyExpirationServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyEmailServiceTest - **CREATE NEW** - All test cases pass
- [ ] Backend: LoyaltyControllerTest - **CREATE NEW** - All test cases pass
- [ ] Frontend: LoyaltyDashboard.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: LoyaltyBalance.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: LoyaltyTierBadge.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: PointRedemptionForm.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: ReferralSection.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: loyaltyService.test.ts - **CREATE NEW** - Service tests

### Phase 2: Integration Tests
- [ ] Postman: Add loyalty endpoint test collection
- [ ] Postman: Verify API contract and response format
- [ ] Postman: Test point earning (AC1)
- [ ] Postman: Test point redemption (AC2)
- [ ] Postman: Test tier upgrades (AC3)
- [ ] Postman: Test balance and history (AC4)
- [ ] Postman: Test enrollment (AC5)
- [ ] Postman: Test expiration (AC6)
- [ ] Postman: Test referral program (AC7)
- [ ] Postman: Test error scenarios (NP1-NP10)
- [ ] Postman: Test edge cases (EC1-EC12)
- [ ] Postman: Test regression risks (Risk 1-7)

### Phase 3: E2E Tests
- [ ] Selenium: Create SCRUM15LoyaltyEnrollmentTest.java
- [ ] Selenium: Create SCRUM15PointsEarningTest.java
- [ ] Selenium: Create SCRUM15PointRedemptionTest.java
- [ ] Selenium: Create SCRUM15TierUpgradeTest.java
- [ ] Selenium: Create SCRUM15ReferralTest.java
- [ ] Selenium: Create SCRUM15MobileLoyaltyTest.java
- [ ] Selenium: Happy path test (HP-1)
- [ ] Selenium: Tier upgrade test (HP-2)
- [ ] Selenium: Referral test (HP-3)
- [ ] Selenium: Redemption test (HP-4)
- [ ] Selenium: Regression tests (Risk 1-7)

### Phase 4: Performance Tests
- [ ] Load test: Point earning API responds within 500ms
- [ ] Load test: Point redemption API responds within 500ms
- [ ] Load test: Tier calculation performance
- [ ] Load test: Expiration scheduler processes efficiently
- [ ] Load test: System handles 1,000+ concurrent loyalty operations

### Phase 5: Manual Testing
- [ ] Mobile responsiveness (AC8.1-AC8.10)
- [ ] Cross-browser compatibility (Chrome, Firefox, Safari, Edge)
- [ ] Accessibility (WCAG 2.1 AA compliance)
- [ ] Email template rendering (tier upgrades, expiration warnings, welcome)
- [ ] Admin dashboard usability (AC9)

---

## 9. 🎯 Priority Test Cases for Immediate Implementation

### High Priority (Must Have Before Production):
1. **HP-1:** Happy path E2E test (Selenium) - Enrollment → Purchase → Points → Tier Upgrade → Redemption
2. **AC1.1:** Purchase points awarded correctly (Unit + Integration)
3. **AC2.1-AC2.4:** Point redemption functionality (Unit + Integration + E2E)
4. **AC3.2-AC3.4:** Tier upgrade logic (Unit + Integration + E2E)
5. **AC5.1:** Automatic enrollment (Unit + Integration + E2E)
6. **AC10.1:** Order integration - points awarded (Unit + Integration + E2E)
7. **NP1-NP3:** Error handling (Unit + Integration)

### Medium Priority (Should Have):
1. **AC1.2-AC1.7:** Other point earning activities (Unit + Integration)
2. **AC3.5-AC3.7:** Gold and Platinum tier benefits (Unit + Integration)
3. **AC4.1-AC4.9:** Balance and history display (Unit + E2E)
4. **AC6.1-AC6.8:** Point expiration (Unit + Integration)
5. **AC7.1-AC7.6:** Referral program (Unit + Integration + E2E)
6. **AC10.2-AC10.3:** Order cancellation/refund handling (Unit + Integration)
7. **Risk 1-4:** Regression tests (E2E)

### Low Priority (Nice to Have):
1. **AC8.1-AC8.10:** Mobile optimization (Manual + E2E)
2. **AC9.1-AC9.15:** Admin dashboard (Unit + Integration)
3. **AC6.9-AC6.12:** Advanced expiration features (Unit + Integration)
4. **EC1-EC12:** Additional edge cases (Unit + Integration)
5. **Performance tests:** Load testing for high volume

---

## 10. 📝 Test Implementation Notes

### Missing Test Coverage Identified:
1. ❌ **All Backend Unit Tests:** Need to create LoyaltyServiceTest, LoyaltyPointsServiceTest, LoyaltyTierServiceTest, LoyaltyReferralServiceTest, LoyaltyExpirationServiceTest, LoyaltyEmailServiceTest, LoyaltyControllerTest
2. ❌ **All Frontend Unit Tests:** Need to create LoyaltyDashboard.test.tsx, LoyaltyBalance.test.tsx, LoyaltyTierBadge.test.tsx, PointRedemptionForm.test.tsx, ReferralSection.test.tsx, loyaltyService.test.ts
3. ❌ **All Integration Tests:** Need to add loyalty endpoints to Postman collection
4. ❌ **All E2E Tests:** Need to create SCRUM15* test classes
5. ⚠️ **Admin Dashboard Tests:** Admin endpoints may be Phase 2, tests can be created when implemented

### Test Data Requirements:
- Test users: New users, enrolled users, opted-out users, users at various tier levels
- Test point balances: 0 points, below threshold, at threshold, above threshold, very large balances
- Test tiers: Bronze, Silver, Gold, Platinum customers
- Test referral codes: Valid codes, invalid codes, expired codes
- Test orders: Various order values, cancelled orders, refunded orders
- Test expiration dates: Points expiring soon, expired points, points with various expiration dates

### Test Environment Setup:
- Mock OrderService for unit tests (don't create real orders)
- Mock EmailService for unit tests (don't send real emails)
- Mock UserService for enrollment tests
- Test database with known loyalty account data
- Selenium test environment with UI running
- Scheduled job testing setup (mock time for expiration tests)

### Component Structure (Expected):
- **New Backend Module:** `loyalty/` - Domain module with service, controller, repository
- **New Database Tables:** `loyalty_accounts`, `loyalty_transactions`, `loyalty_referrals`
- **New Scheduled Job:** `LoyaltyExpirationScheduler` - Processes expirations and birthday/anniversary bonuses
- **New Frontend Service:** `loyaltyService.ts` - API calls for loyalty operations
- **New Frontend Components:** `LoyaltyDashboard.tsx`, `LoyaltyBalance.tsx`, `LoyaltyTierBadge.tsx`, `PointRedemptionForm.tsx`, `ReferralSection.tsx`

### Critical Test Scenarios:
1. **Point Earning Accuracy (AC1):** Critical - points must be awarded correctly for all activity types
2. **Point Redemption (AC2):** Critical - redemption must work correctly at checkout
3. **Tier Upgrades (AC3):** Critical - tier upgrades must be automatic and benefits applied correctly
4. **Order Integration (AC10.1-AC10.3):** Critical - points must be awarded/reversed correctly with orders
5. **Data Consistency (AC10.11):** Critical - loyalty data must remain consistent with order data

---

**Test Plan Created:** 2025-12-02  
**Test Plan Version:** 1.0  
**Story Status:** Analysis (Not Yet Implemented)  
**Next Steps:** 
1. Review test plan with development team
2. Create test cases as implementation begins (Phase 1: Core Loyalty System)
3. Execute tests as features are completed
4. Update JIRA story with test plan
