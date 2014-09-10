package Finder;

import Syntax.*;

public class V {
	public Recv recv;
	public Send send;
	
	public V(Recv r, Send s)
	{
		recv = r;
		send = s;
	}
	
	public Recv getRecv()
	{
		return recv;
	}
	
	public Send getSend()
	{
		return send;
	}
	
	public static V generateV(Recv r, Send s)
	{
		return new V(r,s);
	}
}
