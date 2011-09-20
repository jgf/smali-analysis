package org.jf.dexlib.Code.Analysis.graphs;

import org.jf.dexlib.Code.Analysis.graphs.CFG.Edge.Kind;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.EdgeReversedGraph;

/**
 * This is a control dependence graph. Control dependencies are computed for nodes
 * of a control flow graph. n -> m reads as "m is control dependent on n".
 * 
 * Control dependencies tell us which nodes may control iff a certain node can be reached.
 * E.g. the following control flow graph
 *
 * <pre>
 * CFG:
 * start -> n1 -> n2 -> n -\
 *            \-> n3 -> n4 -> n5 -> exit
 * </pre>
 * results in this control dependence graph:
 * <pre>
 * CDG:
 * start -> n1 -> n2 
 *      \     \-> n 
 *       |    |-> n3 
 *       |    \-> n4 
 *       |-> n5
 *       \-> exit
 * </pre>
 * 
 * The outcome of node n1 can control if node n is executed or not, because the control
 * flow offers two alternatives at node n1: When we choose 
 * the path through node n3, n is never executed. When we choose the path through 
 * node n2, n is executed. 
 * 
 * Node n5 is not control dependent on n1, because it does not matter which path we choose,
 * we will always reach and thus execute n5. So n1 has no control over the execution of n5.
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class CDG extends DirectedPseudograph<CFG.Node, CFG.Edge> {

    private static final long serialVersionUID = -3075242412849707485L;
    
    public static CDG build(CFG cfg) {
        final CDG cdg = new CDG(cfg);
        
        cdg.build();
        
        return cdg;
    }

    private final CFG cfg;
    
    private CDG(final CFG cfg) {
        super(CFG.EDGE_FACTORY);
        this.cfg = cfg;
    }
    
    private void build() {
        final DirectedGraph<CFG.Node, CFG.Edge> reversedCfg = new EdgeReversedGraph<CFG.Node, CFG.Edge>(cfg);
        final DominanceFrontiers<CFG.Node, CFG.Edge> frontiers = DominanceFrontiers.compute(reversedCfg, cfg.getExit());

        for (final CFG.Node node : cfg.vertexSet()) {
            addVertex(node);
        }

        addEdge(cfg.getEntry(), cfg.getExit());
        
        for (final CFG.Node node : cfg.vertexSet()) {
            for (final CFG.Node domFrontier : frontiers.getDominanceFrontier(node)) {
                if (node != domFrontier) {
                    // no self dependencies
                    addEdge(domFrontier, node);
                }
            }
        }        
    }
    
    public CFG.Node getEntry() {
        return cfg.getEntry();
    }
    
    public CFG.Node getExit() {
        return cfg.getExit();
    }
    
    public CFG.Edge addEdge(CFG.Node from, CFG.Node to) {
        return addEdge(from, to, Kind.CD);
    }
    
    public CFG.Edge addEdge(CFG.Node from, CFG.Node to, CFG.Edge.Kind kind) {
        final CFG.Edge edge = CFG.EDGE_FACTORY.createEdge(from, to, kind);
        return (addEdge(from, to, edge) ? edge : null);
    }
    
    public String toString() {
        return "CDG of " + cfg.getName() + "(" + vertexSet().size() + ", " + edgeSet().size() + ")" ;
    }

}
