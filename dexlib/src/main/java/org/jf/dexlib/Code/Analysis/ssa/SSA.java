/*
 * @(#)SSA.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jf.dexlib.Code.Analysis.ssa.dom.Dominators;
import org.jf.dexlib.Code.Analysis.ssa.graphs.AbstractNode;
import org.jf.dexlib.Code.Analysis.ssa.graphs.CFG;
import org.jf.dexlib.Code.Analysis.ssa.graphs.InstructionNode;
import org.jf.dexlib.Code.Analysis.ssa.graphs.Node;
import org.jgrapht.DirectedGraph;

/**
 *
 * @param <V> 
 * @param <E> 
 * @author Patrick Kuhn
 */
public final class SSA<V, E> {

    protected final CFG cfg;
    protected final Dominators<V, E> dom;
    protected final Map<V, Set<V>> domFrontier;
    protected final SSAGraph<V> cdgTree;
    protected final SSABlockTree<V> cdgBlockTree;
    // place part
    protected Set<Integer> variables = new HashSet<Integer>();
    // rename part
    private final Map<Integer, Integer> count;
    private final Map<Integer, LinkedList<Integer>> stack;
    private Set<SSABlockNode<V>> visited;

    @SuppressWarnings("unchecked")
    public static <Y, Z> SSA<Y, Z> compute(DirectedGraph<Y, Z> graph, Dominators<Y, Z> dom) {
        if (graph == null || dom == null) {
            throw new IllegalArgumentException();
        }
        SSA<Y, Z> cdg = new SSA<Y, Z>(graph, dom);
        cdg.place();
        cdg.rename();
        return cdg;
    }

    @SuppressWarnings("unchecked")
    protected SSA(DirectedGraph<V, E> graph, Dominators<V, E> dom) {
        if (!(graph instanceof CFG)) {
            throw new IllegalArgumentException();
        }
        this.cfg = (CFG) graph;
        this.dom = dom;
        this.domFrontier = dom.getDF();
        this.cdgTree = new SSAGraph<V>();
        for (V v : graph.vertexSet()) {
            cdgTree.addVertex(v);
        }
        for (E e : graph.edgeSet()) {
            cdgTree.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
        }
        this.cdgBlockTree = new SSABlockTree<V>(this.cdgTree, dom.getStart());
        // gather variables
        discoverVariables();
        //this.count = new int[variables.size()];
        this.count = new HashMap<Integer, Integer>(variables.size());
        this.stack = new HashMap<Integer, LinkedList<Integer>>(variables.size());
        // for each variable a
        for (int i : variables) {
            //count[i] = 0;
            count.put(i, 0);
            stack.put(i, new LinkedList<Integer>());
            stack.get(i).add(0); // push 0 onto Stack[a]
        }
    }

    /**
     * Get the SSA CFG.
     * @return SSA
     */
    public SSAGraph<V> getSSAGraph() {
        return cdgTree;
    }

    /**
     * Get the SSA CFG with blocks instead of single instructions.
     * If the algorithm for placing and renaming was not yet started etc. an
     * <tt>IllegalStateException</tt> is thrown.
     * @return SSA w/ blocks
     */
    public SSABlockTree<V> getSSAGraphBlocks() {
        return cdgBlockTree;
    }

    /**
     * Find all variables in the CFG and return as Set
     * @return Set of variables
     */
    private void discoverVariables() {
        variables = new HashSet<Integer>();
        for (Node n : cfg.vertexSet()) {
            if (n.isInstruction()) {
                String ss = ((InstructionNode) n).getDestinationRegister();
                if (ss != null) {
                    try {
                        variables.add(Integer.parseInt(ss));
                    } catch (NumberFormatException ex) {
                        throw new AssertionError(ex);
                    }
                }
                String[] parameters = ((InstructionNode) n).getSourceRegisters();
                if (parameters != null) {
                    for (String s : parameters) {
                        variables.add(Integer.parseInt(s));
                    }
                }
            }
        }
    }

    private void place() {
        final Map<Integer, Set<SSABlockNode<V>>> defsites = new HashMap<Integer, Set<SSABlockNode<V>>>();
        // variables defined in n
        final Map<SSABlockNode<V>, Set<Integer>> a_orig = new HashMap<SSABlockNode<V>, Set<Integer>>();
        for (SSABlockNode<V> block : cdgBlockTree.vertexSet()) {
            Set<Integer> s = new HashSet<Integer>();
            for (V v : block) {
                if (v instanceof InstructionNode) {
                    final InstructionNode in = (InstructionNode) v;
                    String destReg = in.getDestinationRegister();
                    if (destReg != null) {
                        s.add(Integer.parseInt(destReg));
                    }
                }
            }
            a_orig.put(block, s);
        }
        final Map<SSABlockNode<V>, Set<Integer>> a_phi = new HashMap<SSABlockNode<V>, Set<Integer>>();
        // for each block node n
        for (SSABlockNode<V> n : cdgBlockTree.vertexSet()) {
            // for each variable a in Aorig[n]
            for (Integer a : a_orig.get(n)) {
                Set<SSABlockNode<V>> defA = defsites.get(a);
                if (defA == null) {
                    defA = new HashSet<SSABlockNode<V>>();
                    defsites.put(a, defA);
                }
                defA.add(n);
            }
        }

        for (int a : variables) {
            LinkedList<SSABlockNode<V>> w = new LinkedList<SSABlockNode<V>>();
            if (defsites.get(a) != null) {
                w.addAll(defsites.get(a));
            }
            while (!w.isEmpty()) {
                // remove some node n from W
                SSABlockNode<V> n = w.pop();
                // for each y in DF[n]
                Set<V> dfs = new HashSet<V>();
                for (V v : n) {
                    if (domFrontier.get(v) != null) {
                        dfs.addAll(domFrontier.get(v));
                    }
                }
                for (V v : dfs) {
                    // avoid exit node
                    if (!((Node) v).isExit()) {
                        SSABlockNode<V> y = cdgBlockTree.getBlockContainingNode(v);
                        // if a not in A_phi[y]
                        if (a_phi.get(y) == null || !a_phi.get(y).contains(a)) {
                            int numberOfParas = cdgBlockTree.inDegreeOf(y);
                            PHI phi = new PHI(a, numberOfParas);
                            @SuppressWarnings("unchecked")
                            V p = (V) new PHINode(phi);
                            insertFirstInBlock(y, p);
                            if (a_phi.get(y) == null) {
                                a_phi.put(y, new HashSet<Integer>());
                            }
                            a_phi.get(y).add(a);
                            if (!a_orig.get(y).contains(a)) {
                                w.add(y);
                            }
                        }
                    }
                }
            }
        }
    }

    private void insertFirstInBlock(final SSABlockNode<V> block, final V node) {
        // insert in cdg and cdgblocktree to avoid rebuilding
        // SSABlockTree
        block.addFirst(node);
        // SSAGraph
        V first = block.getFirstNotPhi();
        Set<SSAEdge> inEdges = cdgTree.incomingEdgesOf(first);
        Set<V> predecessors = new HashSet<V>();
        for (SSAEdge e : inEdges) {
            predecessors.add(cdgTree.getEdgeSource(e));
        }
        cdgTree.addVertex(node);
        cdgTree.addEdge(node, first);
        for (V v : predecessors) {
            cdgTree.addEdge(v, node);
            cdgTree.removeEdge(v, first);
        }
    }

    private void rename() {
        visited = new HashSet<SSABlockNode<V>>();
        // EXIT node must be removed.
        V end = null;
        SSABlockNode<V> endBlock = null;
        for (V n : cdgTree.vertexSet()) {
            if (((Node) n).isExit()) {
                end = n;
                endBlock = SSABlockNode.getInstance(end);
                break;
            }
        }
        Set<V> pre = new HashSet<V>();
        Set<SSABlockNode<V>> preBlock = new HashSet<SSABlockNode<V>>();
        if (end != null) {
            for (SSAEdge e : cdgTree.incomingEdgesOf(end)) {
                pre.add(cdgTree.getEdgeSource(e));
            }
            for (SSAEdge e : cdgBlockTree.incomingEdgesOf(endBlock)) {
                preBlock.add(cdgBlockTree.getEdgeSource(e));
            }
            cdgTree.removeVertex(end);
            cdgBlockTree.removeVertex(endBlock);
        }
        // rename
        rename(cdgBlockTree.getBlockContainingNode(dom.getStart()));
        // add EXIT node again
        cdgTree.addVertex(end);
        cdgBlockTree.addVertex(endBlock);
        for (V v : pre) {
            cdgTree.addEdge(v, end);
        }
        for (SSABlockNode<V> v : preBlock) {
            cdgBlockTree.addEdge(v, endBlock);
        }
    }

    private void rename(SSABlockNode<V> n) {
        for (V s : n) {
            Node sNode = (Node) s;
            // if S is not a phi function
            if (sNode.isInstruction()) {
                InstructionNode ins = (InstructionNode) s;
                // for each use of some variable x in S
                if (ins.getSourceRegisters() != null) {
                    for (String xStr : ins.getSourceRegisters()) {
                        // XXX: check
                        try {
                            int x = Integer.parseInt(xStr);
                            int i = stack.get(x).peek();
                            ins.changeVariableName(x, x + "_" + i);
                        } catch (NumberFormatException ex) {
                            // TODO
                        }
                    }
                }
            }
            if (sNode.isInstruction() || sNode.isPHI()) {
                // for each definition of some variable a in S
                String aStr = null;
                AbstractNode ins = null;
                if (sNode.isInstruction()) {
                    ins = (InstructionNode) s;
                    aStr = ((InstructionNode) s).getDestinationRegister();
                } else if (sNode.isPHI()) {
                    ins = (PHINode) s;
                    aStr = ((PHINode) s).getPHI().getDestination();
                }
                if (aStr != null && ins != null) {
                    try {
                        int a = Integer.parseInt(aStr);
                        //++count[a];
                        count.put(a, count.get(a) + 1);
                        //int i = count[a];
                        int i = count.get(a);
                        stack.get(a).push(i);
                        ins.changeDefinitionName(a + "_" + i);
                    } catch (NumberFormatException ex) {
                        // TODO
                    }
                }
            }
        }

        // for each successor Y of block n
        for (SSAEdge e : cdgBlockTree.outgoingEdgesOf(n)) {
            final SSABlockNode<V> succY = cdgBlockTree.getEdgeTarget(e);
            // find j
            Set<SSAEdge> edges = cdgBlockTree.incomingEdgesOf(succY);
            int j = 0;
            for (SSAEdge edge : edges) {
                SSABlockNode<V> source = cdgBlockTree.getEdgeSource(edge);
                if (source.equals(n)) {
                    break;
                }
                ++j;
            }
            for (V v : succY) {
                if (((Node) v).isPHI()) {
                    PHINode phi = (PHINode) v;
                    String s = phi.getOperands()[j];
                    try {
                        Integer i = stack.get(Integer.parseInt(s)).peek();
                        if (i != null) {
                            phi.changeVariableName(j, phi.getOperands()[j] + "_" + i);
                        }
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(SSA.class.toString()).log(Level.FINE, null, ex);
                    }
                }
            }
        }

        // for each child X of n
        // TODO: should be faster with dominators
        // XXX: child of n are nodes whose idom is n
        for (SSAEdge e : cdgBlockTree.outgoingEdgesOf(n)) {
            final SSABlockNode<V> childX = cdgBlockTree.getEdgeTarget(e);
            visited.add(n);
            if (!visited.contains(childX)) {
                rename(childX);
            }
        }

        // for each statement S in block n
        for (V s : n) {
            Node sNode = (Node) s;
            if (sNode.isInstruction()) {
                InstructionNode ins = (InstructionNode) s;
                String dest = ins.getDestinationRegister();
                if (dest != null) {
                    dest = dest.substring(0, dest.indexOf('_'));
                    try {
                        int a = Integer.parseInt(dest);
                        if (!stack.get(a).isEmpty()) {
                            stack.get(a).pop();
                        }
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(SSA.class.toString()).log(Level.FINE, null, ex);
                        System.err.println(ex);
                    }
                }
            }
        }
    }

    /**
     * Remove duplicates of registers in PHINodes.
     */
    @Deprecated
    private void cleanUp() {
        for (V v : cdgTree.vertexSet()) {
            if (((Node) v).isPHI()) {
                ((PHINode) v).cleanup();
            }
        }
    }
}
