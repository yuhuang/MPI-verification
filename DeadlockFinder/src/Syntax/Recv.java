package Syntax;
import java.util.BitSet;

public class Recv extends Operation {
	
	public int src;
	public int dest;
	public Send match;
	public BitSet var;
	public boolean isBlock;
	public Wait NearestWait;
	
	public Recv(String event, Process process, int src, 
			int dest, Send match, boolean isBlock, Wait nw) {
		super(event, process);
		this.src = src;
		this.dest = dest;
		this.match = match;
		this.var = new BitSet();
		this.isBlock = isBlock;
		this.NearestWait = nw;
	}
	
	public String toString()
	{
		return "Recv" + event;
	}
}
