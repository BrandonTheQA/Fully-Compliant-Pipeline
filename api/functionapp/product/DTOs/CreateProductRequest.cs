using System.ComponentModel.DataAnnotations;

namespace ProductFunction.DTOs;

public class CreateProductRequest
{
    public string? Id { get; set; }
    
    [Required(ErrorMessage = "Product name is required")]
    public string Name { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Product description is required")]
    public string Description { get; set; } = string.Empty;
    
    [Range(0.01, double.MaxValue, ErrorMessage = "Price must be greater than 0")]
    public double Price { get; set; }
    
    [Range(0, int.MaxValue, ErrorMessage = "Quantity cannot be negative")]
    public int Quantity { get; set; }
    
    [Required(ErrorMessage = "Category is required")]
    public string Category { get; set; } = string.Empty;
}
