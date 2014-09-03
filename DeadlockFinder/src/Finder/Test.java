package Finder;

import java.util.BitSet;

import Syntax.*;
import Syntax.Process;

public class Test {

	public static void main(String[] args) throws Exception {
		
		Program program;
		Process process0,process1,process2;
		UnmatchedEP_Finder finder1;
		//test case 1
		//R(*)    S(0)
		//R(1)    S(0)
		program = new Program();
		process0 = new Process(0);
		process1 = new Process(1);
		program.add(process0);
		program.add(process1);
		process0.add(new Recv(process0.getRank()+ "_" + 0, process0,0, -1, 
			0, null, true, null));
		process0.add(new Recv(process0.getRank()+ "_" + 1, process0,1, 1, 
				0, null, true, null));
		
		process1.add(new Send(process1.getRank() + "_" + 0, process1,0, 1, 0, null, 2, 
				true, null));
		process1.add(new Send(process1.getRank() + "_" + 1, process1,1, 1, 0, null, 3, 
				true, null));
		
	    finder1 = new UnmatchedEP_Finder(program);
		
		finder1.Run();
		
		//-----------------------------------------------
		//test case 2
		//R(*)    S(0)    S(0)
		//R(1)
		program = new Program();
		process0 = new Process(0);
		process1 = new Process(1);
		process2 = new Process(2);
		program.add(process0);
		program.add(process1);
		program.add(process2);
		process0.add(new Recv(process0.getRank()+ "_" + 0, process0, 0, -1, 
			0, null, true, null));
		process0.add(new Recv(process0.getRank()+ "_" + 1, process0, 1,1, 
				0, null, true, null));
		
		process1.add(new Send(process1.getRank() + "_" + 0, process1,0, 1, 0, null, 2, 
				true, null));
		process2.add(new Send(process2.getRank() + "_" + 0, process1,0, 2, 0, null, 3, 
				true, null));
		
		finder1 = new UnmatchedEP_Finder(program);
		
		finder1.Run();
		
		//--------------------------------------------------
		//test case 3
		//R(*)   R(*)    S(0)
		//S(1)   S(0)    
		//--------------------
		//<R(1)>          
		//return may deadlock (actually no deadlock, need SMT encoding to verify)
		program = new Program();
		process0 = new Process(0);
		process1 = new Process(1);
		process2 = new Process(2);
		program.add(process0);
		program.add(process1);
		program.add(process2);
		process0.add(new Recv(process0.getRank()+ "_" + 0, process0, 0,-1, 
			0, null, true, null));
		process0.add(new Send(process0.getRank() + "_" + 1, process1, 0, 0, 1, null, 2, 
				true, null));
		process0.add(new Recv(process0.getRank()+ "_" + 2, process0,1, 1, 
				0, null, true, null));
		process1.add(new Recv(process1.getRank()+ "_" + 0, process0, 0,-1, 
				1, null, true, null));
		process1.add(new Send(process1.getRank() + "_" + 1, process1, 0,1, 0, null, 3, 
				true, null));
		process2.add(new Send(process2.getRank() + "_" + 0, process1,0, 2, 0, null, 4, 
				true, null));
		
		finder1 = new UnmatchedEP_Finder(program);
		
		finder1.Run();
		
		
		
	}

}
