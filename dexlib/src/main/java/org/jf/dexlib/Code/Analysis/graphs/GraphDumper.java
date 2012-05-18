package org.jf.dexlib.Code.Analysis.graphs;

import java.io.File;
import java.io.FileNotFoundException;

import org.jf.dexlib.Code.Analysis.graphs.Dominators.DomTree;
import org.jf.dexlib.Interface.DexMethod;

public class GraphDumper {

    private final String toDir;
    private final String fileNamePrefix;
    private final boolean dumpCFG;
    private final boolean dumpDOM;
    private final boolean dumpCDG;
    private final boolean includeExc;
    
    public GraphDumper(final String toDir, final String fileNamePrefix, final boolean dumpCFG,
    		final boolean dumpDOM, final boolean dumpCDG, final boolean includeExc) {
    	if (toDir == null || toDir.isEmpty()) {
    		this.toDir = "." + File.separator;
    	} else {
    		this.toDir = (toDir.endsWith(File.separator) ? toDir : toDir + File.separator);
    	}

    	if (fileNamePrefix == null) {
    		this.fileNamePrefix = "";
    	} else {
    		this.fileNamePrefix = fileNamePrefix.replace(File.separator, "_");
    	}

        this.dumpCFG = dumpCFG;
        this.dumpDOM = dumpDOM;
        this.dumpCDG = dumpCDG;
        this.includeExc = includeExc;
    }

    public void dump(final DexMethod dexMethod, final String name) throws FileNotFoundException {
        if (!dumpCFG && !dumpDOM && !dumpCDG) {
            return;
        }
        
        final File dir = new File(toDir);
        if (dir.exists() && (!dir.isDirectory() || !dir.canWrite())) {
        	throw new FileNotFoundException(dir.getAbsolutePath() + " is not a writable directory.");
        } else if (!dir.exists()) {
        	if (!dir.mkdirs()) {
        		throw new FileNotFoundException("Could not create directory: " + dir.getAbsolutePath());
        	} else {
        		System.out.println("Created " + dir.getAbsolutePath());
        	}
        }
        
        final String fileName = toDir + WriteGraphToDot.sanitizeFileName(fileNamePrefix + name);
        
        CFG cfg = null;
        
        if (dumpCFG) {
            cfg = dexMethod.getControlFlowGraph(includeExc);
            WriteGraphToDot.write(cfg, fileName + ".cfg.dot");
        }
        
        if (dumpDOM) {
        	final DomTree<CFG.Node> domTree = dexMethod.getDominationTree(includeExc);
            WriteGraphToDot.write(domTree, fileName + ".dom.dot");
        }
        
        if (dumpCDG) {
            final CDG cdg = dexMethod.getControlDependenceGraph(includeExc);
            WriteGraphToDot.write(cdg, fileName + ".cdg.dot");
        }
    }
    
}