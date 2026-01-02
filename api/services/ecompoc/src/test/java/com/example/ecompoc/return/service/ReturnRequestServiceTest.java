package com.example.ecompoc.returns.service;

import com.example.ecompoc.order.model.Order;
import com.example.ecompoc.order.model.OrderItem;
import com.example.ecompoc.order.repository.OrderRepository;
import com.example.ecompoc.returns.dto.CreateReturnRequest;
import com.example.ecompoc.returns.dto.ReturnResponse;
import com.example.ecompoc.returns.enums.ReturnReason;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnPolicyConfig;
import com.example.ecompoc.returns.model.ReturnStatusHistory;
import com.example.ecompoc.returns.repository.ReturnRepository;
import com.example.ecompoc.returns.repository.ReturnStatusHistoryRepository;
import com.example.ecompoc.returns.service.RMAGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("ReturnRequestService Tests")
class ReturnRequestServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnStatusHistoryRepository statusHistoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RMAGenerator rmaGenerator;

    @Mock
    private ReturnPolicyService returnPolicyService;

    @InjectMocks
    private ReturnRequestService returnRequestService;

    private Order testOrder;
    private OrderItem testOrderItem1;
    private OrderItem testOrderItem2;
    private ReturnPolicyConfig testPolicy;
    private String testUserId;
    private String testOrderId;
    private String testRmaNumber;

    @BeforeEach
    void setUp() {
        testUserId = "user-123";
        testOrderId = "order-123";
        testRmaNumber = "RMA-20241217-12345";

        // Create test order with items
        testOrder = new Order();
        testOrder.setId(testOrderId);
        testOrder.setUserId(testUserId);
        testOrder.setCreatedAt(LocalDateTime.now().minusDays(10));
        testOrder.setEstimatedDeliveryDate(LocalDateTime.now().minusDays(5));

        testOrderItem1 = new OrderItem("product-1", "Product 1", 2, 25.0);
        testOrderItem1.setId(1L);
        testOrder.addItem(testOrderItem1);

        testOrderItem2 = new OrderItem("product-2", "Product 2", 1, 50.0);
        testOrderItem2.setId(2L);
        testOrder.addItem(testOrderItem2);

        // Create test policy
        testPolicy = new ReturnPolicyConfig(30, 10.0, 50.0, 100.0);
        when(returnPolicyService.getActivePolicy()).thenReturn(testPolicy);
        when(returnPolicyService.isWithinReturnWindow(any(LocalDate.class))).thenReturn(true);
        when(returnPolicyService.calculateRestockingFee(any(BigDecimal.class)))
            .thenAnswer(invocation -> {
                BigDecimal amount = invocation.getArgument(0);
                return amount.multiply(BigDecimal.valueOf(0.10));
            });
    }

    @Test
    @DisplayName("Should create return request successfully (AC1.1)")
    void shouldCreateReturnRequestSuccessfully() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertEquals(testOrderId, response.getOrderId());
        assertEquals(testUserId, response.getUserId());
        assertEquals(testRmaNumber, response.getRmaNumber());
        assertEquals("PENDING_APPROVAL", response.getStatus());
        assertEquals("REFUND_TO_PAYMENT", response.getReturnType());
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());

        verify(returnRepository, times(1)).save(any(Return.class));
        verify(statusHistoryRepository, times(1)).save(any(ReturnStatusHistory.class));
        verify(rmaGenerator, times(1)).generateUniqueRMA();
    }

    @Test
    @DisplayName("Should create partial return with specific items (AC1.2)")
    void shouldCreatePartialReturn() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        // Return only item 1, not item 2
        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("DEFECTIVE");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getOrderItemId());
        assertNotNull(response.getRefundAmount());
        assertTrue(response.getRefundAmount() > 0);
    }

    @Test
    @DisplayName("Should validate return reason (AC1.3)")
    void shouldValidateReturnReason() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("DEFECTIVE"); // Valid reason
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertEquals("DEFECTIVE", response.getItems().get(0).getReturnReason());
    }

    @Test
    @DisplayName("Should handle return type selection (AC1.4)")
    void shouldHandleReturnTypeSelection() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("STORE_CREDIT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertEquals("STORE_CREDIT", response.getReturnType());
    }

    @Test
    @DisplayName("Should handle return with comments (AC1.5)")
    void shouldHandleReturnWithComments() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");
        request.setComments("Item arrived damaged");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("DEFECTIVE");
        itemRequest.setComments("Item arrived damaged");
        itemRequest.setCondition("Damaged");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertEquals("Item arrived damaged", response.getItems().get(0).getComments());
        assertEquals("Damaged", response.getItems().get(0).getCondition());
    }

    @Test
    @DisplayName("Should reject return outside return window (AC1.8, Negative Test 1)")
    void shouldRejectReturnOutsideReturnWindow() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(returnPolicyService.isWithinReturnWindow(any(LocalDate.class))).thenReturn(false);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("outside return window"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId("non-existent-order");
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById("non-existent-order")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when order does not belong to user")
    void shouldThrowExceptionWhenOrderDoesNotBelongToUser() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId("different-user");
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("Order does not belong to user"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when return items list is empty")
    void shouldThrowExceptionWhenReturnItemsListIsEmpty() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");
        request.setItems(new ArrayList<>());

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("at least one item"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when return quantity exceeds order quantity")
    void shouldThrowExceptionWhenReturnQuantityExceedsOrderQuantity() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(5); // Order item only has quantity 2
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("exceeds order quantity"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when order item not found")
    void shouldThrowExceptionWhenOrderItemNotFound() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(999L); // Non-existent order item
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("Order item not found"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when duplicate order items in return request")
    void shouldThrowExceptionWhenDuplicateOrderItems() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest1 = new CreateReturnRequest.ReturnItemRequest();
        itemRequest1.setOrderItemId(1L);
        itemRequest1.setQuantity(1);
        itemRequest1.setReturnReason("CHANGED_MIND");

        CreateReturnRequest.ReturnItemRequest itemRequest2 = new CreateReturnRequest.ReturnItemRequest();
        itemRequest2.setOrderItemId(1L); // Duplicate
        itemRequest2.setQuantity(1);
        itemRequest2.setReturnReason("DEFECTIVE");

        request.setItems(Arrays.asList(itemRequest1, itemRequest2));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.createReturnRequest(request);
        });

        assertTrue(exception.getMessage().contains("Duplicate order items"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should get return by RMA number")
    void shouldGetReturnByRMANumber() {
        // Given
        Return returnEntity = createTestReturn();
        when(returnRepository.findByRmaNumber(testRmaNumber)).thenReturn(Optional.of(returnEntity));

        // When
        ReturnResponse response = returnRequestService.getReturnByRMA(testRmaNumber);

        // Then
        assertNotNull(response);
        assertEquals(testRmaNumber, response.getRmaNumber());
        assertEquals(testOrderId, response.getOrderId());
        verify(returnRepository, times(1)).findByRmaNumber(testRmaNumber);
    }

    @Test
    @DisplayName("Should throw exception when RMA number not found")
    void shouldThrowExceptionWhenRMANumberNotFound() {
        // Given
        when(returnRepository.findByRmaNumber("INVALID-RMA")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.getReturnByRMA("INVALID-RMA");
        });

        assertTrue(exception.getMessage().contains("Return not found for RMA"));
    }

    @Test
    @DisplayName("Should get return by ID")
    void shouldGetReturnById() {
        // Given
        String returnId = "return-123";
        Return returnEntity = createTestReturn();
        returnEntity.setReturnId(returnId);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnEntity));

        // When
        ReturnResponse response = returnRequestService.getReturnById(returnId);

        // Then
        assertNotNull(response);
        assertEquals(returnId, response.getReturnId());
        verify(returnRepository, times(1)).findById(returnId);
    }

    @Test
    @DisplayName("Should throw exception when return ID not found")
    void shouldThrowExceptionWhenReturnIdNotFound() {
        // Given
        when(returnRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnRequestService.getReturnById("non-existent");
        });

        assertTrue(exception.getMessage().contains("Return not found"));
    }

    @Test
    @DisplayName("Should get all returns for a user")
    void shouldGetUserReturns() {
        // Given
        Return return1 = createTestReturn();
        return1.setReturnId("return-1");
        Return return2 = createTestReturn();
        return2.setReturnId("return-2");
        List<Return> returns = Arrays.asList(return1, return2);

        when(returnRepository.findByUserId(testUserId)).thenReturn(returns);

        // When
        List<ReturnResponse> responses = returnRequestService.getUserReturns(testUserId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(returnRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("Should calculate refund amount with restocking fee")
    void shouldCalculateRefundAmountWithRestockingFee() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrderId);
        request.setUserId(testUserId);
        request.setReturnType("REFUND_TO_PAYMENT");

        CreateReturnRequest.ReturnItemRequest itemRequest = new CreateReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setReturnReason("CHANGED_MIND");
        request.setItems(Arrays.asList(itemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(rmaGenerator.generateUniqueRMA()).thenReturn(testRmaNumber);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnResponse response = returnRequestService.createReturnRequest(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getRefundAmount());
        // Original price: 25.0, restocking fee 10% = 2.5, net refund = 22.5
        assertEquals(22.5, response.getRefundAmount(), 0.01);
    }

    @Test
    @DisplayName("Should get return entity by ID (internal use)")
    void shouldGetReturnEntityById() {
        // Given
        String returnId = "return-123";
        Return returnEntity = createTestReturn();
        returnEntity.setReturnId(returnId);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnEntity));

        // When
        Return result = returnRequestService.getReturnEntityById(returnId);

        // Then
        assertNotNull(result);
        assertEquals(returnId, result.getReturnId());
        verify(returnRepository, times(1)).findById(returnId);
    }

    // Helper method to create test return
    private Return createTestReturn() {
        Return returnEntity = new Return(
            "return-123",
            testOrderId,
            testUserId,
            testRmaNumber,
            ReturnStatus.PENDING_APPROVAL,
            ReturnType.REFUND_TO_PAYMENT
        );
        returnEntity.setRefundAmountDecimal(BigDecimal.valueOf(25.0));
        return returnEntity;
    }
}

