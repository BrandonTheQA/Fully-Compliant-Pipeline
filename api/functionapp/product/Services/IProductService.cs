using ProductFunction.DTOs;

namespace ProductFunction.Services;

public interface IProductService
{
    ProductResponse CreateOrUpdateProduct(CreateProductRequest request);
    ProductResponse GetProduct(string id);
    List<ProductResponse> GetAllProducts();
}
