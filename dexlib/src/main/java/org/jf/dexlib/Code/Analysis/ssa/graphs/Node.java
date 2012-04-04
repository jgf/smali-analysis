/*
 * @(#)Node.java
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ssa.PHI;

/**
 * This interface represents a node.
 * @author Patrick Kuhn
 */
public interface Node {

    /**
     * Returns whether this node contains an instruction.
     * @return <tt>true</tt> if this node is an instruction.
     */
    boolean isInstruction();

    /**
     * Returns whether this is the entry node of the programme.
     * @return <tt>true</tt> if entry node
     */
    boolean isEntry();

    /**
     * Returns whether this is the exit node of the programme.
     * @return <tt>true</tt> if exit node
     */
    boolean isExit();

    /**
     * Returns whether this is a phi node.
     * @return <tt>true</tt> if phi node
     */
    boolean isPHI();

    /**
     * Returns the instruction in this node if contained.
     * @return the instruction
     */
    AnalyzedInstruction getInstruction();

    /**
     * Returns the PHI-function of this node if contained.
     * @return 
     */
    PHI getPHI();
    
    /**
     * Change variable name.
     * @param varOriginal original variable name
     * @param varNewName new variable name
     */
    void changeVariableName(int varOriginal, String varNewName);
    
    /**
     * Change definition name.
     * @param newName new definition name
     */
    void changeDefinitionName(String newName);
    
}
