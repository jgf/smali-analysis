package org.jf.dexlib.Code.Analysis.graphs;

import java.util.List;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

/**
 * This class can be used to insert phi nodes into the cfg. Those nodes are
 * needed for the transformation of the code to ssa form.
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class PHI {

    public static void compute(final List<AnalyzedInstruction> instructions) {
        final CFG cfg = CFG.build(instructions, "cfg");
        final DominanceFrontiers<CFG.Node, CFG.Edge> df = DominanceFrontiers.compute(cfg, cfg.getEntry());
        for (AnalyzedInstruction i : instructions) {
            //i.getOriginalInstruction().
        }
    }
    
}
