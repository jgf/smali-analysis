/*
 * @(#)InstructionNode.java
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

import java.util.Arrays;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Code.ThreeRegisterInstruction;
import org.jf.dexlib.Code.TwoRegisterInstruction;

/**
 *
 * @author Patrick Kuhn
 */
public class InstructionNode extends AbstractNode {

    private static int INDEX = 0;
    private final int idx;
    /** Instruction. */
    private final AnalyzedInstruction instr;
    /** Destination register. */
    private String destination;
    /** Parameters. */
    private final String[] parameters;
    /** Operation. */
    private final Opcode op;

    public InstructionNode(final AnalyzedInstruction instr) {
        this.instr = instr;
        this.idx = this.instr.getInstructionIndex();
        int[] register;
        int target;
        final Instruction i = this.instr.getInstruction();
        if (i instanceof ThreeRegisterInstruction) {
            final ThreeRegisterInstruction tri = (ThreeRegisterInstruction) i;
            register = new int[3];
            register[2] = tri.getRegisterC();
            register[1] = tri.getRegisterB();
            register[0] = tri.getRegisterA();
        } else if (i instanceof TwoRegisterInstruction) {
            final TwoRegisterInstruction twori = (TwoRegisterInstruction) i;
            register = new int[2];
            register[1] = twori.getRegisterB();
            register[0] = twori.getRegisterA();
        } else if (i instanceof SingleRegisterInstruction) {
            final SingleRegisterInstruction sri = (SingleRegisterInstruction) i;
            register = new int[1];
            register[0] = sri.getRegisterA();
        } else {
            register = null;
        }

        if (register == null) {
            this.destination = null;
            this.parameters = null;
        } else {
            byte offset = 0;
            if (instr.setsRegister()) {
                target = instr.getDestinationRegister();
                if (target < 0 || target != register[0]) {
                    throw new AssertionError("target was <0 or != registerA");
                }
                this.destination = Integer.toString(target);
                offset = 1;
            } else {
                this.destination = null;
            }
            this.parameters = new String[register.length - offset];
            for (int j = 0; j < parameters.length; ++j) {
                this.parameters[j] = Integer.toString(register[j + offset]);
            }
        }
        this.op = instr.getInstruction().opcode;
    }

    /**
     * Create an instruction node without having an <tt>AnalyzedInstruction</tt>.
     * Primarily eases testing.
     * @param op operation
     * @param target target register, if there is none, insert <tt>-1</tt>
     * @param source source register, i.e. parameters of operation
     */
    public InstructionNode(final Opcode op, final int target, final int... source) {
        this.instr = null;
        this.idx = INDEX;
        ++INDEX;
        this.op = op;
        if (target > 0) {
            this.destination = Integer.toString(target);
        } else {
            this.destination = null;
        }
        if (source != null) {
            this.parameters = new String[source.length];
            for (int i = 0; i < parameters.length; ++i) {
                this.parameters[i] = Integer.toString(source[i]);
            }
        } else {
            this.parameters = null;
        }
    }

    @Override
    public AnalyzedInstruction getInstruction() {
        return instr;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + this.idx;
//        hash = 29 * hash + Objects.hashCode(this.instr);
//        hash = 29 * hash + Objects.hashCode(this.destination);
//        hash = 29 * hash + Arrays.deepHashCode(this.parameters);
//        hash = 29 * hash + (this.op != null ? this.op.hashCode() : 0);
//        return hash;
        return idx;
    }

    @Override
    public boolean equals(final Object o) {
        boolean result = false;
        if (o instanceof InstructionNode) {
            final InstructionNode other = (InstructionNode) o;
            result = instr.equals(other.instr);
        }
        return result;
    }

    @Override
    public String toString() {
        String result;
        final int index = idx;
        if (destination != null || parameters != null) {
            final StringBuilder sb = new StringBuilder("[" + index + "] ");
            if (destination != null) {
                sb.append("v");
                sb.append(destination);
                sb.append(" = ");
            }
            sb.append(op.name);
            sb.append("(");
            boolean flag = false;
            if (parameters != null) {
                for (int i = 0; i < parameters.length; ++i) {
                    flag = true;
                    sb.append("v");
                    sb.append(parameters[i]);
                    sb.append(", ");
                }
            }
            String res;
            if (flag) {
                res = sb.substring(0, sb.length() - 2);
            } else {
                res = sb.toString();
            }
            result = res + ")";
        } else {
            result = "[" + index + "] " + op.name;
        }
        return result;
    }

    @Override
    public void changeVariableName(final int varOriginal, final String varNewName) {
        final String var = Integer.toString(varOriginal);
        boolean found = false;
        for (int i = 0; !found && i < parameters.length; ++i) {
            if (parameters[i].equals(var)) {
                found = true;
                parameters[i] = varNewName;
            }
        }
    }

    @Override
    public void changeDefinitionName(final String newName) {
        if (newName != null) {
            this.destination = newName;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getDestinationRegister() {
        return destination;
    }

    public String[] getSourceRegisters() {
        if (parameters == null) {
            return null;
        } else {
            return Arrays.copyOf(parameters, parameters.length);
        }
    }
}
