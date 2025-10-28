using System.Collections.Concurrent;
using UserFunction.Models;

namespace UserFunction.Repositories;

public class UserRepository : IUserRepository
{
    private readonly ConcurrentDictionary<string, User> _users = new();

    public User Save(User user)
    {
        _users[user.Id] = user;
        return user;
    }

    public User? FindById(string id)
    {
        _users.TryGetValue(id, out var user);
        return user;
    }

    public User? FindByEmail(string email)
    {
        return _users.Values.FirstOrDefault(u => 
            u.Email.Equals(email, StringComparison.OrdinalIgnoreCase));
    }

    public bool ExistsByEmail(string email)
    {
        return _users.Values.Any(u => 
            u.Email.Equals(email, StringComparison.OrdinalIgnoreCase));
    }

    public long Count()
    {
        return _users.Count;
    }
}
