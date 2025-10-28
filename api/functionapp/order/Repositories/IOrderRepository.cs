using OrderFunction.Models;

namespace OrderFunction.Repositories;

public interface IOrderRepository
{
    Order Save(Order order);
    Order? FindById(string id);
    List<Order> FindByUserId(string userId);
    long Count();
}
