package Finder;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import Syntax.Operation;
import Syntax.Recv;

public class Johnson 
{    
    public LinkedList<Hashtable<Integer, Recv>> patterns;
    private Stack<Operation> stack;
    Digraph G;
    private int leastvertex;
    

    /**
     * Computes the strong components of the digraph <tt>G</tt>.
     * @param G the digraph
     */
    public Johnson(Digraph Graph) {
    	G = Graph;
        stack = new Stack<Operation>();
        patterns = new LinkedList<Hashtable<Integer, Recv>>(); 
        find();
    }
    
    public HashSet<Operation> leastSCC(int lowerbound)
    {
    	TarjanSCC tarjan = new TarjanSCC(G, lowerbound);
    	leastvertex = tarjan.leastvertex;
    	//the result could be null
    	return tarjan.leastSCC;
    }
    
    public void find()
    {
    	Hashtable<Operation, Boolean> blocked = new Hashtable<Operation, Boolean>();
        Hashtable<Operation, List<Operation>> blockedNodes = new Hashtable<Operation, List<Operation>>();
    	stack.empty();
    	int s = 0;
    	while(s < G.Vlist.size()-1)
    	{
    		Set<Operation> leastSCC = leastSCC(s);
    		if(leastSCC != null)
    		{
    			s = leastvertex;
    			for(Operation v : leastSCC)
    			{
    				blocked.put(v, false);
                    blockedNodes.put(v, new LinkedList<Operation>());
    			}
    			
                boolean dummy = circuit(leastSCC, s, s, stack, blocked, blockedNodes);
                s++;
    		}
    		else
    		{
    			s = G.Vlist.size()-1;
    		}
    	}
    	
    }
    
    public boolean circuit(Set<Operation> dg, int v, int s, Stack<Operation> stack, 
    		Hashtable<Operation, Boolean> blocked,
    		Hashtable<Operation, List<Operation>> blockedNodes) 
    {
    	if (dg == null) { return false; }
        if (dg.size() == 0) { return false; }
        boolean f = false;
        Operation vertex = G.Vlist.get(v);
        Operation startvertex = G.Vlist.get(s);
        stack.push(vertex);
        blocked.put(vertex, true);
        HashSet<Operation> adj_leastSCC = new HashSet<Operation>(); //all the operation in leastSCC that v can connect
        continuepoint:
        for (Operation w : G.Etable.get(vertex)) {
        	//only vertex in leastSCC is considered
        	if(!dg.contains(w))
        		continue continuepoint;
        	adj_leastSCC.add(w);
            if (w == startvertex) {
                stack.push(startvertex);
                //pattern generation based on the detected circuit
                Hashtable<Integer, Recv> pattern = new Hashtable<Integer, Recv>();
                for(Operation op : stack)
                {
                	//add the first receive of each process in stack to the pattern
                	if(!(op instanceof Recv))
                		continue;
                	Recv r = (Recv)op;
                	if(!pattern.containsKey(r.dest))
                	{
                		pattern.put(r.dest, r);
                		continue;
                	}
                	//only keep the receive with lower rank
                	if(pattern.get(r.dest).rank > r.rank)
                	{
                		pattern.put(r.dest, r);
                	}
                }
                if(pattern.size() > 1)
                	patterns.add(pattern);
                stack.pop();
                f = true;
            }
            else {
                if (! blocked.get(w)) {
                    if (circuit(dg, G.Vlist.indexOf(w), s, stack,blocked,blockedNodes)) { f = true; }
                }
            }
        }
        if (f) { unblock(vertex,blocked,blockedNodes); }
        else {
            for (Operation w : adj_leastSCC) {
                if (! blockedNodes.get(w).contains(vertex)) {
                    blockedNodes.get(w).add(vertex);
                }
            }
        }
        stack.pop();
        return f;
    }
    
    //recursion 
    public void unblock(Operation v, Hashtable<Operation, Boolean> blocked,
    		Hashtable<Operation, List<Operation>> blockedNodes)
    {
    	blocked.put(v, false);
        while (blockedNodes.get(v).size() > 0) {
            Operation w = blockedNodes.get(v).remove(0);
            if (blocked.get(w)) {
                unblock(w,blocked,blockedNodes);
            }
        }
    }
    
    public void printCircles(LinkedList<Hashtable<Integer, Set<Operation>>> circles)
    {
    	for(int i = 0; i < circles.size(); i++)
    	{
    		System.out.print("Circle[" + i + "]:");
    		System.out.println(circles.get(i));
    	}
    }

}
