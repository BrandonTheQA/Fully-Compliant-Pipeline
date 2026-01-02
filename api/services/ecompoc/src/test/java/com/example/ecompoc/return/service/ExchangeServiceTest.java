package com.example.ecompoc.returns.service;

import com.example.ecompoc.order.dto.CreateOrderRequest;
import com.example.ecompoc.order.dto.OrderResponse;
import com.example.ecompoc.order.service.OrderService;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.returns.dto.ExchangeRequest;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.repository.ReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeService Tests")
class ExchangeServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ExchangeService exchangeService;

    private Return testReturn;
    private Product testProduct;
    private Product exchangeProduct;
    private String testReturnId;
    private String testProductId;
    private String exchangeProductId;

    @BeforeEach
    void setUp() {
        testReturnId = "return-123";
        testProductId = "product-1";
        exchangeProductId = "product-2";

        testReturn = new Return(
            testReturnId,
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            ReturnStatus.APPROVED,
            ReturnType.EXCHANGE
        );
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));

        testProduct = new Product(testProductId, "Product 1", "Description 1", 50.0, 10, "Category");
        exchangeProduct = new Product(exchangeProductId, "Product 2", "Description 2", 75.0, 5, "Category");
    }

    @Test
    @DisplayName("Should process exchange for same product different size (AC8.1)")
    void shouldProcessExchangeForSameProductDifferentSize() {
        // Given
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));
        
        OrderResponse exchangeOrder = new OrderResponse();
        exchangeOrder.setId("exchange-order-123");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(exchangeOrder);

        // When
        OrderResponse result = exchangeService.processExchange(testReturnId, exchangeRequest);

        // Then
        assertNotNull(result);
        assertEquals("exchange-order-123", result.getId());
        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should process exchange for different product (AC8.2)")
    void shouldProcessExchangeForDifferentProduct() {
        // Given
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));
        
        OrderResponse exchangeOrder = new OrderResponse();
        exchangeOrder.setId("exchange-order-123");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(exchangeOrder);

        // When
        OrderResponse result = exchangeService.processExchange(testReturnId, exchangeRequest);

        // Then
        assertNotNull(result);
        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should calculate price difference for upgrade (charge difference) (AC8.3)")
    void shouldCalculatePriceDifferenceForUpgrade() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0)); // Original item value
        exchangeProduct.setPrice(75.0); // Exchange item value (higher)
        
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));

        // When
        Double priceDifference = exchangeService.calculatePriceDifference(testReturnId, exchangeProductId, 1);

        // Then
        assertNotNull(priceDifference);
        assertEquals(25.0, priceDifference, 0.01); // 75 - 50 = 25 (positive = charge customer)
    }

    @Test
    @DisplayName("Should calculate price difference for downgrade (refund difference) (AC8.4)")
    void shouldCalculatePriceDifferenceForDowngrade() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(75.0)); // Original item value
        exchangeProduct.setPrice(50.0); // Exchange item value (lower)
        
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));

        // When
        Double priceDifference = exchangeService.calculatePriceDifference(testReturnId, exchangeProductId, 1);

        // Then
        assertNotNull(priceDifference);
        assertEquals(-25.0, priceDifference, 0.01); // 50 - 75 = -25 (negative = refund customer)
    }

    @Test
    @DisplayName("Should calculate price difference for equal value exchange")
    void shouldCalculatePriceDifferenceForEqualValue() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        exchangeProduct.setPrice(50.0);
        
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));

        // When
        Double priceDifference = exchangeService.calculatePriceDifference(testReturnId, exchangeProductId, 1);

        // Then
        assertEquals(0.0, priceDifference, 0.01);
    }

    @Test
    @DisplayName("Should create new order for exchange item (AC8.7)")
    void shouldCreateNewOrderForExchangeItem() {
        // Given
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(2);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));
        
        OrderResponse exchangeOrder = new OrderResponse();
        exchangeOrder.setId("exchange-order-123");
        exchangeOrder.setUserId("user-123");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(exchangeOrder);

        // When
        OrderResponse result = exchangeService.processExchange(testReturnId, exchangeRequest);

        // Then
        assertNotNull(result);
        assertEquals("exchange-order-123", result.getId());
        verify(orderService, times(1)).createOrder(argThat(request -> {
            CreateOrderRequest req = (CreateOrderRequest) request;
            return req.getUserId().equals("user-123") &&
                   req.getItems().size() == 1 &&
                   req.getItems().get(0).getProductId().equals(exchangeProductId) &&
                   req.getItems().get(0).getQuantity() == 2;
        }));
    }

    @Test
    @DisplayName("Should throw exception for non-exchange return type")
    void shouldThrowExceptionForNonExchangeReturnType() {
        // Given
        testReturn.setReturnType(ReturnType.REFUND_TO_PAYMENT);
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            exchangeService.processExchange(testReturnId, exchangeRequest);
        });

        assertTrue(exception.getMessage().contains("not an exchange"));
        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when exchange product not found")
    void shouldThrowExceptionWhenExchangeProductNotFound() {
        // Given
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId("non-existent-product");
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById("non-existent-product")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeService.processExchange(testReturnId, exchangeRequest);
        });

        assertTrue(exception.getMessage().contains("Exchange product not found"));
        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when return not found")
    void shouldThrowExceptionWhenReturnNotFound() {
        // Given
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeService.processExchange("non-existent", exchangeRequest);
        });

        assertTrue(exception.getMessage().contains("Return not found"));
        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle quantity in price difference calculation")
    void shouldHandleQuantityInPriceDifferenceCalculation() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        exchangeProduct.setPrice(25.0);
        
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));

        // When - exchange 2 items
        Double priceDifference = exchangeService.calculatePriceDifference(testReturnId, exchangeProductId, 2);

        // Then
        assertEquals(0.0, priceDifference, 0.01); // 2 * 25 = 50, same as original
    }

    @Test
    @DisplayName("Should use default quantity of 1 when quantity is null")
    void shouldUseDefaultQuantityOfOneWhenQuantityIsNull() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        exchangeProduct.setPrice(50.0);
        
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));

        // When
        Double priceDifference = exchangeService.calculatePriceDifference(testReturnId, exchangeProductId, null);

        // Then
        assertEquals(0.0, priceDifference, 0.01); // Should use quantity 1
    }

    @Test
    @DisplayName("Should handle null refund amount in exchange")
    void shouldHandleNullRefundAmountInExchange() {
        // Given
        testReturn.setRefundAmountDecimal(null);
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));
        
        OrderResponse exchangeOrder = new OrderResponse();
        exchangeOrder.setId("exchange-order-123");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(exchangeOrder);

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            exchangeService.processExchange(testReturnId, exchangeRequest);
        });

        // Then
        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle null product price in exchange")
    void shouldHandleNullProductPriceInExchange() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        exchangeProduct.setPrice(null);
        
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setExchangeProductId(exchangeProductId);
        exchangeRequest.setQuantity(1);

        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(productRepository.findById(exchangeProductId)).thenReturn(Optional.of(exchangeProduct));
        
        OrderResponse exchangeOrder = new OrderResponse();
        exchangeOrder.setId("exchange-order-123");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(exchangeOrder);

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            exchangeService.processExchange(testReturnId, exchangeRequest);
        });

        // Then
        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }
}

