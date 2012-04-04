/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

/**
 *
 * @author Patrick Kuhn
 */
public class ExitNode extends AbstractNode {

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public String toString() {
        return "EXIT";
    }
}
