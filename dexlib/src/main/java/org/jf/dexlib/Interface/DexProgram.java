package org.jf.dexlib.Interface;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.SyntheticAccessorResolver;
import org.jf.dexlib.Code.Analysis.graphs.GraphDumper;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexProgram {

	private final List<DexClass> classes = new LinkedList<DexClass>();
	private final SyntheticAccessorResolver synth;
	private final String name;
	
	public DexProgram(final String name, final DexFile file) {
		this(name, new SyntheticAccessorResolver(file));
	}

	public DexProgram(final String name, final SyntheticAccessorResolver synth) {
		this.synth = synth;
		this.name = name;
	}
	
	public SyntheticAccessorResolver getSyntheticResolver() {
		return synth;
	}
	
	public List<DexClass> getClasses() {
		return Collections.unmodifiableList(classes);
	}
	
	public void addClass(final DexClass cls) {
		classes.add(cls);
	}
	
	public void dumpGraphs(final GraphDumper gDump) throws FileNotFoundException {
		for (final DexClass dexClass : classes) {
			dexClass.dumpGraphs(gDump);
		}
	}
	
	public String toString() {
		return "DexProgram(" + name +"): " + classes.size() + " classes";
	}
	
}
