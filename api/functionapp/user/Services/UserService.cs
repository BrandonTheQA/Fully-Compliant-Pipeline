using System.Security.Cryptography;
using System.Text;
using UserFunction.DTOs;
using UserFunction.Exceptions;
using UserFunction.Models;
using UserFunction.Repositories;
using Microsoft.Extensions.Logging;

namespace UserFunction.Services;

public class UserService : IUserService
{
    private readonly IUserRepository _userRepository;
    private readonly ILogger<UserService> _logger;

    public UserService(IUserRepository userRepository, ILogger<UserService> logger)
    {
        _userRepository = userRepository;
        _logger = logger;
    }

    public UserResponse CreateUser(CreateUserRequest request)
    {
        // Check if user already exists
        if (_userRepository.ExistsByEmail(request.Email))
        {
            throw new UserAlreadyExistsException($"User with email {request.Email} already exists");
        }

        // Create new user
        var user = new User
        {
            Id = Guid.NewGuid().ToString(),
            Name = request.Name,
            Email = request.Email,
            Password = HashPassword(request.Password)
        };

        var savedUser = _userRepository.Save(user);
        return MapToResponse(savedUser);
    }

    public UserResponse GetUser(string id)
    {
        var user = _userRepository.FindById(id);
        if (user == null)
        {
            throw new UserNotFoundException("User not found");
        }
        return MapToResponse(user);
    }

    public LoginResponse Login(LoginRequest request)
    {
        var user = _userRepository.FindByEmail(request.Email);
        if (user == null)
        {
            throw new AuthenticationException("Invalid email or password");
        }

        // Verify password
        var hashedPassword = HashPassword(request.Password);
        if (user.Password != hashedPassword)
        {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate simple token (in production, use JWT)
        var token = GenerateToken(user);

        return new LoginResponse
        {
            Token = token,
            User = MapToResponse(user)
        };
    }

    private UserResponse MapToResponse(User user)
    {
        return new UserResponse
        {
            Id = user.Id,
            Name = user.Name,
            Email = user.Email,
            CreatedAt = user.CreatedAt,
            UpdatedAt = user.UpdatedAt
        };
    }

    private string HashPassword(string password)
    {
        using var sha256 = SHA256.Create();
        var hashedBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(hashedBytes);
    }

    private string GenerateToken(User user)
    {
        // Simple token generation (in production, use JWT)
        var tokenData = $"{user.Id}:{user.Email}:{DateTime.UtcNow.Ticks}";
        var tokenBytes = Encoding.UTF8.GetBytes(tokenData);
        return Convert.ToBase64String(tokenBytes);
    }
}
