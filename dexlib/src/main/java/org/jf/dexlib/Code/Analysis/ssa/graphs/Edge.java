/*
 * @(#)Edge.java
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

/**
 * An edge.
 * @author Patrick Kuhn
 */
public interface Edge {

    static enum Kind {

        /* control flow */
        CF,
        /* exception control flow */
        CF_EX,
        /* control dependence */
        CD
    }

    Node getSource();

    Node getTarget();

    Kind getKind();
}
