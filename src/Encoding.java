import java.io.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Encoding {
String TYPE = "(declare-datatypes (T1 T2 T3 T4 T5)\n " +
		"((Recv (mk-recv (match T1) (ep T2) (var T3) (event T4) (nearestwait T5)))))\n" +
		"(declare-datatypes (T1 T2 T3 T4)\n" +
		"((Send (mk-send (match T1) (ep T2) (value T3) (event T4)))))\n" 
		;

String HBdef = "(define-fun HB ((a Int) (b Int)) Bool\n"+
        "  (if(< a b)\n" +
		" true\n" +
		" false))\n";
		
String MATCH = "(define-fun MATCH ((r (Recv Int Int Int Int Int)) (s (Send Int Int Int Int))) Bool\n" +
  "(if(and (= (ep r) (ep s)) (= (var r) (value s))\n" + 
    "(HB (event s) (nearestwait r)) (= (match r) (event s))\n" + 
    "(= (match s) (event r)))\n" +
    "true\n" +
    "false\n" +
  ")\n" +
")\n";

String fileinput;
String fileoutput;

String def = "";
String mdef = "";
String match = "";
String asserts = "";
String hb = "";

int N = 0;//need to be initialized 

boolean zeroBuffer = false;//zero buffer

match mc;
Hashtable<String, String> nearestWaits;
HashSet<String> variableSet;
String[] current_barrier; //the barrier for HB relations
LinkedList<String>[] barriers;//to store the barriers for all processes
String[] last_nearest_waits;
boolean[][] HB_lastwait_send_available;
String[] last_sends;

FileInputStream fstream;
FileOutputStream fostream;
// Get the object of DataInputStream
DataInputStream in;
DataOutputStream out;
BufferedReader br;
BufferedWriter bw;

boolean debug = false;
boolean debug2 = false;

boolean isZero = false;

Encoding(String finput, String foutput,int threadNum,boolean zero){
   fileinput = finput;
   fileoutput = foutput;
   N = threadNum;
   isZero = zero;
   current_barrier = new String[N];
   barriers = new LinkedList[N];
   for(int i = 0; i < N; i ++)
   {
	   current_barrier[i] = "";
	   barriers[i] = new LinkedList<String>();
   }
   last_nearest_waits = new String[N];
   HB_lastwait_send_available = new boolean[N][N];
   
   for(int i = 0; i < N; i++)
   {
	   for(int j = 0; j < N; j++)
		   HB_lastwait_send_available[i][j] = true;
   }
   
   for(int i = 0; i < N; i++)
	   last_nearest_waits[i] = "";
   
   
   if(isZero)
   {
	   last_sends = new String[N];		  
	   for(int i = 0; i < N; i ++)
		   last_sends[i] = "";
	   
	   HBdef  = "(define-fun HB ((a Int) (b Int)) Bool\n"+
		        "  (if(< a b)\n" +
				" true\n" +
				" false))\n" +
				"(define-fun HB* ((a Int) (b Int)) Bool\n"+
		        "  (if(= a (- b 1))\n" +
				" true\n" +
				" false))\n";
	   MATCH = "(define-fun MATCH ((r (Recv Int Int Int Int Int)) (s (Send Int Int Int Int))) Bool\n" +
			   "(if(and (= (ep r) (ep s)) (= (var r) (value s))\n" + 
			     "(HB* (event s) (event r)) (= (match r) (event s))\n" + 
			     "(= (match s) (event r)))\n" +
			     "true\n" +
			     "false\n" +
			   ")\n" +
			 ")\n";
	   
   }
  
}

void ExtractSnd(int myep, String statement, String event ){
	//String op = statement.split("\\(")[0];
	String ep = statement.split("\\(|,|\\)| ")[1];
	String value = statement.split("\\(|,|\\)| ")[3];
	if(debug){
		System.out.println("value in ExtractSnd: " + value);
	}
	String name = "send"+event;

	mdef += "(declare-const " + name + " (Send Int Int Int Int))\n(assert (and (= (ep " + name + ") "
	+ ep + ") (= (event " + name + ") " + event + ") (= (value " + name + ") " + value + ")))\n";
	
	//generate send list for match pair and add HB for sends with the same endpoint
	match.send newsend = mc.createSend(name, mc.sendlist[Integer.parseInt(ep)][myep].size());
	mc.sendlist[Integer.parseInt(ep)][myep].add(newsend);
	
	if(!isZero && mc.sendlist[Integer.parseInt(ep)][myep].size() > 1){
		match.send lastsend = mc.sendlist[Integer.parseInt(ep)][myep].get(mc.sendlist[Integer.parseInt(ep)][myep].size()-2);
		hb += "(HB " + lastsend.exp.substring(4) + " " + newsend.exp.substring(4) + ") ";
	}
	
	if(isZero)
	{
		last_sends[myep] = newsend.exp;
	}
	
	
	//HB rules over the nearest wait of R and s
	//may need more structures to support that only one hb relation is enforced for sends with common ep
	if(!last_nearest_waits[myep].equals("") && HB_lastwait_send_available[Integer.parseInt(ep)][myep])
	{
		//HB relation
		hb += "(HB " + last_nearest_waits[myep] + " " + event +") ";
		HB_lastwait_send_available[Integer.parseInt(ep)][myep] = false;
	}
	
}

void ExtractRecv(int ep, String statement, String event){

	String var = statement.split("\\(|,|\\)| ")[3];
	if(!variableSet.contains(var)){
		//currently, only integer is supported as basic type of variables in the encoding
		def += "(declare-const " + var +" Int)\n";
		variableSet.add(var);
	}
		
	if(debug){
		System.out.println("variable in ExtractRecv: " + var);
	}
	String name = "recv"+event;
	String des = ") (= (select " + name;
	
	
	
	mdef += "(declare-const " + name + " (Recv Int Int Int Int Int))\n(assert (and (= (ep " + name + ") " 
			+ ep + ") (= (event " + name + ") " + event + ") (= (var " + name + ") " + var + ")))\n";
	
	//generate recv list for match pair and add HB for recvs
	match.recv newrecv = mc.createRecv(name, mc.recvlist[ep].size(),event);
	mc.recvlist[ep].add(newrecv);
	
	if(mc.recvlist[ep].size() > 1){
		match.recv lastrecv = mc.recvlist[ep].get(mc.recvlist[ep].size()-2);
		//use substring because we record name, which is recv/send followed by the event
		hb += "(HB " + lastrecv.exp.substring(4) + " " + newrecv.exp.substring(4) + ") ";
	}
}



void ExtractWait(int ep, String statement, String event){
	String event_r = statement.split("\\(|\\)")[1];
	if(debug)
		System.out.println(event+": event for recv in ExtractWait: " + event_r);
	//first, check if event_r exists in recvlist
	Iterator<match.recv> ite_r = mc.recvlist[ep].iterator();
	boolean exists = false;
	while(ite_r.hasNext()){
		match.recv r = ite_r.next();
		if(r.event.equals(event_r)){
			exists = true;
			break;
		}
	}
	//second, add in the hashtable the nearest wait for all the previous recvs before event_r IF the recv has no wait yet
	//if event_r has already been assigned a wait, NO HB for event_r and wait added; otherwise, add the HB
	if(exists){
		//add nearest in the recv record for matching
		mdef += "(assert (= (nearestwait recv" + event_r + ") " + event + "))\n"; 
		
		//change the value of the nearest wait for barrier
		//reset the availability of HB over wait and the send in a sequential order on each process
		{
			last_nearest_waits[ep] = event;
			
			for(int i = 0; i < N; i++)
			{
				  for(int j = 0; j < N; j++)
				      HB_lastwait_send_available[i][j] = true;
			}
		}
		
		ite_r = mc.recvlist[ep].iterator();
		while(ite_r.hasNext()){
			match.recv r = ite_r.next();
			
			if(r.event.equals(event_r)){
				//first check if event_r itself has been assigned wait
				if(!nearestWaits.containsKey(event_r)){
					if(debug)
						System.out.println("event_r is not in nearestWaits of ExtractWaits: " + event_r);
					//second, if not, add an HB for event_r and its wait
					hb += "(HB " + event_r + " " + event +") ";
					nearestWaits.put(r.event, event);
				}
				break;//end loop if we witness event_r
			}else if(!nearestWaits.containsKey(r.event)){//if the recv has not been assigned a wait
					nearestWaits.put(r.event, event);
				}
		}
	}
}

//boolean barrierIssued(int binary,int ep, int n)
//{
//	int temp = binary;
//	int i = n-1;
//	int square = (int)Math.pow(2, i);
//	while(temp >= 0)
//	{
//		if(temp >= square)
//		{
//			return true;
//		}
//		temp = temp - square;
//		i--;
//		square = (int)Math.pow(2, i);
//	}
//	return false;
//}

void ExtractBarrier(int ep, String event,String statement)
{
	

	//HB rules over the nearest wait of R and the nearest barrier on every process
	if(!last_nearest_waits[ep].equals(""))
	{
		//HB relation
		hb += "(HB " + last_nearest_waits[ep] + " " + current_barrier[ep] +") ";
		//reset the nearest_wait if witnessing a barrier
		last_nearest_waits[ep] = "";
	}

}

void ExtractAssumeAssert(int ep, String statement){
	//need to negate the assert, will do it in the future...
	//naively take the substring of the assert or assume
	String content = statement.substring(6);
	if(debug){
		System.out.println("statement in ExtractAssumeAssert: " + content);
	}
	ExtractVariable(content);
	asserts += "(assert " + content + ")\n";
	/*String[] variables = statement.split("\\(|\\)| |\\+|-|\\*|/|^|%|[0-9]+");//????
	for(String var : variables){
		if(!variableSet.contains(var)){
			def += "(define " + var +"::int)\n";
			variableSet.add(var); 
		}
	}*/
}

void ExtractVariable(String st){
	
	String regx = "\\(|\\)|\\+|-|\\*|/|=|<|>|^|%|\\s";
	String[] subst = st.split(regx);
	for(String var : subst){
		//if the string is integer, igore, otherwise add variable. Other basic type are not supported
		if(Pattern.matches("\\d+", var))
			continue;
		if(!variableSet.contains(var)&&!var.equals("")
				&&!var.equals("and")&&!var.equals("or")&&!var.equals("not")){
			//currently, only integer is supported as basic type of variables in the encoding
			def += "(declare-const " + var +" Int)\n";
			variableSet.add(var);
		}
	}
}

void Extract(String s){
	if(debug)
		System.out.println("source: " + s);
	String source = s;
	String[] substring = source.split(": ");
	if(debug){
		for(int i =0;i<substring.length;i++)
			System.out.println("substring["+i+"]: " + substring[i]);
	}
	String event = substring[0];
	
	int endpoint = Integer.parseInt(substring[0].split("T|_")[1].trim());
	
	if(debug)
		System.out.println("endpoint in Extract: " + endpoint);
	
	if(endpoint < 0 || endpoint > N-1){
		System.out.println("error for node number: " + endpoint);
		return;
	}
	
	String op = substring[1].split("\\(")[0];
	String st = substring[1];		
	
	if(debug){
		System.out.println("operation in Extract: " + op);
	}
	
	if(op.equals("Barrier")||op.equals("barrier"))
	{
		String comm = st.split("\\(|,|\\)| ")[1];
		
		//add a barrier to the list
		event = "TB_" + comm;

		boolean isDefined = false;
		for(int i = 0; i < N; i++)
		{
			if(barriers[i].contains(event))
			{
				isDefined = true;
				break;
			}
		}
		if(!isDefined)
			def += "(declare-const " + event + " Int)\n";
		barriers[endpoint].addLast(event);
		
		//HB over barrier and any operation on ep because all the barriers are witnessed
		//may be revised to get rid of some redundant HB relations TODO
		if(!current_barrier[endpoint].equals("") )
		{
			//HB relation
			hb += "(HB " + current_barrier[endpoint] + " " + event +") ";
		}
		
		//current barrier for process ep is changed
		current_barrier[endpoint] = event; 
	}
	else
	{
		def += "(declare-const " + event + " Int)\n";
		//HB over barrier and any operation on ep because all the barriers are witnessed
		//may be revised to get rid of some redundant HB relations TODO
		if(!current_barrier[endpoint].equals("") )
		{
			//HB relation
			hb += "(HB " + current_barrier[endpoint] + " " + event +") ";
		}
	}
	

	if(isZero)
	{
		//for zero buffering, a send happens before any operation except the wait on an idential process
		if(!last_sends[endpoint].equals("") && !op.equals("wait"))
		{
			hb += "(HB " + last_sends[endpoint].substring(4) + " " + event + ") ";
		}
	}
	
	if(op.equals("snd")){
		ExtractSnd(endpoint, st, event);
	}
	else if(op.equals("rcv")){
		ExtractRecv(endpoint, st, event);
	}
	else if(op.equals("assume")||op.equals("assert")){
		ExtractAssumeAssert(endpoint, st);
	}
	else if(op.equals("wait")){
		ExtractWait(endpoint, st, event);
	}
	else if(op.equals("Barrier")||op.equals("barrier"))
	{
		ExtractBarrier(endpoint, event,st);
	}
	else{
		System.out.println("error extraction: " + source);
		return;
	}
	
}

void GenerateEncoding() throws IOException{
	hb = "(assert (and\n";
	match = "(assert (and\n";
	fstream = new FileInputStream(fileinput);
	in = new DataInputStream(fstream);
	br = new BufferedReader(new InputStreamReader(in));
	String line;
	
	mc = new match(N);
	nearestWaits = new Hashtable<String,String>();
	variableSet = new HashSet<String>();
	
	while((line = br.readLine()) != null){
		//System.out.println("line: " + line);
		Extract(line);
	}
	
	
	
	if(debug2){
		for(int i = 0; i < mc.recvlist.length; i++){
			Iterator<match.recv> ite_r = mc.recvlist[i].iterator();
			while(ite_r.hasNext()){
				match.recv r = ite_r.next();
				System.out.println("recv event and rank for recvlist["+i+"]: " + r.exp + ", " + r.rank);			
			}
		}
		
		for(int i = 0; i < mc.sendlist.length; i++){
			for(int j = 0; j < mc.sendlist[i].length; j++){
				Iterator<match.send> ite_ss = mc.sendlist[i][j].iterator();
				while(ite_ss.hasNext()){
					match.send s = ite_ss.next();
					System.out.println("send event and rank for sendlist[" + i + "][" + j + "]: " + s.exp + ", " + s.rank);
				}
			}
		}
	}
	
	
	hb += "))\n\n";
	
	
	//generate matches
	long t1 = System.currentTimeMillis();
	mc.generateMatch();
	//print out the match pair
	System.out.println("Match Pair set: " + mc.matchSize() + "\nOver-Approximiated possible choices: " + mc.possibleChoice_overappromiation());
	System.out.println("========================================");
	Hashtable<String, LinkedList<String>> matchtable = mc.match_table;
	
	if(matchtable.isEmpty()){
		System.out.println("Error: No match pairs!");
		return;
	}
	
	//should add M_s < M_s' somewhere TODO
	for(String key : matchtable.keySet()){
		if(debug)
			System.out.println("key in matchtable: " + key);
		LinkedList<String> sends = matchtable.get(key);
		match += "(or ";
		Iterator<String> ite_s = sends.iterator();
		while(ite_s.hasNext()){
			String send = ite_s.next();
			match += "(MATCH " + key + " " + send + ") "; 
		}
		match += ")\n";
	}
	match += "))\n";
	long t2 = System.currentTimeMillis();
	System.out.println("Generating Match Pair for " + ((double)(t2-t1))/(double)1000 + "seconds");
	
	in.close();
	fostream = new FileOutputStream(fileoutput);
    out = new DataOutputStream(fostream);
    bw = new BufferedWriter(new OutputStreamWriter(out));
    bw.write(TYPE + "\n" + HBdef + "\n" + MATCH + "\n" + def + "\n" + mdef + "\n" + hb + "\n" + match + "\n" + asserts + "\n(check-sat)\n");
    bw.close();
}

public static void main(String[] args)throws IOException{
	String trace = "", objective="", threads="", zero="";
	System.out.println("Specify the parameters: 1, trace address, 2, objective file address, 3, number of running threads, 4 is zero buffering?(Y or N)");
	java.util.Scanner sc = new Scanner(System.in);
	trace = sc.next();
	objective = sc.next();
	threads = sc.next();
	zero=sc.next();
	boolean isZero = (zero.equals("Y"))?true:false;
	System.out.println("Program is in process...");
	long t1 = System.currentTimeMillis();
	System.out.println("Program starts at " + t1);
	Encoding encoding = new Encoding(trace, objective, Integer.parseInt(threads),isZero/*"trace_perftest_pktuse_512.txt","perftest_pktuse_512.smt2",5,Y*/);
	encoding.GenerateEncoding();
	long t2 = System.currentTimeMillis();
	System.out.println("Program ends at " + t2);
	System.out.println("Program executes " + ((double)(t2-t1))/(double)1000 + "seconds");
	
}

}
