using System.Text.Json;
using OrderFunction.DTOs;
using OrderFunction.Exceptions;
using OrderFunction.Models;
using OrderFunction.Repositories;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace OrderFunction.Services;

public class OrderService : IOrderService
{
    private readonly IOrderRepository _orderRepository;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IConfiguration _configuration;
    private readonly ILogger<OrderService> _logger;
    private readonly string _userServiceBaseUrl;
    private readonly string _productServiceBaseUrl;

    public OrderService(
        IOrderRepository orderRepository,
        IHttpClientFactory httpClientFactory,
        IConfiguration configuration,
        ILogger<OrderService> logger)
    {
        _orderRepository = orderRepository;
        _httpClientFactory = httpClientFactory;
        _configuration = configuration;
        _logger = logger;
        
        var environment = _configuration["ENVIRONMENT"] ?? "dev";
        _userServiceBaseUrl = _configuration["USER_SERVICE_URL"] 
            ?? $"https://joaz-func-user-9021-{environment}.azurewebsites.net/api";
        _productServiceBaseUrl = _configuration["PRODUCT_SERVICE_URL"] 
            ?? $"https://joaz-func-product-9021-{environment}.azurewebsites.net/api";
    }

    public async Task<OrderResponse> CreateOrderAsync(CreateOrderRequest request)
    {
        // Verify user exists
        await VerifyUserExistsAsync(request.UserId);

        // Build order items with product validation
        var orderItems = new List<OrderItem>();
        double totalAmount = 0.0;

        foreach (var itemRequest in request.Items)
        {
            var product = await VerifyProductAvailabilityAsync(itemRequest.ProductId, itemRequest.Quantity);
            
            var productName = product.GetProperty("name").GetString() ?? "";
            var price = product.GetProperty("price").GetDouble();
            var quantity = itemRequest.Quantity;
            var subtotal = price * quantity;

            var orderItem = new OrderItem
            {
                ProductId = itemRequest.ProductId,
                ProductName = productName,
                Quantity = quantity,
                Price = price,
                Subtotal = subtotal
            };
            
            orderItems.Add(orderItem);
            totalAmount += subtotal;
        }

        // Create order
        var order = new Order
        {
            Id = Guid.NewGuid().ToString(),
            UserId = request.UserId,
            Items = orderItems,
            TotalAmount = totalAmount,
            Status = "PENDING"
        };

        var savedOrder = _orderRepository.Save(order);
        return MapToResponse(savedOrder);
    }

    public OrderResponse GetOrder(string id)
    {
        var order = _orderRepository.FindById(id);
        if (order == null)
        {
            throw new OrderNotFoundException("Order not found");
        }
        return MapToResponse(order);
    }

    public List<OrderResponse> GetUserOrders(string userId)
    {
        var orders = _orderRepository.FindByUserId(userId);
        return orders.Select(MapToResponse).ToList();
    }

    private async Task VerifyUserExistsAsync(string userId)
    {
        try
        {
            var client = _httpClientFactory.CreateClient();
            var url = $"{_userServiceBaseUrl}/users/{userId}";
            var response = await client.GetAsync(url);

            if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                throw new OrderValidationException($"User not found: {userId}");
            }

            response.EnsureSuccessStatusCode();
        }
        catch (OrderValidationException)
        {
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "User service is unavailable");
            throw new ServiceUnavailableException($"User service is unavailable: {ex.Message}", ex);
        }
    }

    private async Task<JsonElement> VerifyProductAvailabilityAsync(string productId, int requestedQuantity)
    {
        try
        {
            var client = _httpClientFactory.CreateClient();
            var url = $"{_productServiceBaseUrl}/products/{productId}";
            var response = await client.GetAsync(url);

            if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                throw new OrderValidationException($"Product not found: {productId}");
            }

            response.EnsureSuccessStatusCode();
            
            var content = await response.Content.ReadAsStringAsync();
            var product = JsonSerializer.Deserialize<JsonElement>(content);

            // Check if product has sufficient quantity
            var availableQuantity = product.GetProperty("quantity").GetInt32();
            if (availableQuantity < requestedQuantity)
            {
                throw new OrderValidationException(
                    $"Product {productId} does not have sufficient quantity. " +
                    $"Requested: {requestedQuantity}, Available: {availableQuantity}");
            }

            // Validate price exists
            if (!product.TryGetProperty("price", out _))
            {
                throw new OrderValidationException($"Product {productId} has no price");
            }

            return product;
        }
        catch (OrderValidationException)
        {
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Product service is unavailable");
            throw new ServiceUnavailableException($"Product service is unavailable: {ex.Message}", ex);
        }
    }

    private OrderResponse MapToResponse(Order order)
    {
        return new OrderResponse
        {
            Id = order.Id,
            UserId = order.UserId,
            Items = order.Items.Select(item => new OrderItemResponse
            {
                ProductId = item.ProductId,
                ProductName = item.ProductName,
                Quantity = item.Quantity,
                Price = item.Price,
                Subtotal = item.Subtotal
            }).ToList(),
            TotalAmount = order.TotalAmount,
            Status = order.Status,
            CreatedAt = order.CreatedAt,
            UpdatedAt = order.UpdatedAt
        };
    }
}
