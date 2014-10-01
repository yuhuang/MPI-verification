package Finder;

import Syntax.Operation;

public class E {
	public Operation src;
	public Operation dest;
	
	public E(Operation s, Operation d)
	{
		src = s;
		dest = d;
	}
	
	public Operation getSource()
	{
		return src;
	}
	
	public Operation getDestination()
	{
		return dest;
	}
	
	public static E generateE(Operation s, Operation d)
	{
		return new E(s,d);
	}
	
	public String toString()
	{
		return "[" + src + "-->" + dest + "]"; 
	}
}
