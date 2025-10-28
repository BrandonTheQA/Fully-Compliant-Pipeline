using ProductFunction.Models;

namespace ProductFunction.Repositories;

public interface IProductRepository
{
    Product Save(Product product);
    Product? FindById(string id);
    List<Product> FindAll();
    bool ExistsById(string id);
    long Count();
}
