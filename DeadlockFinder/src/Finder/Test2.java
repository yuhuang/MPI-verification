package Finder;

import Syntax.Process;
import Syntax.Program;
import Syntax.Recv;
import Syntax.Send;

public class Test2 {

	public static void main(String[] args) throws Exception {
		
		Program program;
		Process process0,process1,process2;
		Circle_Finder finder;
		//test case 1
		//R(*)    R(*)   R(*)
		//S(1)    S(2)   S(0)
		//S(1)
		program = new Program(false);
		process0 = new Process(0);
		process1 = new Process(1);
		process2 = new Process(2);
		program.add(process0);
		program.add(process1);
		program.add(process2);
		process0.add(new Recv(process0.getRank()+ "_" + 0, process0,0, -1, 
			0, null, true, null));
		
		process0.add(new Send(process0.getRank() + "_" + 1, process0,0, 0, 1, null, 2, 
				true, null));
		process0.add(new Send(process0.getRank() + "_" + 2, process0,1, 0, 1, null, 4, 
				true, null));
		process1.add(new Recv(process1.getRank()+ "_" + 0, process1,0, -1, 
				1, null, true, null));
		process1.add(new Send(process1.getRank() + "_" + 1, process1,0, 1, 2, null, 3, 
				true, null));
		process2.add(new Recv(process2.getRank()+ "_" + 0, process2,0, -1, 
				2, null, true, null));
		process2.add(new Send(process2.getRank() + "_" + 1, process1,0, 2, 0, null, 5, 
				true, null));
		program.InitGraph();

//		Digraph graph = new Digraph(program);
//		TarjanSCC tc = new TarjanSCC(graph);
		finder = new Circle_Finder(program);
		finder.Run();
		
//		tc.printSCC();
//		tc.printCircles(tc.toCircles());
		System.out.println("=========================================================");
		
		//test case 2
		//S(1)
		//R(*)    R(*)   R(*)
		//S(1)    S(2)   S(0)
		
		program = new Program(false);
		process0 = new Process(0);
		process1 = new Process(1);
		process2 = new Process(2);
		program.add(process0);
		program.add(process1);
		program.add(process2);
		process0.add(new Send(process0.getRank() + "_" + 0, process0,0, 0, 1, null, 4, 
				true, null));
		process0.add(new Recv(process0.getRank()+ "_" + 1, process0,0, -1, 
				0, null, true, null));
				
		process0.add(new Send(process0.getRank() + "_" + 2, process0,1, 0, 1, null, 2, 
				true, null));
		process1.add(new Recv(process1.getRank()+ "_" + 0, process1,0, -1, 
				1, null, true, null));
		process1.add(new Send(process1.getRank() + "_" + 1, process1,0, 1, 2, null, 3, 
				true, null));
		process2.add(new Recv(process2.getRank()+ "_" + 0, process2,0, -1, 
				2, null, true, null));
		process2.add(new Send(process2.getRank() + "_" + 1, process1,0, 2, 0, null, 5, 
						true, null));
		program.InitGraph();
		finder = new Circle_Finder(program);
		finder.Run();
		System.out.println("=========================================================");
		
		//test case 2
		//S(1)    R(*)
		//R(*)    R(*)   R(*)
		//S(1)    S(2)   S(0)
				
		program = new Program(false);
		process0 = new Process(0);
		process1 = new Process(1);
		process2 = new Process(2);
		program.add(process0);
		program.add(process1);
		program.add(process2);
		process0.add(new Send(process0.getRank() + "_" + 0, process0,0, 0, 1, null, 4, 
				true, null));
		process0.add(new Recv(process0.getRank()+ "_" + 1, process0,0, -1, 
				0, null, true, null));
						
		process0.add(new Send(process0.getRank() + "_" + 2, process0,1, 0, 1, null, 2, 
				true, null));
		process1.add(new Recv(process1.getRank()+ "_" + 0, process1,0, -1, 
				1, null, true, null));
		process1.add(new Recv(process1.getRank()+ "_" + 1, process1,1, -1, 
				1, null, true, null));
		process1.add(new Send(process1.getRank() + "_" + 2, process1,0, 1, 2, null, 3, 
				true, null));
		process2.add(new Recv(process2.getRank()+ "_" + 0, process2,0, -1, 
				2, null, true, null));
		process2.add(new Send(process2.getRank() + "_" + 1, process1,0, 2, 0, null, 5, 
						true, null));
		program.InitGraph();
		
		finder = new Circle_Finder(program);
		finder.Run();
		System.out.println("=========================================================");
		
	}

}
