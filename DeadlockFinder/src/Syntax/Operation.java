package Syntax;

public class Operation {
	public String event;
	public Process process;
	public Operation(String event, Process process)
	{
		this.event = event;
		this.process = process;
	}
	
	public String toString()
	{
		return "Operation " + event;
	}
}
