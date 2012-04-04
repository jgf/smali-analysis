/*
 * @(#)SSABlockTree.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import java.util.LinkedList;
import java.util.List;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * A CDG Tree which contains blocks instead of nodes.
 * @param <V> type of nodes in the blocks
 * @author Patrick Kuhn
 */
public class SSABlockTree<V> extends DefaultDirectedGraph<SSABlockNode<V>, SSAEdge> {

    private static final long serialVersionUID = 1L;
    /**CDG on which this tree is based on. */
    protected final SSAGraph<V> cdg;
    /** List to check whether certain nodes were visited already. */
    private final List<V> visited;

    /**
     * Create a CDG Block Tree. As start node the first node in the vertex set is taken.
     * @param cdg the CDG
     */
    public SSABlockTree(SSAGraph<V> cdg) {
        this(cdg, cdg.vertexSet().iterator().next());
    }

    /**
     * Create a CDG Block Tree.
     * @param cdg the CDG
     * @param start the start node
     */
    public SSABlockTree(SSAGraph<V> cdg, V start) {
        super(SSAEdge.class);
        this.cdg = cdg;
        this.visited = new LinkedList<V>();
        createTree(null, start);
    }

    /**
     * Create a block tree out of an ordinary cdg. One run of this method creates
     * a block. In recursive calls it creates further blocks and connects them.
     * @param before block which came before this one, may be <tt>null</tt>
     * @param start first node of the block
     */
    private void createTree(SSABlockNode<V> before, V start) {
        assert start != null;
        SSABlockNode<V> node = SSABlockNode.getInstance(start);
        if (!visited.contains(start)) {
            visited.add(start);
            this.addVertex(node);
            if (before != null) {
                this.addEdge(before, node);
            }

            V v = start;
            while (cdg.outDegreeOf(v) == 1) {
                v = cdg.getEdgeTarget(cdg.outgoingEdgesOf(v).iterator().next());
                if (cdg.inDegreeOf(v) > 1) {
                    break;
                }
                node.add(v);
            }

            for (SSAEdge e : cdg.outgoingEdgesOf(node.getLast())) {
                V target = cdg.getEdgeTarget(e);
                createTree(node, target);
            }
        } else {
            if (before != null && !this.containsEdge(before, node)) {
                this.addEdge(before, node);
            }
        }
    }

    /**
     * Get the block which contains a certain node.
     * @param v the node to seek
     * @return the block which contains the node or <tt>null</tt> if unsuccessful.
     */
    public SSABlockNode<V> getBlockContainingNode(V v) {
        SSABlockNode<V> result = null;
        for (SSABlockNode<V> block : this.vertexSet()) {
            if (block.contains(v)) {
                result = block;
                break;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "CDG Block Tree";
    }
}
