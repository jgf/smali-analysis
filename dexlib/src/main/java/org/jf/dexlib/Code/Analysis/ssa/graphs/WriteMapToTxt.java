/*
 * @(#)WriteMapToTxt.java
 */
package org.jf.dexlib.Code.Analysis.ssa.graphs;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

/**
 * Write a Map to a txt file.
 * @author Patrick Kuhn
 */
public final class WriteMapToTxt {

    private WriteMapToTxt() {
        throw new AssertionError();
    }

    public static <A, B> void write(Map<A, Set<B>> map, String fileName) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(fileName);
        out.println("--DF--");
        out.println("Node \t DF(Node)");

        Set<A> keys = map.keySet();
        for (A a : keys) {
            out.print(a.toString().split("\\s+")[0] + "\t");
            for (B b : map.get(a)) {
                out.print(b.toString().split("\\s+")[0] + " ");
            }
            out.println();
        }

        out.flush();
    }
}
