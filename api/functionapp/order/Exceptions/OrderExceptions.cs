namespace OrderFunction.Exceptions;

public class OrderNotFoundException : Exception
{
    public OrderNotFoundException(string message) : base(message) { }
}

public class OrderValidationException : Exception
{
    public OrderValidationException(string message) : base(message) { }
}

public class ServiceUnavailableException : Exception
{
    public ServiceUnavailableException(string message, Exception? innerException = null) 
        : base(message, innerException) { }
}
