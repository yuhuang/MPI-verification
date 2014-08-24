package Syntax;

public class UnmatchedEP_Pattern {
	public Process process;
//	public Recv wildcard;
	public Recv determinstic;
	
	static UnmatchedEP_Pattern generatePattern(Recv d)
	{
		UnmatchedEP_Pattern newPattern = new UnmatchedEP_Pattern();
//		newPattern.wildcard = w;
		newPattern.determinstic = d;
		newPattern.process = d.process;
		return newPattern;
	}
}
