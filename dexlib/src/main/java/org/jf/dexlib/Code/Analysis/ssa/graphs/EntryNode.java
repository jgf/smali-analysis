/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

/**
 *
 * @author Patrick Kuhn
 */
public class EntryNode extends AbstractNode {

    @Override
    public boolean isEntry() {
        return true;
    }

    @Override
    public String toString() {
        return "ENTRY";
    }

    @Override
    public void changeVariableName(int varOriginal, String varNewName) {
        throw new UnsupportedOperationException("Not applicable to entry nodes.");
    }
}
