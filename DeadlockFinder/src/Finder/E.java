package Finder;

public class E {
	public V src;
	public V dest;
	
	public E(V s, V d)
	{
		src = s;
		dest = d;
	}
	
	public V getSource()
	{
		return src;
	}
	
	public V getDestination()
	{
		return dest;
	}
	
	public static E generateE(V s, V d)
	{
		return new E(s,d);
	}
	
	public String toString()
	{
		return "[" + src + "-->" + dest + "]"; 
	}
}
