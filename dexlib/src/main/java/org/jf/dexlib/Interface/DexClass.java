package org.jf.dexlib.Interface;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.Code.Analysis.graphs.GraphDumper;

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
	
	public ClassDataItem getClassDataItem() {
		return cDefItem.getClassData();
	}
	
    public void dumpGraphs(final GraphDumper gDump) throws FileNotFoundException {
    	for (final DexMethod dexMethod : methods) {
    		dexMethod.dumpGraphs(gDump);
    	}
    }
	
	public String toString() {
		return "DexClass(" + cDefItem.getClassType().getTypeDescriptor() + "): " + methods.size() + " methods";
	}
}
