/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

import org.jf.dexlib.Code.Analysis.ssa.graphs.Edge.Kind;
import org.jgrapht.EdgeFactory;

/**
 *
 * @author Patrick Kuhn
 */
public final class CFGEdgeFactory implements EdgeFactory<Node, Edge> {

    public CFGEdge createEdge(final Node from, final Node to) {
        return new CFGEdge(from, to);
    }

    public CFGEdge createEdge(final Node from, final Node to, final Kind kind) {
        return new CFGEdge(from, to, kind);
    }
}
