/*
 * @(#)AbstractNode.java
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ssa.PHI;

/**
 * An abstract node.
 * @author Patrick Kuhn
 */
public abstract class AbstractNode implements Node {

    public boolean isInstruction() {
        return false;
    }

    public boolean isEntry() {
        return false;
    }

    public boolean isExit() {
        return false;
    }

    public boolean isPHI() {
        return false;
    }

    public AnalyzedInstruction getInstruction() {
        throw new IllegalStateException("Not an instruction node. Use isInstruction()");
    }

    public PHI getPHI() {
        throw new IllegalStateException("Not a phi node. Use isPHI()");
    }

    public void changeVariableName(final int varOriginal, final String varNewName) {
        throw new UnsupportedOperationException("Not an instruction node. Use isInstruction()");
    }

    public void changeDefinitionName(final String newName) {
        throw new UnsupportedOperationException("Not an instruction node. Use isInstruction()");
    }

    @Override
    public abstract String toString();
}
