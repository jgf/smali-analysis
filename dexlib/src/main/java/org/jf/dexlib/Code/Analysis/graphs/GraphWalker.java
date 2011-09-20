package org.jf.dexlib.Code.Analysis.graphs;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.DirectedGraph;

/**
 * Provides ways to traverse a directed graph. Currently only depth first search (DFS) is implemented.
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 * @param <V>
 * @param <E>
 */
public abstract class GraphWalker<V, E> {

    private final DirectedGraph<V, E> graph;
    
    public GraphWalker(DirectedGraph<V, E> graph) {
        this.graph = graph;
    }
    
    public final DirectedGraph<V, E> getGraph() {
        return graph;
    }
    
    /*
     * So far the recursive solution is more elegant. Better leave this turned off.
     */
    private static final boolean NO_RECURSION = true;
    
    public final void traverseDFS(final V start) {
        if (NO_RECURSION) {
            dfsNoRecrusion(start);
        } else {
            Set<V> visited = new HashSet<V>();
            dfs(start, visited);
        }
    }
    
    private void dfs(final V node, final Set<V> visited) {
        visited.add(node);
        
        discover(node);
        
        for (final E out : graph.outgoingEdgesOf(node)) {
            final V succ = graph.getEdgeTarget(out);
            if (!visited.contains(succ)) {
                dfs(succ, visited);
            }
        }
        
        finish(node);
    }

    /**
     * Is called when the node is discovered by the walker.
     * @param node The node that has been discovered.
     */
    public abstract void discover(V node);
    
    /**
     * Is called when the node is left (finished) by the walker.
     * @param node The node that is left.
     */
    public abstract void finish(V node);
    
    /*
     * A non-recursive solution of the dfs iteration. Not sure this really works...
     */
    private void dfsNoRecrusion(final V entry) {
        // iterate dfs finish time
        final Set<V> visited = new HashSet<V>(graph.vertexSet().size());
        final Stack<V> stack = new Stack<V>();
        V current = entry;

        while (current != null) {
            boolean foundOne = false;

            if (!visited.contains(current)) {
                visited.add(current);
                stack.push(current);

                discover(current);
                
                for (E succEdge : graph.outgoingEdgesOf(current)) {
                    // there may be a problem iff the successor can be the same node twice (or more)
                    // think of succ normal flow + succ exception flow. But this never happens in the current
                    // code. Even with empty catch blocks.
                    final V succ = graph.getEdgeTarget(succEdge);
                    if (!visited.contains(succ)) {

                        //XXX this is slow and should be removed with some elegant solution
                        // so far I propose to use the recursive algorithm.
                        stack.remove(succ);

                        if (!foundOne) {
                            foundOne = true;
                            current = succ;
                        } else {
                            stack.push(succ);
                        }
                    }
                }
            } else {
                // finished current node. act.
                finish(current);
            }
            
            if (!foundOne) {
                current = (stack.isEmpty() ? null : stack.pop());
            }
        }        
    }
}