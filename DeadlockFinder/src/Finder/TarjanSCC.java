package Finder;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import Syntax.Operation;

public class TarjanSCC {

    private boolean[] marked;        // marked[v] = has v been visited?
    
    //
    private LinkedList<HashSet<Operation>> sccs;
    public HashSet<Operation> leastSCC;
    public int leastvertex;
    private int[] low;               // low[v] = low number of v
    private int pre;                 // preorder number counter
    private int count;               // number of strongly-connected components
    private Stack<Operation> stack;
    Digraph G;
    
    //used to filter any vertices lower than lowerbound and any edge connecting those vertices
    private int lowerbound;
    

    /**
     * Computes the strong components of the digraph <tt>G</tt>.
     * @param G the digraph
     */
    public TarjanSCC(Digraph Graph, int lb) {
    	lowerbound = lb;
    	G = Graph;
    	int Vsize = G.getVSize();
        marked = new boolean[Vsize];
        stack = new Stack<Operation>();
        sccs = new LinkedList<HashSet<Operation>>(); 
        leastSCC = null;
        leastvertex = lowerbound;
        low = new int[Vsize];
        
        int minrank = Integer.MAX_VALUE;
        
        //only start from lowerbound
        for (int i = lowerbound; i < Vsize; i++) {
            if (!marked[i]) dfs(G, G.Vlist.get(i),minrank);
        }

        // check that id[] gives strong components
//        assert check(G);
    }

    
//    public LinkedList<Set<V>> toCircles()
//    {
//    	LinkedList<Set<V>> circles = new LinkedList<Set<V>>();
//    	for(Hashtable<Integer, Set<Operation>> table : id)
//    	{
//    		if(table.size() > 1)
//    		{
//    			for(Integer pRank : table.keySet())
//    			{
//					Set<V> vset = table.get(pRank);
//    				if(circles.size() == 0)
//    				{
//    					for(V v : vset)
//    					{
//    						HashSet<V> tempset = new HashSet<V>();
//    						tempset.add(v);
//    						circles.add(tempset);
//    					}
//    				}
//    				else
//    				{
//    					//for every element in vset, add a copy of existing circle set
//    					int existingSize = circles.size();
//    					for(int i = 0; i < existingSize; i++)
//    					{
//    						Set<V> temp = circles.removeFirst();
//    						for(V v : vset)
//    						{
//    							Set<V> newset = new HashSet<V>();
//    							newset.addAll(temp);
//    							newset.add(v);
//    							circles.add(newset);
//    						}
//    					}
//    				}
//    			}
//    		}
//    	}
//    	
//    	return circles;
//    	
//    }

    private void dfs(Digraph G, Operation v, int minrank) { 
        int vrank = G.Vlist.indexOf(v);
    	marked[vrank] = true;
        low[vrank] = pre++;
        int min = low[vrank];
        stack.push(v);
        if(G.adj(v)!=null){
	        for (Operation w : G.adj(v)) {
	        	//only consider all the edges with vertices >= lowerbound
	        	if(G.Vlist.indexOf(w) >= lowerbound)
	        	{
		        	int wrank = G.Vlist.indexOf(w);
		            if (!marked[wrank]) dfs(G, w, minrank);
		            if (low[wrank] < min) min = low[wrank];
	            }
	        }
        }
        if (min < low[vrank]) { low[vrank] = min; return; }
        Operation w;
        int minlocal = Integer.MAX_VALUE;
        do {
            w = stack.pop();
            int wrank = G.Vlist.indexOf(w);
            if(wrank < minlocal)
            {
            	minlocal = wrank;
            }
            if(sccs.size() <= count)
            	sccs.add(new HashSet<Operation>());
            sccs.get(count).add(w);
//            id[wrank] = count;
            low[wrank] = G.getVSize();
        } while (w != v);
        
        //this scc is assigned leastSCC iff it has the least vertex and the size of this scc > 1 
        if(minlocal < minrank && sccs.get(count).size() > 1)
        {
        	minrank = minlocal;
        	leastvertex = minrank;
        	leastSCC = sccs.get(count);
        }
        count++;
    }

    
    

    /**
     * Returns the number of strong components.
     * @return the number of strong components
     */
    public int count() {
        return count;
    }


//    /**
//     * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
//     * @param v one vertex
//     * @param w the other vertex
//     * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the same
//     *     strong component, and <tt>false</tt> otherwise
//     */
//    public boolean stronglyConnected(int v, int w) {
//        return id[v] == id[w];
//    }

    /**
     * Returns the component id of the strong component containing vertex <tt>v</tt>.
     * @param v the vertex
     * @return the component id of the strong component containing vertex <tt>v</tt>
     */
//    public int id(V v) {
//    	int vrank = G.Vlist.indexOf(v);
//        return id[vrank];
//    }
    
//    public void printSCC()
//    {
//    	for(int i = 0; i < id.size(); i++)
//    	{
//    		System.out.println("SCC[" + i + "]:");
//    		for(Integer processRank : id.get(i).keySet())
//    		{
//    			System.out.print("P" + processRank + ":{");
//    			Set<Operation> vset = id.get(i).get(processRank);
//    			for(Operation v : vset)
//    			{
//    				System.out.print(v + " ");
//    			}
//    			System.out.print("}\t");
//    		}
//    		System.out.println();
//    	}
//    }
    
    public void printCircles(LinkedList<Set<V>> circles)
    {
    	for(Set<V> vset : circles)
    	{
    		System.out.println("Circle[" + circles.indexOf(vset) + "]:");
    		System.out.print("{");
    		for(V v : vset)
    		{
    			System.out.print(v + " ");
    		}
    		System.out.println("}");
    	}
    }

    // does the id[] array contain the strongly connected components?
//    private boolean check(Digraph G) {
//        TransitiveClosure tc = new TransitiveClosure(G);
//        for (int v = 0; v < G.V(); v++) {
//            for (int w = 0; w < G.V(); w++) {
//                if (stronglyConnected(v, w) != (tc.reachable(v, w) && tc.reachable(w, v)))
//                    return false;
//            }
//        }
//        return true;
//    }

    /**
     * Unit tests the <tt>TarjanSCC</tt> data type.
     */
//    public static void main(String[] args) {
//        In in = new In(args[0]);
//        Digraph G = new Digraph(in);
//        TarjanSCC scc = new TarjanSCC(G);
//
//        // number of connected components
//        int M = scc.count();
//        StdOut.println(M + " components");
//
//        // compute list of vertices in each strong component
//        Queue<Integer>[] components = (Queue<Integer>[]) new Queue[M];
//        for (int i = 0; i < M; i++) {
//            components[i] = new Queue<Integer>();
//        }
//        for (int v = 0; v < G.V(); v++) {
//            components[scc.id(v)].enqueue(v);
//        }
//
//        // print results
//        for (int i = 0; i < M; i++) {
//            for (int v : components[i]) {
//                StdOut.print(v + " ");
//            }
//            StdOut.println();
//        }
//
//    }

}