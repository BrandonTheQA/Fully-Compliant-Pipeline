# Shipping Cost Bug Fix

## Problem

The frontend was incorrectly showing "FREE" shipping (shippingCost = 0) when the API correctly returned `shippingCost: 5.99` for carts below the free shipping threshold.

## Root Cause

The bug was in the caching logic in `ui/src/services/shippingService.ts` (line 154):

```typescript
const shippingCost = qualifiesForFreeShipping ? 0 : cachedData.data.defaultShippingCost;
```

### The Issue:

1. **Cached Data Recalculation**: When using cached shipping cost data, the code recalculates `shippingCost` based on the cached `freeShippingThreshold`.

2. **Stale Cache Problem**: If the cache contained stale data with `freeShippingThreshold: 0` (from a previous test run or dev environment state), then:
   - `qualifiesForFreeShipping = cartTotal >= 0` is **always true** (any cart qualifies)
   - This sets `shippingCost = 0` even when the API would return 5.99

3. **Cache Not Fully Cleared**: The test's cache clearing wasn't comprehensive enough, leaving some stale cache entries that could cause this issue.

## The Fix

### 1. Added Cache Validation (`shippingService.ts`)

Added a check to detect and invalidate cache entries with invalid thresholds:

```typescript
// If cached threshold is 0 or invalid, don't use cache - fetch fresh data
if (cachedData.data.freeShippingThreshold <= 0) {
  // Cache is invalid (threshold should never be 0), continue to fetch from API
  sessionStorage.removeItem(cacheKey);
}
```

This ensures that if stale cache with a threshold of 0 exists, it's automatically cleared and fresh data is fetched from the API.

### 2. Improved Cache Clearing in Tests (`SCRUM8ShippingRecommendationsTest.java`)

Enhanced the `clearShippingCache()` method to:
- Clear all known shipping cache keys (including region-specific ones)
- Clear all keys with shipping-related prefixes
- Add a small delay to ensure cache clearing completes

## Why This Happened

The cache could get into a bad state if:
1. A previous test run or dev environment had a threshold of 0
2. The cache wasn't fully cleared between test runs
3. The browser session had stale data from previous runs

## Testing

After this fix:
1. Stale cache with threshold 0 will be automatically detected and cleared
2. Fresh API calls will be made, returning correct shipping costs
3. Tests should now properly see shipping costs and display recommendations

## Files Modified

- `ui/src/services/shippingService.ts` - Added cache validation to detect invalid thresholds
- `selenium/src/test/java/SCRUM8ShippingRecommendationsTest.java` - Improved cache clearing

## Next Steps

1. Re-run the Selenium tests to verify the fix
2. Monitor for any other cache-related issues
3. Consider adding cache versioning or expiration to prevent similar issues

