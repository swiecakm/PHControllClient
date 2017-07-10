package phcontroll.com.phcontrollclient;

public class NetClientServerResponseException  extends Exception
{
    private static final long serialVersionUID = -1911829663171570739L;
    public NetClientServerResponseException(){}
    public NetClientServerResponseException(String message)
    {
        super(message);
    }
}