using ProductFunction.DTOs;
using ProductFunction.Exceptions;
using ProductFunction.Models;
using ProductFunction.Repositories;
using Microsoft.Extensions.Logging;

namespace ProductFunction.Services;

public class ProductService : IProductService
{
    private readonly IProductRepository _productRepository;
    private readonly ILogger<ProductService> _logger;

    public ProductService(IProductRepository productRepository, ILogger<ProductService> logger)
    {
        _productRepository = productRepository;
        _logger = logger;
    }

    public ProductResponse CreateOrUpdateProduct(CreateProductRequest request)
    {
        string productId;
        
        if (!string.IsNullOrEmpty(request.Id))
        {
            // Update existing product
            productId = request.Id;
            var existingProduct = _productRepository.FindById(productId);
            
            if (existingProduct == null)
            {
                throw new ProductNotFoundException($"Product not found: {productId}");
            }

            existingProduct.Name = request.Name;
            existingProduct.Description = request.Description;
            existingProduct.Price = request.Price;
            existingProduct.Quantity = request.Quantity;
            existingProduct.Category = request.Category;
            existingProduct.UpdatedAt = DateTime.UtcNow.ToString("o");

            var updatedProduct = _productRepository.Save(existingProduct);
            return MapToResponse(updatedProduct);
        }
        else
        {
            // Create new product
            productId = Guid.NewGuid().ToString();
            
            var product = new Product
            {
                Id = productId,
                Name = request.Name,
                Description = request.Description,
                Price = request.Price,
                Quantity = request.Quantity,
                Category = request.Category
            };

            var savedProduct = _productRepository.Save(product);
            return MapToResponse(savedProduct);
        }
    }

    public ProductResponse GetProduct(string id)
    {
        var product = _productRepository.FindById(id);
        if (product == null)
        {
            throw new ProductNotFoundException("Product not found");
        }
        return MapToResponse(product);
    }

    public List<ProductResponse> GetAllProducts()
    {
        var products = _productRepository.FindAll();
        return products.Select(MapToResponse).ToList();
    }

    private ProductResponse MapToResponse(Product product)
    {
        return new ProductResponse
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            Quantity = product.Quantity,
            Category = product.Category,
            CreatedAt = product.CreatedAt,
            UpdatedAt = product.UpdatedAt
        };
    }
}
