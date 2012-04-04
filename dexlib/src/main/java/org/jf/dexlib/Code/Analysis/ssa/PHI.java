/*
 * @(#)PHI.java
 */
package org.jf.dexlib.Code.Analysis.ssa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a phi-function.
 * @author Patrick Kuhn
 */
public class PHI {

    private final int originDestination;
    private String destination;
    private String[] parameters;
    private boolean cleanedUp;

    /**
     * Create a Phi-instruction.
     * a <- Phi(b, c, d, ...)
     * @param destination a
     * @param parameters b, c, d, ...
     */
    public PHI(final int destination, final List<Integer> parameters) {
        this.originDestination = destination;
        this.destination = Integer.toString(destination);
        this.parameters = new String[parameters.size()];
        int i = 0;
        for (Integer j : parameters) {
            this.parameters[i] = Integer.toString(j);
            ++i;
        }
        this.cleanedUp = false;
    }

    public PHI(final int destination, final int numberOfParameters) {
        this.originDestination = destination;
        this.destination = Integer.toString(destination);
        this.parameters = new String[numberOfParameters];

        for (int i = 0; i < numberOfParameters; ++i) {
            this.parameters[i] = this.destination;
        }
    }

    public void changeVariableName(final int varOriginal, final String varNewName) {
        if (cleanedUp) {
            throw new IllegalStateException("Already cleaned up, no more change possible!");
        }
        this.parameters[varOriginal] = varNewName;
    }

    public void changeDefinitionName(final String newName) {
        if (cleanedUp) {
            throw new IllegalStateException("Already cleaned up, no more change possible!");
        }
        this.destination = newName;
    }

    public String getDestination() {
        return destination;
    }

    /**
     * Get destination register name before any renaming.
     * @return the origin destination
     */
    public int getOriginDestination() {
        return originDestination;
    }

    /**
     * Get the current operands.
     * @return the operands
     */
    public String[] getOperands() {
        return Arrays.copyOf(parameters, parameters.length);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("v").append(destination);
        sb.append(" = PHI(");
        boolean flag = false;
        for (String i : parameters) {
            sb.append("v").append(i);
            sb.append(", ");
            flag = true;
        }
        if (flag) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * 
     */
    @Deprecated
    void cleanup() {
        cleanedUp = true;
        Set<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(parameters));
        if (set.size() < parameters.length) {
            parameters = set.toArray(new String[0]);
        }
    }
}
