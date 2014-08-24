package Syntax;

import java.util.BitSet;

public class Send extends Operation {
	
	public int src;
	public int dest;
	public Recv match;
	public BitSet value;
	public boolean isBlock;
	public Wait NearestWait;
	public Send(String event, Process process, int src, int dest, Recv match, BitSet value, 
			boolean isBlock, Wait nw) {
		super(event, process);
		this.src = src;
		this.dest = dest;
		this.match = match;
		this.value = value;
		this.isBlock = isBlock;
		this.NearestWait = nw;
	}
	
	public String toString()
	{
		return "Send" + event;
	}
}
