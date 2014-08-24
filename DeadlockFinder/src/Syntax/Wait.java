package Syntax;

public class Wait extends Operation {
	public Operation op;
	public Wait(String event, Process process, Operation op) {
		super(event, process);
		this.op = op;
	}
}
