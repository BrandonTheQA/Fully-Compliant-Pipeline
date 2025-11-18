package com.example.ecompoc.shipping.recommendation.service;

import com.example.ecompoc.product.dto.ProductResponse;
import com.example.ecompoc.product.service.ProductService;
import com.example.ecompoc.shipping.recommendation.dto.OptimizationPath;
import com.example.ecompoc.shipping.recommendation.dto.RecommendationResponse;
import com.example.ecompoc.shipping.recommendation.dto.RecommendedProduct;
import com.example.ecompoc.shipping.service.GeolocationService;
import com.example.ecompoc.shipping.service.ShippingRuleService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating shipping optimization recommendations
 */
@Service
public class ShippingRecommendationService {
    
    private final ShippingRuleService shippingRuleService;
    private final ProductService productService;
    private final GeolocationService geolocationService;
    
    private static final BigDecimal MAX_PRICE_MULTIPLIER = new BigDecimal("1.5");
    private static final int MAX_RECOMMENDATIONS_PER_PATH = 5;
    private static final int MAX_SINGLE_PRODUCT_PATHS = 5;
    
    public ShippingRecommendationService(ShippingRuleService shippingRuleService,
                                       ProductService productService,
                                       GeolocationService geolocationService) {
        this.shippingRuleService = shippingRuleService;
        this.productService = productService;
        this.geolocationService = geolocationService;
    }
    
    /**
     * Generate shipping optimization recommendations
     * 
     * @param cartTotal Current cart total
     * @param cartItemIds List of product IDs already in cart
     * @param region Shipping region
     * @param userId Optional user ID for personalization (future use)
     * @return RecommendationResponse with optimization paths
     */
    @Cacheable(value = "recommendations", key = "#cartTotal.toString() + '-' + (#cartItemIds != null ? String.join(',', #cartItemIds) : '') + '-' + (#region != null ? #region : 'default')")
    public RecommendationResponse generateRecommendations(BigDecimal cartTotal, 
                                                          List<String> cartItemIds,
                                                          String region, 
                                                          String userId) {
        // Default values
        BigDecimal currentCartTotal = cartTotal != null ? cartTotal : BigDecimal.ZERO;
        Set<String> cartItemSet = cartItemIds != null ? new HashSet<>(cartItemIds) : new HashSet<>();
        String detectedRegion = region != null && !region.isEmpty() 
            ? geolocationService.detectRegion(region) 
            : geolocationService.detectRegion();
        
        // Check if already qualifies for free shipping
        boolean qualifiesForFreeShipping = shippingRuleService.qualifiesForFreeShipping(currentCartTotal, detectedRegion);
        BigDecimal remainingAmount = shippingRuleService.calculateRemainingAmount(currentCartTotal, detectedRegion);
        BigDecimal freeShippingThreshold = shippingRuleService.getFreeShippingThreshold(detectedRegion);
        BigDecimal defaultShippingCost = shippingRuleService.getShippingRule(detectedRegion).getDefaultShippingCost();
        
        // If already qualifies, return empty recommendations
        if (qualifiesForFreeShipping) {
            return new RecommendationResponse(
                new ArrayList<>(),
                true,
                BigDecimal.ZERO,
                detectedRegion,
                currentCartTotal,
                freeShippingThreshold
            );
        }
        
        // Get all products
        List<ProductResponse> allProducts = productService.getAllProducts();
        
        // Get cart items for category matching
        Set<String> cartCategories = getCartCategories(allProducts, cartItemSet);
        
        // Find optimal products
        List<ProductResponse> candidateProducts = findOptimalProducts(
            allProducts, 
            remainingAmount, 
            cartItemSet, 
            cartCategories
        );
        
        // Generate optimization paths
        List<OptimizationPath> optimizationPaths = calculateOptimizationPaths(
            candidateProducts, 
            currentCartTotal, 
            freeShippingThreshold,
            defaultShippingCost,
            cartCategories
        );
        
        return new RecommendationResponse(
            optimizationPaths,
            false,
            remainingAmount,
            detectedRegion,
            currentCartTotal,
            freeShippingThreshold
        );
    }
    
    /**
     * Find optimal products for recommendations
     */
    private List<ProductResponse> findOptimalProducts(List<ProductResponse> allProducts,
                                                      BigDecimal remainingAmount,
                                                      Set<String> cartItemIds,
                                                      Set<String> cartCategories) {
        return allProducts.stream()
            .filter(product -> {
                // Filter out products already in cart
                if (cartItemIds.contains(product.getId())) {
                    return false;
                }
                
                // Filter by inventory availability
                if (product.getQuantity() == null || product.getQuantity() <= 0) {
                    return false;
                }
                
                // Filter by price range (products priced up to 1.5x remaining amount)
                if (product.getPrice() == null || product.getPrice() <= 0) {
                    return false;
                }
                
                BigDecimal maxPrice = remainingAmount.multiply(MAX_PRICE_MULTIPLIER);
                BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
                return productPrice.compareTo(maxPrice) <= 0;
            })
            .sorted((p1, p2) -> {
                // Score and sort products
                int score1 = scoreProduct(p1, cartCategories, remainingAmount);
                int score2 = scoreProduct(p2, cartCategories, remainingAmount);
                return Integer.compare(score2, score1); // Descending order
            })
            .limit(MAX_RECOMMENDATIONS_PER_PATH * 3) // Get more candidates for path generation
            .collect(Collectors.toList());
    }
    
    /**
     * Score a product based on relevance
     */
    private int scoreProduct(ProductResponse product, Set<String> cartCategories, BigDecimal remainingAmount) {
        int score = 0;
        
        // Category match scoring
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            if (cartCategories.contains(product.getCategory())) {
                score += 10; // Same category
            } else {
                score += 5; // Different category but still relevant
            }
        }
        
        // Price proximity scoring (closer to remaining amount = higher score)
        if (product.getPrice() != null) {
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal difference = remainingAmount.subtract(productPrice).abs();
            // Score inversely proportional to difference (max 20 points)
            if (difference.compareTo(BigDecimal.ZERO) == 0) {
                score += 20;
            } else if (difference.compareTo(remainingAmount.multiply(new BigDecimal("0.1"))) <= 0) {
                score += 15; // Within 10% of remaining amount
            } else if (difference.compareTo(remainingAmount.multiply(new BigDecimal("0.3"))) <= 0) {
                score += 10; // Within 30% of remaining amount
            } else {
                score += 5;
            }
        }
        
        // Popularity scoring (newer products = more popular, use createdAt if available)
        // For now, we'll use a simple approach - products with non-null createdAt get bonus
        if (product.getCreatedAt() != null && !product.getCreatedAt().isEmpty()) {
            score += 3;
        }
        
        return score;
    }
    
    /**
     * Get categories from cart items
     */
    private Set<String> getCartCategories(List<ProductResponse> allProducts, Set<String> cartItemIds) {
        return allProducts.stream()
            .filter(p -> cartItemIds.contains(p.getId()))
            .map(ProductResponse::getCategory)
            .filter(Objects::nonNull)
            .filter(cat -> !cat.isEmpty())
            .collect(Collectors.toSet());
    }
    
    /**
     * Calculate optimization paths
     */
    private List<OptimizationPath> calculateOptimizationPaths(List<ProductResponse> candidateProducts,
                                                             BigDecimal cartTotal,
                                                             BigDecimal freeShippingThreshold,
                                                             BigDecimal defaultShippingCost,
                                                             Set<String> cartCategories) {
        List<OptimizationPath> paths = new ArrayList<>();
        
        // Generate single product paths (products that alone reach threshold)
        List<OptimizationPath> singleProductPaths = generateSingleProductPaths(
            candidateProducts, 
            cartTotal, 
            freeShippingThreshold,
            defaultShippingCost
        );
        paths.addAll(singleProductPaths);
        
        // For Phase 1, focus on single product paths only
        // Bundle paths can be added in Phase 2
        
        // Limit total paths
        return paths.stream()
            .limit(MAX_SINGLE_PRODUCT_PATHS)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate single product optimization paths
     */
    private List<OptimizationPath> generateSingleProductPaths(List<ProductResponse> candidateProducts,
                                                              BigDecimal cartTotal,
                                                              BigDecimal freeShippingThreshold,
                                                              BigDecimal defaultShippingCost) {
        List<OptimizationPath> paths = new ArrayList<>();
        
        for (ProductResponse product : candidateProducts) {
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            
            // Check if this product alone reaches the threshold
            BigDecimal newCartTotal = cartTotal.add(productPrice);
            if (newCartTotal.compareTo(freeShippingThreshold) >= 0) {
                // Calculate savings (shipping cost that would be saved)
                BigDecimal savingsAmount = defaultShippingCost;
                
                // Create recommended product DTO
                RecommendedProduct recommendedProduct = new RecommendedProduct(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    productPrice,
                    product.getCategory(),
                    String.format("Add this to get FREE shipping and save $%.2f", savingsAmount),
                    null // imageUrl - can be added later
                );
                
                // Create optimization path
                String message = String.format("Add %s → FREE shipping", product.getName());
                OptimizationPath path = new OptimizationPath(
                    Collections.singletonList(recommendedProduct),
                    productPrice,
                    savingsAmount,
                    message,
                    "single"
                );
                
                paths.add(path);
                
                // Limit number of single product paths
                if (paths.size() >= MAX_SINGLE_PRODUCT_PATHS) {
                    break;
                }
            }
        }
        
        return paths;
    }
}

