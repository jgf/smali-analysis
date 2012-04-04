package org.jf.dexlib.Code.Analysis.ssa.graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.graphs.GraphWalker;
import org.jf.dexlib.Code.Analysis.ssa.graphs.Edge.Kind;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class CFG extends DirectedPseudograph<Node, Edge> {

    private static final long serialVersionUID = -8848165249946385503L;
    static final CFGEdgeFactory EDGE_FACTORY = new CFGEdgeFactory();

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
        StringBuilder sb = new StringBuilder("CFG of " + name + "\n");
        for (Node n : vertexSet()) {
            sb.append(n).append(" -> ");
            for (Edge e : outgoingEdgesOf(n)) {
                sb.append(e.getTarget()).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "CFG of " + name + "(" + vertexSet().size() + ", " + edgeSet().size() + ")";
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
}
