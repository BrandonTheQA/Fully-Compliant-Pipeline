using UserFunction.Models;

namespace UserFunction.Repositories;

public interface IUserRepository
{
    User Save(User user);
    User? FindById(string id);
    User? FindByEmail(string email);
    bool ExistsByEmail(string email);
    long Count();
}
