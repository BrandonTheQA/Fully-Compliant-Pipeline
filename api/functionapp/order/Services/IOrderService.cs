using OrderFunction.DTOs;

namespace OrderFunction.Services;

public interface IOrderService
{
    Task<OrderResponse> CreateOrderAsync(CreateOrderRequest request);
    OrderResponse GetOrder(string id);
    List<OrderResponse> GetUserOrders(string userId);
}
