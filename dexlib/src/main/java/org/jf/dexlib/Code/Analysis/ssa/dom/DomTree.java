/*
 * @(#)DomTree.java
 */
package org.jf.dexlib.Code.Analysis.ssa.dom;

import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * A Dominator tree.
 * @param <V> type of nodes
 * @author Patrick Kuhn
 */
public class DomTree<V> extends SimpleDirectedGraph<V, DomEdge> {

    private static final long serialVersionUID = 1445142467229185713L;

    /**
     * Create a DomTree object.
     */
    public DomTree() {
        super(DomEdge.class);
    }

    @Override
    public String toString() {
        return "IDOM Tree";
    }
}
