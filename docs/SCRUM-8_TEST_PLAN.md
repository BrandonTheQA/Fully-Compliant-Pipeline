# Test Plan: SCRUM-8 - Intelligent Shipping Cost Optimization Recommendations

## 1. 📝 Story Summary & Core Objective

**Story:** Intelligent Shipping Cost Optimization Recommendations to Reduce Cart Abandonment and Increase AOV

**Core Objective:** Provide intelligent, actionable product recommendations to help users reach free shipping thresholds, reducing cart abandonment and increasing Average Order Value (AOV).

**User Problem Solved:** Users currently see "Add $X more for free shipping" but don't know WHICH products to add. This feature actively guides users by recommending specific products that help them qualify for free shipping, removing decision paralysis and information overload.

---

## 2. ✅ Acceptance Criteria (AC) Test Cases

### **AC 1: Shipping Optimization Recommendation Engine**
**Given** a user has items in their cart below the free shipping threshold  
**When** the cart page is displayed  
**Then** the system must analyze the cart contents and shipping rules and generate at least 3-5 intelligent product recommendations

#### Test Cases:

* **Test Case 1.1:** Cart below threshold generates recommendations
  * **Description:** Cart with $35.00 total (below $50.00 US threshold) should generate 3-5 product recommendations
  * **Expected Result:** API returns `RecommendationResponse` with 3-5 `OptimizationPath` objects, each containing products that would help reach threshold
  * **Automated Test Type:** Unit test (Service), Integration test (Postman), E2E test (Selenium)

* **Test Case 1.2:** Recommendations ranked by category similarity
  * **Description:** Cart contains products from "Electronics" category - recommendations should prioritize products in same category
  * **Expected Result:** Products in "Electronics" category appear first in recommendations (higher score)
  * **Automated Test Type:** Unit test (Service scoring logic)

* **Test Case 1.3:** Recommendations ranked by price proximity
  * **Description:** Cart needs $15.00 to reach threshold - products priced around $15.00 should rank higher than products priced $50.00+
  * **Expected Result:** Products priced closest to remaining amount ($15.00) appear first
  * **Automated Test Type:** Unit test (Service scoring logic)

* **Test Case 1.4:** Products already in cart are filtered out
  * **Description:** Cart contains product IDs ["prod1", "prod2"] - recommendations should not include these products
  * **Expected Result:** All recommended products have IDs different from cart items
  * **Automated Test Type:** Unit test (Service), Integration test (Postman)

* **Test Case 1.5:** Products with zero inventory are filtered out
  * **Description:** Product with `quantity: 0` should not appear in recommendations
  * **Expected Result:** All recommended products have `quantity > 0`
  * **Automated Test Type:** Unit test (Service)

* **Test Case 1.6:** Each recommendation displays required fields
  * **Description:** Verify each `RecommendedProduct` contains: name, price, savings message, imageUrl
  * **Expected Result:** All recommended products have non-null name, price > 0, non-empty savingsMessage, imageUrl (even if placeholder)
  * **Automated Test Type:** Unit test (DTO validation), Integration test (Postman)

* **Test Case 1.7:** API response time < 500ms (95th percentile)
  * **Description:** Load test with 100 concurrent requests - 95% should complete within 500ms
  * **Expected Result:** 95th percentile response time < 500ms
  * **Automated Test Type:** Performance test (JMeter/Gatling)

---

### **AC 2: Multiple Optimization Paths Display**
**Given** a user's cart is below the free shipping threshold  
**When** shipping optimization recommendations are displayed  
**Then** the system must show at least 2-3 different optimization paths

#### Test Cases:

* **Test Case 2.1:** Single path displayed when only one recommendation exists
  * **Description:** Cart state where only one product recommendation exists - UI should display directly (no tabs)
  * **Expected Result:** Single product card displayed without tab interface
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 2.2:** Multiple paths displayed with tabs when multiple recommendations exist
  * **Description:** Cart state generates 3+ recommendations - UI should show tabs for each path
  * **Expected Result:** Tab interface displayed with tabs for each optimization path (e.g., "Single Product", "Bundle", "Category")
  * **Automated Test Type:** Unit test (React component), E2E test (Selenium)

* **Test Case 2.3:** Tab switching works correctly
  * **Description:** User clicks different tabs - correct path content should be displayed
  * **Expected Result:** Clicking tab changes displayed products to match selected path
  * **Automated Test Type:** Unit test (React component interactions), E2E test (Selenium)

* **Test Case 2.4:** Each path shows total cost, savings, and final cart total
  * **Description:** Verify each `OptimizationPath` displays: totalCost, savingsAmount, and calculated final cart total
  * **Expected Result:** Path cards show "Add All ($X.XX)", "Save $Y.YY on shipping", and final cart total calculation
  * **Automated Test Type:** Unit test (React component rendering), Integration test (Postman - verify DTO structure)

* **Test Case 2.5:** "Add All" button adds all products from bundle path (when Phase 2 implemented)
  * **Description:** Bundle path with 2-3 products - clicking "Add All" should add all products to cart
  * **Expected Result:** All products from bundle path added to cart in single action
  * **Automated Test Type:** E2E test (Selenium) - **Note:** Currently Phase 1 only supports single product paths

* **Test Case 2.6:** One-click action adds recommended product
  * **Description:** Clicking "Add to Cart" on a recommended product should add it to cart
  * **Expected Result:** Product added to cart, recommendations recalculate
  * **Automated Test Type:** Unit test (React component callbacks), E2E test (Selenium)

---

### **AC 3: Dynamic Recommendation Updates**
**Given** a user is viewing shipping optimization recommendations  
**When** the user adds, removes, or modifies items in their cart  
**Then** recommendations must update in real-time (within 200ms)

#### Test Cases:

* **Test Case 3.1:** Add item to cart triggers recommendation update
  * **Description:** User adds product to cart - recommendations should refresh within 200ms (debounced)
  * **Expected Result:** New recommendations generated based on updated cart total, displayed within 200ms
  * **Automated Test Type:** Unit test (React useEffect/hooks), E2E test (Selenium with timing verification)

* **Test Case 3.2:** Remove item from cart triggers recommendation update
  * **Description:** User removes product from cart - recommendations should recalculate
  * **Expected Result:** Recommendations updated to reflect new cart state
  * **Automated Test Type:** Unit test (React state management), E2E test (Selenium)

* **Test Case 3.3:** Update quantity triggers recommendation update
  * **Description:** User changes product quantity from 1 to 2 - cart total changes, recommendations update
  * **Expected Result:** Recommendations recalculated based on new cart total
  * **Automated Test Type:** Unit test (React hooks), E2E test (Selenium)

* **Test Case 3.4:** Reach free shipping threshold removes recommendations
  * **Description:** Cart total reaches threshold (e.g., $50.00) - recommendations should disappear
  * **Expected Result:** Recommendations component not rendered when `qualifiesForFreeShipping: true`
  * **Automated Test Type:** Unit test (React conditional rendering), E2E test (Selenium)

* **Test Case 3.5:** Remove item below threshold shows recommendations again
  * **Description:** Cart at $50.00, user removes item bringing total to $35.00 - recommendations reappear
  * **Expected Result:** Recommendations component rendered again with new recommendations
  * **Automated Test Type:** Unit test (React state), E2E test (Selenium)

* **Test Case 3.6:** Update happens within 200ms (debounced)
  * **Description:** Rapid cart changes (5 changes in 100ms) - recommendations should update once after 200ms debounce
  * **Expected Result:** Only one API call made, recommendations update after 200ms debounce period
  * **Automated Test Type:** Unit test (React debounce logic), Performance test (timing verification)

---

### **AC 4: Cart Modification Recommendations** ⚠️ **NOT IMPLEMENTED (Phase 3)**
**Note:** This AC is for Phase 3 and is not currently implemented. Tests should be created when feature is implemented.

#### Test Cases (Future):

* **Test Case 4.1:** Replace item suggestion generated when applicable
* **Test Case 4.2:** Remove+add suggestion shows cost comparison
* **Test Case 4.3:** Quantity increase suggestion displayed
* **Test Case 4.4:** One-click apply optimization works

---

### **AC 5: Personalized Recommendation Ranking** ⚠️ **PARTIAL (Basic Category Matching Only)**
**Given** a user is viewing shipping optimization recommendations  
**When** recommendations are displayed  
**Then** products must be ranked by personalization factors

#### Test Cases:

* **Test Case 5.1:** Products in same category as cart items ranked higher
  * **Description:** Cart contains "Electronics" products - recommendations should prioritize "Electronics" products
  * **Expected Result:** Same-category products appear first in recommendations list
  * **Automated Test Type:** Unit test (Service scoring - category match gets +10 points)

* **Test Case 5.2:** Guest users see relevant recommendations (no personalization)
  * **Description:** Guest user (no userId) - recommendations should still be relevant based on category and price
  * **Expected Result:** Recommendations generated based on cart categories and price proximity (fallback logic)
  * **Automated Test Type:** Unit test (Service), Integration test (Postman - no userId parameter)

* **Test Case 5.3:** Logged-in users receive personalized recommendations (when Phase 3 implemented)
  * **Description:** User with purchase history - recommendations should consider user's past purchases
  * **Expected Result:** Products similar to user's purchase history ranked higher
  * **Automated Test Type:** Unit test (Service - future), E2E test (Selenium - future)
  * **Status:** ⚠️ **NOT IMPLEMENTED - Phase 3**

---

### **AC 6: Mobile-Optimized Recommendation Display**
**Given** a user accesses the cart page on a mobile device  
**When** shipping optimization recommendations are displayed  
**Then** recommendations must be mobile-friendly

#### Test Cases:

* **Test Case 6.1:** Mobile layout displays correctly (iOS Safari)
  * **Description:** View cart page on iOS Safari - recommendations should render properly without horizontal scrolling
  * **Expected Result:** All recommendation content visible without horizontal scroll, responsive layout
  * **Automated Test Type:** Manual test (device), E2E test (Selenium with mobile viewport)

* **Test Case 6.2:** Mobile layout displays correctly (Android Chrome)
  * **Description:** View cart page on Android Chrome - recommendations should render properly
  * **Expected Result:** Responsive layout, no horizontal scrolling required
  * **Automated Test Type:** Manual test (device), E2E test (Selenium with mobile viewport)

* **Test Case 6.3:** Touch targets are 44x44px minimum
  * **Description:** "Add to Cart" buttons and tabs must meet minimum touch target size
  * **Expected Result:** All interactive elements have minimum 44x44px touch target
  * **Automated Test Type:** Manual test (device), Visual regression test (CSS verification)

* **Test Case 6.4:** Swipeable carousel works on mobile
  * **Description:** Multiple recommendations displayed in carousel - user can swipe between them
  * **Expected Result:** Swipe gestures work to navigate between recommendations
  * **Automated Test Type:** Manual test (device), E2E test (Selenium touch actions)

* **Test Case 6.5:** Page load impact < 300ms on mobile networks
  * **Description:** Recommendation component should not significantly impact page load time on 3G/4G
  * **Expected Result:** Recommendations load within 300ms additional load time on mobile networks
  * **Automated Test Type:** Performance test (Lighthouse mobile, network throttling)

* **Test Case 6.6:** Cart actions remain accessible
  * **Description:** Recommendations should not obstruct "View Cart" or "Checkout" buttons
  * **Expected Result:** All critical cart actions visible and accessible with recommendations displayed
  * **Automated Test Type:** Manual test (device), E2E test (Selenium)

---

### **AC 7: Recommendation Analytics and Tracking** ⚠️ **NOT IMPLEMENTED (Phase 4)**
**Note:** This AC is for Phase 4 and is not currently implemented. Tests should be created when feature is implemented.

#### Test Cases (Future):

* **Test Case 7.1:** Recommendation impressions tracked
* **Test Case 7.2:** Click-through rate on recommendations tracked
* **Test Case 7.3:** Add-to-cart events from recommendations tracked
* **Test Case 7.4:** Conversion rate (checkout completion) tracked
* **Test Case 7.5:** Analytics dashboard displays data

---

### **AC 8: Performance and Scalability**
**Given** the recommendation engine is processing cart optimization requests  
**When** multiple users access recommendations simultaneously  
**Then** the system must handle load and perform within SLA

#### Test Cases:

* **Test Case 8.1:** Response time < 500ms for 95% of requests
  * **Description:** Load test with 100 concurrent users - 95th percentile response time should be < 500ms
  * **Expected Result:** 95% of requests complete within 500ms
  * **Automated Test Type:** Performance test (JMeter/Gatling)

* **Test Case 8.2:** System handles 500+ concurrent requests without degradation
  * **Description:** Load test with 500 concurrent users making recommendation requests
  * **Expected Result:** All requests complete successfully, no errors, response times remain acceptable
  * **Automated Test Type:** Load test (JMeter/Gatling)

* **Test Case 8.3:** Cache reduces response time for similar cart states
  * **Description:** Same cart state requested twice - second request should be faster (cached)
  * **Expected Result:** Cached requests return within 50ms (vs 200-500ms for uncached)
  * **Automated Test Type:** Unit test (Service caching), Performance test (cache hit rate)

* **Test Case 8.4:** Graceful degradation when service unavailable
  * **Description:** ProductService throws exception - recommendations should return empty list, not crash
  * **Expected Result:** API returns empty recommendations gracefully, frontend hides recommendations component
  * **Automated Test Type:** Unit test (Service error handling), Integration test (Postman - mock service failure)

* **Test Case 8.5:** Logging configured for monitoring
  * **Description:** Recommendation requests and errors should be logged for monitoring
  * **Expected Result:** All requests logged with timestamp, cartTotal, region; errors logged with stack trace
  * **Automated Test Type:** Manual test (log verification), Integration test (verify logging behavior)

---

## 3. 👍 Positive Test Cases ("Happy Path")

### HP-1: Complete Happy Path Flow
**Description:** User with cart below threshold sees recommendations, adds recommended product, qualifies for free shipping
1. User adds products to cart totaling $35.00 (below $50.00 US threshold)
2. Recommendations appear showing 3-5 products
3. User clicks "Add to Cart" on a $20.00 recommended product
4. Cart total becomes $55.00
5. Recommendations disappear (qualifies for free shipping)
6. Free shipping banner shows "You've qualified for FREE shipping!"
**Automated Test Type:** E2E test (Selenium) - **Primary E2E test**

### HP-2: Multiple Recommendations Displayed
**Description:** Cart generates multiple optimization paths - all paths displayed correctly
1. Cart state generates 3+ single product recommendations
2. All recommendations displayed in tabs
3. User can switch between tabs
4. Each tab shows correct products
**Automated Test Type:** Unit test (React component), E2E test (Selenium)

### HP-3: Recommendations Update After Cart Change
**Description:** Recommendations update dynamically when cart changes
1. Cart at $35.00 shows recommendations
2. User adds item, cart becomes $40.00
3. Recommendations update within 200ms
4. New recommendations reflect updated cart total
**Automated Test Type:** Unit test (React hooks), E2E test (Selenium)

### HP-4: Different Regions Show Correct Thresholds
**Description:** US region ($50 threshold) vs CA region ($75 threshold) show appropriate recommendations
1. US user: Cart at $45.00 - recommendations for $5.00+ products
2. CA user: Cart at $70.00 - recommendations for $5.00+ products
3. Each region shows correct remaining amount
**Automated Test Type:** Unit test (Service - region handling), Integration test (Postman)

---

## 4. 👎 Negative Test Cases ("Sad Path")

### NP-1: Empty Cart Shows No Recommendations
**Description:** Cart with $0.00 total should not show recommendations (or show appropriate message)
**Expected Result:** Recommendations not displayed or empty recommendations returned
**Automated Test Type:** Unit test (Service - empty cart handling), Integration test (Postman)

### NP-2: Invalid Cart Total Handled Gracefully
**Description:** API called with negative cart total or null - should default to $0.00
**Expected Result:** Service treats null/negative as $0.00, returns appropriate recommendations
**Automated Test Type:** Unit test (Service - input validation), Integration test (Postman)

### NP-3: Product Service Failure Handled Gracefully
**Description:** ProductService throws exception - recommendations should return empty list, not crash
**Expected Result:** API returns empty recommendations, frontend handles gracefully (no recommendations shown)
**Automated Test Type:** Unit test (Service - error handling), Integration test (Postman - mock failure)

### NP-4: API Timeout Handled Gracefully
**Description:** Recommendation API times out - frontend should hide recommendations, not show error
**Expected Result:** Recommendations component not rendered, no error message to user
**Automated Test Type:** Unit test (Frontend - API error handling), E2E test (Selenium - simulate timeout)

### NP-5: Invalid Region Handled Gracefully
**Description:** API called with invalid region code - should fallback to default region
**Expected Result:** Invalid region falls back to default ("US"), recommendations still generated
**Automated Test Type:** Unit test (Service - region fallback), Integration test (Postman)

### NP-6: Cart Items Parameter Malformed
**Description:** `cartItems` parameter contains invalid format - should parse gracefully or ignore
**Expected Result:** Service handles malformed cartItems (e.g., extra commas, empty strings) gracefully
**Automated Test Type:** Unit test (Controller - input parsing), Integration test (Postman)

### NP-7: No Products Available to Recommend
**Description:** All products are out of stock or already in cart - should return empty recommendations
**Expected Result:** Empty recommendations returned, frontend shows nothing (not error)
**Automated Test Type:** Unit test (Service - no candidates), Integration test (Postman)

---

## 5. 边界 Edge Cases

### EC-1: Cart Total Exactly at Threshold
**Description:** Cart total exactly equals threshold (e.g., $50.00 for US) - should not show recommendations
**Expected Result:** `qualifiesForFreeShipping: true`, empty recommendations
**Automated Test Type:** Unit test (Service - boundary condition), Integration test (Postman)

### EC-2: Cart Total $0.01 Below Threshold
**Description:** Cart total at $49.99 (US threshold $50.00) - should show recommendations for $0.01+ products
**Expected Result:** Recommendations generated, even if only products that slightly exceed threshold
**Automated Test Type:** Unit test (Service - boundary condition), Integration test (Postman)

### EC-3: Cart Total Very Large (Above Threshold)
**Description:** Cart total at $1000.00 (well above threshold) - should not show recommendations
**Expected Result:** Qualifies for free shipping, no recommendations
**Automated Test Type:** Unit test (Service - large values), Integration test (Postman)

### EC-4: All Products Already in Cart
**Description:** Cart contains all available products - no products left to recommend
**Expected Result:** Empty recommendations returned
**Automated Test Type:** Unit test (Service - all products filtered), Integration test (Postman)

### EC-5: Products with Null/Empty Values
**Description:** Product catalog contains products with null name, price, or category - should filter out
**Expected Result:** Only products with valid data (non-null name, price > 0) appear in recommendations
**Automated Test Type:** Unit test (Service - null safety checks)

### EC-6: Very Long Product Names
**Description:** Product with 200+ character name - should display without breaking layout
**Expected Result:** Product name truncated or wrapped, UI remains usable
**Automated Test Type:** Unit test (React component - text truncation), Manual test

### EC-7: Very High Product Prices
**Description:** Product priced at $10,000 - should not appear in recommendations if far above remaining amount
**Expected Result:** Products priced > 1.5x remaining amount filtered out
**Automated Test Type:** Unit test (Service - price range filtering)

### EC-8: Cart Items List Contains Duplicates
**Description:** `cartItems` parameter contains duplicate product IDs - should handle correctly
**Expected Result:** Duplicates ignored, recommendations generated correctly
**Automated Test Type:** Unit test (Controller - input parsing)

### EC-9: Region Case Sensitivity
**Description:** API called with "us", "US", "Us" - all should work correctly
**Expected Result:** Region normalized to uppercase, correct recommendations returned
**Automated Test Type:** Unit test (Service - region normalization), Integration test (Postman)

### EC-10: Concurrent Cart Updates
**Description:** User rapidly adds/removes items multiple times - recommendations should debounce correctly
**Expected Result:** Only one API call made after 200ms debounce, recommendations update correctly
**Automated Test Type:** Unit test (React debounce logic), E2E test (Selenium)

---

## 6. 🔄 Regression Risks

### Risk 1: Shipping Cost Calculator Integration
**Area:** Shipping Cost Calculator component and `/api/shipping/cost` endpoint  
**Risk:** Recommendations feature might interfere with existing shipping cost calculations  
**Test Cases:**
- Verify shipping cost calculator still works correctly when recommendations are displayed
- Verify both components can coexist on same page without conflicts
- Verify cart total calculations are consistent between components
**Automated Test Type:** Integration test (Postman), E2E test (Selenium)

### Risk 2: Shipping Banner Component
**Area:** Shipping Banner component that displays threshold messages  
**Risk:** Recommendations might conflict with banner display or cause layout issues  
**Test Cases:**
- Verify shipping banner still displays correctly with recommendations
- Verify both components show consistent "remaining amount" values
- Verify layout doesn't break with both components visible
**Automated Test Type:** Unit test (React component rendering), E2E test (Selenium)

### Risk 3: Cart State Management (AppContext)
**Area:** AppContext state management for cart items and totals  
**Risk:** Recommendation updates might cause cart state inconsistencies  
**Test Cases:**
- Verify adding recommended product updates cart state correctly
- Verify cart total calculations remain accurate after recommendations
- Verify cart items list remains synchronized
**Automated Test Type:** Unit test (React context), E2E test (Selenium)

### Risk 4: Product Service Integration
**Area:** ProductService and product catalog API  
**Risk:** Recommendation service might cause performance issues or conflicts with product queries  
**Test Cases:**
- Verify ProductService.getAllProducts() still works correctly
- Verify product queries don't slow down when recommendations are active
- Verify product inventory remains accurate
**Automated Test Type:** Integration test (Postman), Performance test

### Risk 5: Shipping Rule Service
**Area:** ShippingRuleService for threshold and cost calculations  
**Risk:** Recommendation service might interfere with shipping rule lookups  
**Test Cases:**
- Verify shipping threshold calculations still accurate
- Verify shipping cost calculations unchanged
- Verify region detection works correctly
**Automated Test Type:** Unit test (ShippingRuleService), Integration test (Postman)

---

## 7. 📊 Automated Test Strategy (Test Pyramid)

### Unit Tests (Foundation - 70% coverage target)

#### Backend Unit Tests:
- ✅ **ShippingRecommendationServiceTest** - Test recommendation generation logic
  - Test cases: AC1.2, AC1.3, AC1.4, AC1.5, AC3.1-3.5, AC5.1, AC8.3, AC8.4, EC1-EC9
- ✅ **ShippingRecommendationControllerTest** - Test API endpoint
  - Test cases: AC1.1, AC1.4, AC8.4, NP2, NP5, NP6, EC8, EC9
- **DTO Tests** - Validate data structures
  - Test cases: AC1.6, AC2.4

#### Frontend Unit Tests:
- ❌ **ShippingRecommendations.test.tsx** - **MISSING** - Test component rendering and interactions
  - Test cases: AC2.1, AC2.2, AC2.3, AC2.4, AC3.1-3.6, AC6.3, HP2, HP3
- ✅ **shippingService.test.ts** - Test API service layer (already exists)
  - Test cases: NP1, NP3, NP4, EC1

**Current Status:**
- ✅ Backend unit tests: **COMPLETE** (Service and Controller)
- ❌ Frontend unit tests: **MISSING** (ShippingRecommendations component needs tests)

---

### Integration Tests (Middle Layer - 20% coverage)

#### Postman/API Tests:
- ✅ **GET /api/shipping/recommendations** - Verify API contract
  - Test cases: AC1.1, AC1.4, AC1.6, AC2.4, AC5.2, HP4, NP1, NP2, NP5, EC1, EC2
- ✅ **Integration with ProductService** - Verify service integration
  - Test cases: NP3, NP7, Risk 4
- ✅ **Integration with ShippingRuleService** - Verify rule service integration
  - Test cases: Risk 5

**Current Status:**
- ⚠️ Postman tests: **PARTIAL** - Need to add recommendation endpoint tests

---

### E2E Tests (Top Layer - 10% coverage)

#### Selenium E2E Tests:
- ❌ **SCRUM8ShippingRecommendationsTest.java** - **MISSING** - Happy path E2E test
  - Test case: **HP-1** (Primary happy path: cart below threshold → see recommendations → add product → qualify for free shipping)
  - Test cases: AC2.1, AC2.2, AC2.3, AC3.1, AC3.4, AC3.5, AC6.4, AC6.6, HP2, HP3, Risk 1, Risk 2, Risk 3

**Current Status:**
- ⚠️ E2E tests: **PARTIAL** - Basic recommendation checks exist in `E2EWorkflowTest.java` but no dedicated test class

---

## 8. 📋 Test Execution Checklist

### Phase 1: Unit Tests (Foundation)
- [ ] Backend: ShippingRecommendationServiceTest - All test cases pass
- [ ] Backend: ShippingRecommendationControllerTest - All test cases pass
- [ ] Frontend: ShippingRecommendations.test.tsx - **CREATE NEW** - Component tests
- [ ] Frontend: shippingService.test.ts - Extend existing tests for recommendations

### Phase 2: Integration Tests
- [ ] Postman: Add recommendation endpoint test collection
- [ ] Postman: Verify API contract and response format
- [ ] Postman: Test error scenarios (NP1-NP7)
- [ ] Postman: Test edge cases (EC1-EC10)

### Phase 3: E2E Tests
- [ ] Selenium: Create SCRUM8ShippingRecommendationsTest.java
- [ ] Selenium: Happy path test (HP-1)
- [ ] Selenium: Multiple recommendations test (HP-2)
- [ ] Selenium: Dynamic updates test (HP-3)
- [ ] Selenium: Regression tests (Risk 1-5)

### Phase 4: Performance Tests
- [ ] Load test: Response time < 500ms (AC8.1)
- [ ] Load test: 500+ concurrent requests (AC8.2)
- [ ] Cache effectiveness test (AC8.3)

### Phase 5: Manual Testing
- [ ] Mobile responsiveness (AC6.1, AC6.2, AC6.3, AC6.4)
- [ ] Cross-browser compatibility (Chrome, Firefox, Safari, Edge)
- [ ] Accessibility (WCAG 2.1 AA compliance)

---

## 9. 🎯 Priority Test Cases for Immediate Implementation

### High Priority (Must Have Before Production):
1. **HP-1:** Happy path E2E test (Selenium)
2. **AC1.1:** Cart below threshold generates recommendations (Unit + Integration)
3. **AC3.4:** Reach threshold removes recommendations (Unit + E2E)
4. **AC8.4:** Graceful degradation (Unit + Integration)
5. **NP3:** Product service failure handling (Unit + Integration)

### Medium Priority (Should Have):
1. **AC2.1-2.3:** Multiple paths display and tab switching (Unit + E2E)
2. **AC3.1-3.3:** Dynamic updates (Unit + E2E)
3. **AC1.4:** Filter out cart items (Unit + Integration)
4. **EC1-EC2:** Boundary conditions (Unit + Integration)
5. **Risk 1-3:** Regression tests (E2E)

### Low Priority (Nice to Have):
1. **AC6.1-6.6:** Mobile optimization (Manual + E2E)
2. **AC8.1-8.2:** Performance load tests
3. **AC5.1:** Category ranking (Unit test)
4. **EC3-EC10:** Additional edge cases

---

## 10. 📝 Test Implementation Notes

### Missing Test Coverage Identified:
1. ❌ **Frontend Unit Tests:** `ShippingRecommendations.tsx` component needs comprehensive unit tests
2. ❌ **Dedicated E2E Test Class:** Need `SCRUM8ShippingRecommendationsTest.java` (currently only basic checks in `E2EWorkflowTest.java`)
3. ⚠️ **Postman Tests:** Need to add recommendation endpoint to Postman collection
4. ⚠️ **Performance Tests:** Load testing not yet implemented

### Test Data Requirements:
- Test products with various categories, prices, and inventory levels
- Test cart states: empty, below threshold, at threshold, above threshold
- Test regions: US ($50 threshold), CA ($75 threshold), invalid regions

### Test Environment Setup:
- Mock ProductService for unit tests
- Mock ShippingRuleService for unit tests
- Test database with known product catalog
- Selenium test environment with UI running

---

**Test Plan Created:** [Current Date]  
**Test Plan Version:** 1.0  
**Story Status:** Phase 1 Complete (Single Product Recommendations)  
**Next Phase:** Phase 2 (Bundle Recommendations), Phase 3 (Personalization), Phase 4 (Analytics)
