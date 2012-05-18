package org.jf.dexlib.Interface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.ClassDefItem;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexClass {

	private final ClassDefItem cDefItem;
	private final List<DexMethod> methods = new LinkedList<DexMethod>();
	
	public DexClass(final ClassDefItem cDefItem) {
		this.cDefItem = cDefItem;
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
	
	public String toString() {
		return "DexClass(" + cDefItem.getClassType().getTypeDescriptor() + "): " + methods.size() + " methods";
	}
}
