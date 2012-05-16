package org.jf.dexlib.Interface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.Code.Analysis.SyntheticAccessorResolver;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexProgram {

	private final List<DexClass> classes = new LinkedList<DexClass>();
	private final SyntheticAccessorResolver synth;
	private final String dexFile;
	
	DexProgram(final String dexFile, final SyntheticAccessorResolver synth) {
		this.synth = synth;
		this.dexFile = dexFile;
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
	
	public String toString() {
		return "DexProgram(" + dexFile +"): " + classes.size() + " classes";
	}
	
}
