/*
 * @(#)Dominators.java
 */
package org.jf.dexlib.Code.Analysis.ssa.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;

/**
 * Computation of dominators in a flow graph. Algorithm see "A fast algorithm
 * for finding dominators in a flowgraph" from Lengauer and Tarjan, TOPLAS 1979
 * 
 * The dominators (DOM) of a node n in a directed graph with a unique start node (called flowgraph)
 * are all nodes that lie on every path from the start node to node n. Including the node n itself.
 * So when you cut the graph at any of node n's dominators, n cannot be reached from the
 * start anymore.
 * 
 * Strict dominators (SDOM) of a node n are the same as dominators of node n, excluding the node n itself.
 * 
 * As all nodes in the flowgraph have to be reachable from the start node (per definition), every node
 * has at least a single strict dominator - except the start node itself.
 * 
 * So for each node n the set of strict dominantors SDOM(n) always contains a single dominator
 * called immediate dominator (IDOM). This is the only node in SDOM(n) that does not dominate any other
 * node in SDOM(n). There is always exactly one node that fulfills this property (theres a proof -> google)
 * This node the dominator that is "closest" to n. 
 * 
 * E.g.
 * <pre>
 * start -> n1 -> n2 -> n -\
 *            \-> n3 -> n4 -> n5
 *           
 * DOM(n)  = {start, n1, n2, n}
 * SDOM(n) = {start, n1, n2}
 * IDOM(n) = n2
 * 
 * SDOM(n5) = {start, n1}
 * IDOM(n5) = n1
 * </pre>
 * 
 * As every node has a single immediate dominator, we can construct a graph in tree form where the
 * parent of each node is its immediate dominator. This tree is called dominator tree.
 * 
 * E.g.
 * <pre>
 * start -> n1 -> n2 -> n
 *      |     \-> n3 -> n4
 *      \-> n5 
 * </pre>
 * 
 * @param <V> Type of the nodes in the flowgraph
 * @param <E> Type of the edges in the flowgraph
 * 
 * @author Patrick Kuhn
 */
public final class Dominators<V, E> {

    // Input stuff
    /** The CFG. */
    protected final DirectedGraph<V, E> graph;
    /** Start node. */
    protected final V start;
    // DOM Stuff
    /** The DOM Tree. */
    protected final DomTree<V> domTree;
    /** Number of nodes. */
    protected int N;
    protected final Map<V, Integer> dfnum;
    protected final Map<V, Set<V>> bucket;
    protected final Map<V, V> semi;
    protected final Map<V, V> idom;
    protected final Map<V, V> samedom;
    protected final Map<Integer, V> vertex;
    protected final Map<V, V> parent;
    protected final Map<V, V> ancestor;
    /** The DF. */
    protected Map<V, Set<V>> domFrontier;
    private Set<V> visited;

    /**
     * Compute the DOM.
     * @param <Y> type of vertices
     * @param <Z> type of edges
     * @param graph the control flow graph to convert
     * @param entry first node of CFG
     * @return the DOM
     */
    public static <Y, Z> Dominators<Y, Z> compute(DirectedGraph<Y, Z> graph, Y entry) {
        if (graph == null || entry == null) {
            throw new IllegalArgumentException("Must not be null!");
        }
        Dominators<Y, Z> dom = new Dominators<Y, Z>(graph, entry);
        dom.compute();
        return dom;
    }

    /**
     * Constructor. <tt>compute</tt> must be called afterwards!
     * @param graph the CFG
     * @param start the first node
     */
    @SuppressWarnings("unchecked")
    private Dominators(final DirectedGraph<V, E> graph, final V start) {
        assert graph != null && start != null;
        this.graph = graph;
        this.start = start;
        this.domTree = new DomTree<V>();

        this.N = 0;
        // forall n : dfnum[n] := 0
        // forall n : bucket[n] := { }
        this.dfnum = new HashMap<V, Integer>(graph.vertexSet().size());
        this.bucket = new HashMap<V, Set<V>>(graph.vertexSet().size());
        this.semi = new HashMap<V, V>(graph.vertexSet().size());
        this.idom = new HashMap<V, V>(graph.vertexSet().size());
        this.samedom = new HashMap<V, V>(graph.vertexSet().size());
        this.vertex = new HashMap<Integer, V>(graph.vertexSet().size());
        this.parent = new HashMap<V, V>(graph.vertexSet().size());
        this.ancestor = new HashMap<V, V>(graph.vertexSet().size());
        int i = 0;
        for (V node : graph.vertexSet()) {
            dfnum.put(node, 0);
            bucket.put(node, new HashSet<V>());
            idom.put(node, null);
            samedom.put(node, null);
            vertex.put(i, node);
            ++i;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="DOM calculation">
    /**
     * Compute the DOM.
     */
    @SuppressWarnings("unchecked")
    protected void compute() {
        // correct
        dfs(null, start);

        V n = null;
        for (int i = N - 1; i >= 1; --i) {
            n = vertex.get(i);
            V p = parent.get(n);
            V s = p;

            // Calculate semidominator of n
            // for each predecessor v of n
            for (E e : graph.incomingEdgesOf(n)) {
                V v = graph.getEdgeSource(e);
                V sn;
                if (dfnum.get(v) <= dfnum.get(n)) {
                    sn = v;
                } else {
                    sn = semi.get(ancestorWithLowestSemi(v));
                }
                assert sn != null;
                if (dfnum.get(sn) < dfnum.get(s)) {
                    s = sn;
                }
            }

            // link path from s to n to the forest
            semi.put(n, s);
            bucket.get(s).add(n);
            link(p, n);

            for (V v : bucket.get(p)) {
                V y = ancestorWithLowestSemi(v);
                if (semi.get(y) == semi.get(v)) {
                    idom.put(v, p);
                } else {
                    samedom.put(v, y);
                }
            }
            bucket.get(p).clear();
        }
        for (int i = 1; i < N; ++i) {
            n = vertex.get(i);
            if (samedom.get(n) != null) {
                idom.put(n, idom.get(samedom.get(n)));
            }
        }

        // now build the tree
        for (V v : idom.keySet()) {
            V id = idom.get(v);
            addEdge(id, v);
        }


        // calculate DF
        domFrontier = new HashMap<V, Set<V>>();
        visited = new HashSet<V>();
        computeDF(start);
    }

    /**
     * In the forest, find the non-root ancestor of <tt>v</tt> that has
     * the lowest-numbered semidominator.
     * @param node the vertex
     * @return non-root ancestor of <tt>v</tt> that has the lowest-numbered semidominator.
     */
    protected V ancestorWithLowestSemi(final V node) {
        // correct
        V u = node;
        V v = node;
        // while ancestor[v] != none
        while (ancestor.get(v) != null) {
            V semiV = semi.get(v);
            V semiU = semi.get(u);
            if (dfnum.get(semiV) < dfnum.get(semiU)) {
                u = v;
            }
            v = ancestor.get(v);
        }
        return u;
    }

    private void dfs(V p, V n) {
        assert n != null;
        if (dfnum.get(n) == 0) {
            dfnum.put(n, N);
            vertex.put(N, n);
            parent.put(n, p);
            ++N;
            for (E e : graph.outgoingEdgesOf(n)) {
                V w = graph.getEdgeTarget(e);
                dfs(n, w);
            }
        }
    }

    /**
     * Compute the DF for a node. One should start with the start node!!
     * @param node the node to compute the DF for
     */
    private void computeDF(V node) {
        assert node != null;
        visited.add(node);

        Set<V> set = new HashSet<V>();
        // compute DFlocal[n]
        for (E e : graph.outgoingEdgesOf(node)) {
            V y = graph.getEdgeTarget(e);
            // if idom[y] != n
            if (!idom.get(y).equals(node)) {
                set.add(y);
            }
        }
        // compute DFup[n]
        // children[n] == nodes whose immediate dominator n is
        for (V child : getChildren(node)) {
            if (!visited.contains(child)) {
                computeDF(child);
            }
            for (V w : domFrontier.get(child)) {
                if (node.equals(w) || !dominates(node, w)) {
                    set.add(w);
                }
            }
        }
        domFrontier.put(node, set);
    }

    private Set<V> getChildren(final V n) {
        Set<V> result = new HashSet<V>();
        for (DomEdge e : domTree.outgoingEdgesOf(n)) {
            V c = domTree.getEdgeTarget(e);
            result.add(c);
            result.addAll(getChildren(c));
        }
        return result;
    }

    /**
     * Check whether n dominates w.
     * @param n possible dominator to test
     * @param w node which is dominated
     * @return  <tt>true</tt> if <tt>n dom w</tt> or <tt>n == w</tt>
     */
    public boolean dominates(final V n, final V w) {
        if (w == null) {
            return false;
        } else if (n.equals(w)) {
            return true;
        }
        return dominates(n, getIDom(w));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="helper">
    /**
     * Add an Edge to the Dominator Tree.
     * If one or both of the vertices does not exist in this graph, they are added.
     * @param p source node
     * @param n target node
     */
    protected void addEdge(final V p, final V n) {
        if (p == null || n == null) {
            return;
        }
        if (!domTree.containsVertex(n)) {
            domTree.addVertex(n);
        }
        if (!domTree.containsVertex(p)) {
            domTree.addVertex(p);
        }
        domTree.addEdge(p, n);
    }

    /**
     * Link two nodes.
     * @param p source node
     * @param n destination node
     */
    protected void link(final V p, final V n) {
        assert p != null && n != null;
        ancestor.put(n, p);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="getter">
    public V getIDom(final V node) {
        return idom.get(node);
    }

    public Iterable<V> getNodesWithIDom(final V node) {
        //TODO: getNodesWithIDOM
        throw new UnsupportedOperationException("getNodesWithIDom not yet implemented!");
    }

    public DomTree<V> getDominationTree() {
        return domTree;
    }

    public Map<V, Set<V>> getDF() {
        return Collections.unmodifiableMap(domFrontier);
    }

    public V getStart() {
        return start;
    }
    //</editor-fold>
}
