using UserFunction.DTOs;

namespace UserFunction.Services;

public interface IUserService
{
    UserResponse CreateUser(CreateUserRequest request);
    UserResponse GetUser(string id);
    LoginResponse Login(LoginRequest request);
}
