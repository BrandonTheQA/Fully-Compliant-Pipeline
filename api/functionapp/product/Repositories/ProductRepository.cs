using System.Collections.Concurrent;
using ProductFunction.Models;

namespace ProductFunction.Repositories;

public class ProductRepository : IProductRepository
{
    private readonly ConcurrentDictionary<string, Product> _products = new();

    public Product Save(Product product)
    {
        _products[product.Id] = product;
        return product;
    }

    public Product? FindById(string id)
    {
        _products.TryGetValue(id, out var product);
        return product;
    }

    public List<Product> FindAll()
    {
        return _products.Values.ToList();
    }

    public bool ExistsById(string id)
    {
        return _products.ContainsKey(id);
    }

    public long Count()
    {
        return _products.Count;
    }
}
