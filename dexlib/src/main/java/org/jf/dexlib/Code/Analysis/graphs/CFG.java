package org.jf.dexlib.Code.Analysis.graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.graphs.CFG.Edge.Kind;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class CFG extends DirectedPseudograph<CFG.Node, CFG.Edge> {

    private static final long serialVersionUID = -8848165249946385503L;
    
    public static final CFGEdgeFactory EDGE_FACTORY = new CFGEdgeFactory();

    public static CFG build(final List<AnalyzedInstruction> instructions, final String name) {
        return build(instructions, name, false);
    }

    public static CFG build(final List<AnalyzedInstruction> instructions, final String name, final boolean includeExc) {
        CFG cfg = new CFG(instructions, name + (includeExc ? " with uncatched exceptions" : ""));
        
        cfg.build(includeExc);
        
        return cfg.stripUnreachableNodes();
    }

    private final List<AnalyzedInstruction> instructions;
    private final EntryNode entry;
    private final ExitNode exit;
    private final String name;
    
    private CFG(final List<AnalyzedInstruction> instructions, final String name) {
        this(instructions, name, new EntryNode(), new ExitNode());
    }
    
    private CFG(final List<AnalyzedInstruction> instructions, final String name, EntryNode entry, ExitNode exit) {
        super(EDGE_FACTORY);
        this.instructions = instructions;
        this.name = name;
        this.entry = entry;
        this.exit = exit;
        addVertex(entry);
        addVertex(exit);
        addEdge(entry, exit);
    }
    
    private CFG stripUnreachableNodes() {
        final Set<Node> reachable = new HashSet<Node>();
        GraphWalker<Node, Edge> walk = new GraphWalker<Node, Edge>(this) {

            @Override
            public void discover(Node node) {
                reachable.add(node);
            }

            @Override
            public void finish(Node node) {
            }
        };
        
        walk.traverseDFS(entry);
        
        if (reachable.size() == vertexSet().size()) {
            // shortcut for cfgs that do not have to be altered.
            return this;
        }
        
        CFG stripped = new CFG(instructions, name + " - " + (vertexSet().size() - reachable.size()) 
                + " unreachable nodes", entry, exit);

        for (Node node : reachable) {
            stripped.addVertex(node);
        }
        
        for (Node node : reachable) {
            for (Edge edge : outgoingEdgesOf(node)) {
                if (reachable.contains(edge.getTarget())) {
                    stripped.addEdge(node, edge.getTarget(), edge);
                }
            }
        }
        
        return stripped;
    }
    
    public String getName() {
        return name;
    }
    
    public Node getEntry() {
        return entry;
    }
    
    public Node getExit() {
        return exit;
    }
    
    public String toExtendedString() {
        StringBuffer sb = new StringBuffer("CFG of " + name + "\n");
        for (Node n : vertexSet()) {
            sb.append(n + " -> ");
            for (Edge e : outgoingEdgesOf(n)) {
                sb.append(e.getTarget() + " ");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
        
    public String toString() {
        return "CFG of " + name + "(" + vertexSet().size() + ", " + edgeSet().size() + ")" ;
    }
        
    
    public interface Edge {
        public enum Kind { 
            /* control flow */ CF, 
            /* exception control flow */ CF_EX, 
            /* control dependence */ CD }
        
        public Node getSource();
        public Node getTarget();
        public Kind getKind();
    }
    
    public interface Node {
        public boolean isInstruction();
        public boolean isEntry();
        public boolean isExit();
        public boolean isPHI();
        public AnalyzedInstruction getInstruction();
        public PHI getPHI();
    }
    
    public Edge addEdge(Node from, Node to, Kind kind) {
        CFGEdge edge = new CFGEdge(from, to, kind);
        
        if (addEdge(from, to, edge)) {
            return edge;
        } else {
            return null;
        }
    }
    
    private void build(final boolean includeUncatchedExceptions) {
        final Map<AnalyzedInstruction, InstructionNode> tmpMap = new HashMap<AnalyzedInstruction, InstructionNode>();

        boolean isFirst = true;
        for (final AnalyzedInstruction instr : instructions) {
            InstructionNode node = new InstructionNode(instr);
            addVertex(node);
            tmpMap.put(instr, node);
            
            if (isFirst) {
                addEdge(entry, node);
                isFirst = false;
            }
        }
        
        for (final AnalyzedInstruction instr : instructions) {
            final InstructionNode node = tmpMap.get(instr);

            final Iterator<AnalyzedInstruction> successors = instr.getSuccessors().iterator();
            if (!successors.hasNext()) {
                addEdge(node, exit);
            } else {
                // successors contains both. exception flow and normal flow
                // there may be duplicates, iff the exception flow and the normal
                // flow of an instruction result in the same successor
                // e.g. empty catch block
                final Set<CFGEdge> duplicates = new HashSet<CFGEdge>();
                
                while (successors.hasNext()) {
                    final AnalyzedInstruction succ = successors.next();
                    final InstructionNode succNode = tmpMap.get(succ);
                    
                    if (!containsEdge(node, succNode)) {
                        addEdge(node, succNode);
                    } else {
                        // if an edge is contained a second time in this list, the first
                        // one is normal and the second one exception flow
                        duplicates.add(new CFGEdge(node, succNode, Kind.CF_EX));
                    }
                }

                for (final AnalyzedInstruction excSucc : instr.getExceptionSuccessors()) {
                    final InstructionNode succNode = tmpMap.get(excSucc);

                    final CFGEdge dupEdge = new CFGEdge(node, succNode, Kind.CF_EX);
                    if (duplicates.contains(dupEdge)) {
                        // add a new edge if the edge was also duplicated in the successors
                        addEdge(node, succNode, dupEdge);
                    } else {
                        // change the existing edge to an exception edge otherwise
                        final CFGEdge edge = (CFGEdge) getEdge(node, succNode);
                        assert edge.kind == Kind.CF;
                        removeEdge(edge);
                        addEdge(edge.source, edge.target, Kind.CF_EX);
                    }
                }
                
            }
            
            // we conservatively approximate exception control flow by adding 
            // control flow to the exit node for each instruction that may throw
            // an exception.
           if (includeUncatchedExceptions && instr.getOriginalInstruction().opcode.canThrow()) {
                addEdge(node, exit, Kind.CF_EX);
            }
        }        
    }

    private static class CFGEdge implements Edge {

        private final Node source;
        private final Node target;
        private final Kind kind;

        private CFGEdge(final Node source, final Node target) {
            this(source, target, Kind.CF);
        }
        
        private CFGEdge(final Node source, final Node target, final Kind kind) {
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
        
        public int hashCode() {
            return source.hashCode() * (target.hashCode() >> 6);
        }
        
        public boolean equals(Object o) {
            if (o instanceof CFGEdge) {
                CFGEdge other = (CFGEdge) o;
                return kind == other.kind && source.equals(other.source) && target.equals(other.target);
            }
            
            return false;
        }
        
        public String toString() {
            return kind.name();
        }
    }
    
    public static class PHI {
        //TODO
    }
    
    private static abstract class AbstractNode implements Node {

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
        
        
        public abstract String toString();
    }
    
    private static class EntryNode extends AbstractNode {
        public boolean isEntry() {
            return true;
        }

        @Override
        public String toString() {
            return "ENTRY";
        }
    }
   
    private static class ExitNode extends AbstractNode {
        public boolean isExit() {
            return true;
        }

        @Override
        public String toString() {
            return "EXIT";
        }
    }
    
    private static class InstructionNode extends AbstractNode {
        private final AnalyzedInstruction instr;
        
        public InstructionNode(final AnalyzedInstruction instr) {
            this.instr = instr;
        }

        public AnalyzedInstruction getInstruction() {
            return instr;
        }

        public boolean isInstruction() {
            return true;
        }

        public int hashCode() {
            return instr.hashCode() * 23;
        }
        
        public boolean equals(Object o) {
            if (o instanceof InstructionNode) {
                InstructionNode other = (InstructionNode) o;
                return instr.equals(other.instr);
            }
            
            return false;
        }

        @Override
        public String toString() {
            final int index = instr.getInstructionIndex();
            final Opcode op = instr.getInstruction().opcode;
            
            return "[" + index + "] " + (op.setsRegister() ? "v" + instr.getDestinationRegister() + " = " : "") 
                + op.name;
        }
    }
    
    private static class PHINode extends AbstractNode {
        private final PHI phi;
        
        public PHINode(final PHI phi) {
            this.phi = phi;
        }

        public boolean isPHI() {
            return true;
        }

        public PHI getPHI() {
            return phi;
        }


        public int hashCode() {
            return phi.hashCode() * 7;
        }
        
        public boolean equals(Object o) {
            if (o instanceof PHINode) {
                PHINode other = (PHINode) o;
                return phi.equals(other.phi);
            }
            
            return false;
        }

        @Override
        public String toString() {
            return phi.toString();
        }
    }

    public static final class CFGEdgeFactory implements EdgeFactory<Node, Edge> {

        private CFGEdgeFactory() {}
        
        public CFGEdge createEdge(final Node from, final Node to) {
            return new CFGEdge(from, to);
        }
        
        public CFGEdge createEdge(final Node from, final Node to, final Kind kind) {
            return new CFGEdge(from, to, kind);
        }
        
    }
}