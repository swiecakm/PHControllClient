package com.example.phcontrollclient;

public class NetClientBroadcastException extends Exception
{
	private static final long serialVersionUID = -3279582061542873813L;
	public NetClientBroadcastException(){}
	public NetClientBroadcastException(String message)
    {
       super(message);
    }
}
