/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

/**
 *
 * @author Patrick Kuhn
 */
public class CFGEdge implements Edge {

    final Node source;
    final Node target;
    final Kind kind;

    public CFGEdge(final Node source, final Node target) {
        this(source, target, Kind.CF);
    }

    public CFGEdge(final Node source, final Node target, final Kind kind) {
        this.source = source;
        this.target = target;
        this.kind = kind;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return source.hashCode() * (target.hashCode() >> 6);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CFGEdge) {
            CFGEdge other = (CFGEdge) o;
            return kind == other.kind && source.equals(other.source) && target.equals(other.target);
        }
        return false;
    }

    @Override
    public String toString() {
        return kind.name();
    }
}
