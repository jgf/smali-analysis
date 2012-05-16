package org.jf.dexlib.Interface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexClass {

	private final ClassDefItem cDefItem;
	private final ClassDef cDef;
	private final List<DexMethod> methods = new LinkedList<DexMethod>();
	
	private DexClass(final ClassDefItem cDefItem, final ClassDef cDef) {
		this.cDefItem = cDefItem;
		this.cDef = cDef;
	}

	public List<DexMethod> getMethods() {
		return Collections.unmodifiableList(methods);
	}
	
	public void addMethod(final DexMethod method) {
		methods.add(method);
	}
	
	public ClassDefItem getItem() {
		return cDefItem;
	}
	
	public ClassDef getDef() {
		return cDef;
	}
	
}
