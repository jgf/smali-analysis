package org.jf.dexlib.Interface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexProgram {

	private final List<DexClass> classes = new LinkedList<DexClass>();
	
	private DexProgram() {
	}
	
	public List<DexClass> getClasses() {
		return Collections.unmodifiableList(classes);
	}
	
	public void addClass(final DexClass cls) {
		classes.add(cls);
	}
	
}
