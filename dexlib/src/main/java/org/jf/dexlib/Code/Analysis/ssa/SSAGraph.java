/*
 * @(#)SSAGraph.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * A CDG tree.
 * @param <V> type of nodes
 * @author Patrick Kuhn
 */
public class SSAGraph<V> extends DefaultDirectedGraph<V, SSAEdge> {

    private static final long serialVersionUID = 1L;

    /**
     * Create a CDG tree object.
     */
    public SSAGraph() {
        super(SSAEdge.class);
    }

    @Override
    public String toString() {
        return "CDG Tree";
    }
}
