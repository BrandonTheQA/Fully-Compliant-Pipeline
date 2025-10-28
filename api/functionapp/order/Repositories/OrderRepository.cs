using System.Collections.Concurrent;
using OrderFunction.Models;

namespace OrderFunction.Repositories;

public class OrderRepository : IOrderRepository
{
    private readonly ConcurrentDictionary<string, Order> _orders = new();

    public Order Save(Order order)
    {
        _orders[order.Id] = order;
        return order;
    }

    public Order? FindById(string id)
    {
        _orders.TryGetValue(id, out var order);
        return order;
    }

    public List<Order> FindByUserId(string userId)
    {
        return _orders.Values
            .Where(o => o.UserId == userId)
            .ToList();
    }

    public long Count()
    {
        return _orders.Count;
    }
}
