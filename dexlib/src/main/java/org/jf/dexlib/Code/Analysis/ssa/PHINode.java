/*
 * @(#)PHINode.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import org.jf.dexlib.Code.Analysis.ssa.graphs.AbstractNode;

/**
 * A node containing a phi-function.
 * @author Patrick Kuhn
 */
public class PHINode extends AbstractNode {

    /** Phi function. */
    private final PHI phi;

    /**
     * Create a phi node.
     * @param phi the phi function
     */
    public PHINode(final PHI phi) {
        super();
        this.phi = phi;
    }

    @Deprecated
    public void cleanup() {
        this.phi.cleanup();
    }

    @Override
    public boolean isPHI() {
        return true;
    }

    @Override
    public PHI getPHI() {
        return phi;
    }

    @Override
    public int hashCode() {
        return phi.hashCode() * 7;
    }

    @Override
    public boolean equals(final Object o) {
        boolean result = false;
        if (o instanceof PHINode) {
            final PHINode other = (PHINode) o;
            result = phi.equals(other.phi);
        }
        return result;
    }

    @Override
    public String toString() {
        return phi.toString();
    }

    @Override
    public void changeVariableName(final int varOriginal, final String varNewName) {
        phi.changeVariableName(varOriginal, varNewName);
    }

    @Override
    public void changeDefinitionName(final String newName) {
        phi.changeDefinitionName(newName);
    }

    public String[] getOperands() {
        return phi.getOperands();
    }
}
