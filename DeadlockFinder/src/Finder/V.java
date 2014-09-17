package Finder;

import Syntax.*;

public class V {
	public Recv recv;
	public Send send;
	public int pRank;
	
	public V(Recv r, Send s)
	{
		recv = r;
		send = s;
		pRank = r.dest;
	}
	
	public Recv getRecv()
	{
		return recv;
	}
	
	public Send getSend()
	{
		return send;
	}
	
	public int getProcessRank()
	{
		return pRank;
	}
	
	public static V generateV(Recv r, Send s)
	{
		return new V(r,s);
	}
	
	public String toString()
	{
		return "(" + recv + "," + send + ")";
	}
}
