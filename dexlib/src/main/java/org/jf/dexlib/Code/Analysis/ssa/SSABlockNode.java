/*
 * @(#)SSABlockNode.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import java.util.Iterator;
import java.util.LinkedList;

import org.jf.dexlib.Code.Analysis.ssa.graphs.AbstractNode;
import org.jf.dexlib.Code.Analysis.ssa.graphs.Node;

/**
 * A CDG block node containing CDG nodes.
 * @param <V> 
 * @author Patrick Kuhn
 */
public class SSABlockNode<V> extends AbstractNode implements Iterable<V> {
    
    private static final LinkedList<SSABlockNode<?>> NODES = new LinkedList<SSABlockNode<?>>();
    /** Nodes in this block. */
    protected final LinkedList<V> block;
    
    /**
     * Find the block node containing a certain node.
     * @param <V> type of nodes
     * @param startNode first node of block to find
     * @return the block
     */
    @SuppressWarnings("unchecked")
    public static <V> SSABlockNode<V> getInstance(final V startNode) {
        SSABlockNode<V> result = null;
        for (SSABlockNode<?> v : NODES) {
            if (v.getFirst().equals(startNode)) {
                result = (SSABlockNode<V>) v;
                break;
            }
        }
        if (result == null) {
            result = new SSABlockNode<V>(startNode);
            NODES.add(result);
        }
        return result;
    }
    
    public static void clear() {
        NODES.clear();
    }
    
    /**
     * Create an empty block and add node.
     * @param node the first node of the block
     */
    private SSABlockNode(final V node) {
        this();
        block.add(node);
    }
    
    /**
     * Create an empty block.
     */
    public SSABlockNode() {
        super();
        this.block = new LinkedList<V>();
    }
    
    /**
     * Add a node to the end of this block.
     * @param node a node
     * @return <tt>true</tt> p.d.
     */
    public boolean add(V node) {
        return this.block.add(node);
    }
    
    /**
     * Add a node as first element in the block.
     * @param node node to add
     */
    public void addFirst(V node) {
        this.block.addFirst(node);
    }
    
    /**
     * Checks whether a node is in this block.
     * @param o a node
     * @return <tt>true</tt> if node is in this block
     */
    public boolean contains(V o) {
        return block.contains(o);
    }
    
    public V getFirst() {
        return block.getFirst();
    }
    
    public V getFirstNotPhi() {
        V v = block.getFirst();
        int i = 0;
        while (((Node) v).isPHI() && i < block.size()) {
            v = block.get(i);
            ++i;
        }
        if (!((Node) v).isPHI()) {
            return v;
        }
        return null;
    }
    
    public V getLastNotPhi() {
        V v = block.getLast();
        int i = block.size() - 1;
        while (((Node) v).isPHI() && i >= 0) {
            v = block.get(i);
            --i;
        }
        if (!((Node) v).isPHI()) {
            return v;
        }
        return null;
    }
    
    public V getLast() {
        return block.getLast();
    }
    
    /**
     * Check whether the last node in block is the EXIT node.
     * @return <tt>true</tt> if last node in block is EXIT
     */
    public boolean hasExit() {
        return ((Node)block.getLast()).isExit();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (V v : block) {
            sb.append(v.toString());
            sb.append("\\n");
        }
        return sb.substring(0, sb.length() - 2);
    }
    
    public Iterator<V> iterator() {
        return block.iterator();
    }
    
    @Override
    public void changeVariableName(int varOriginal, String varNewName) {
        throw new UnsupportedOperationException("Not possible with Block Nodes");
    }
}
