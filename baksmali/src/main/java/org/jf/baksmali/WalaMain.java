package org.jf.baksmali;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Analysis.SyntheticAccessorResolver;
import org.jf.dexlib.Code.Analysis.wala.Dex2Wala;

public class WalaMain {

	public static SyntheticAccessorResolver syntheticAccessorResolver = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.err.println("Invalid arguments");
			return;
		}
     
        String bootClassPath = null;
        StringBuffer extraBootClassPathEntries = new StringBuffer();
        
        String bcp = args[0];
        if (bcp != null && bcp.charAt(0) == ':') {
            extraBootClassPathEntries.append(bcp);
        } else {
            bootClassPath = bcp;
        }
        
        String inputDexFileName = args[1];
        try {
            File dexFileFile = new File(inputDexFileName);
            if (!dexFileFile.exists()) {
                System.err.println("Can't find the file " + inputDexFileName);
                System.exit(1);
            }

            //Read in and parse the dex file
            DexFile dexFile = new DexFile(dexFileFile, true, false);
       
            String extraBootClassPath = extraBootClassPathEntries.toString();
            String[] classPathDirs = {"."};
        
            ClassPath.ClassPathErrorHandler classPathErrorHandler = null;

            try {
                String[] extraBootClassPathArray = null;
                if (extraBootClassPath != null && extraBootClassPath.length() > 0) {
                    assert extraBootClassPath.charAt(0) == ':';
                    extraBootClassPathArray = extraBootClassPath.substring(1).split(":");
                }

                if (dexFile.isOdex() && bootClassPath == null) {
                    //ext.jar is a special case - it is typically the 2nd jar in the boot class path, but it also
                    //depends on classes in framework.jar (typically the 3rd jar in the BCP). If the user didn't
                    //specify a -c option, we should add framework.jar to the boot class path by default, so that it
                    //"just works"
                    if (extraBootClassPathArray == null && isExtJar(dexFileFile.getPath())) {
                        extraBootClassPathArray = new String[] {"framework.jar"};
                    }
                    ClassPath.InitializeClassPathFromOdex(classPathDirs, extraBootClassPathArray, dexFileFile.getPath(), dexFile,
                            classPathErrorHandler);
                } else {
                    String[] bootClassPathArray = null;
                    if (bootClassPath != null) {
                        bootClassPathArray = bootClassPath.split(":");
                    }
                    ClassPath.InitializeClassPath(classPathDirs, bootClassPathArray, extraBootClassPathArray,
                    		dexFileFile.getPath(), dexFile, classPathErrorHandler);
                }
            } catch (Exception ex) {
                System.err.println("\n\nError occured while loading boot class path files. Aborting.");
                ex.printStackTrace(System.err);
                System.exit(1);
            }

        syntheticAccessorResolver = new SyntheticAccessorResolver(dexFile);

        //sort the classes, so that if we're on a case-insensitive file system and need to handle classes with file
        //name collisions, then we'll use the same name for each class, if the dex file goes through multiple
        //baksmali/smali cycles for some reason. If a class with a colliding name is added or removed, the filenames
        //may still change of course
        ArrayList<ClassDefItem> classDefItems = new ArrayList<ClassDefItem>(dexFile.ClassDefsSection.getItems());
        Collections.sort(classDefItems, new Comparator<ClassDefItem>() {
            public int compare(ClassDefItem classDefItem1, ClassDefItem classDefItem2) {
                return classDefItem1.getClassType().getTypeDescriptor().compareTo(classDefItem1.getClassType().getTypeDescriptor());
            }
        });

        for (ClassDefItem classDefItem: classDefItems) {
            /**
             * The path for the disassembly file is based on the package name
             * The class descriptor will look something like:
             * Ljava/lang/Object;
             * Where the there is leading 'L' and a trailing ';', and the parts of the
             * package name are separated by '/'
             */

            //If we are analyzing the bytecode, make sure that this class is loaded into the ClassPath. If it isn't
            //then there was some error while loading it, and we should skip it
            ClassPath.ClassDef classDef = ClassPath.getClassDef(classDefItem.getClassType(), false);
            if (classDef == null || classDef instanceof ClassPath.UnresolvedClassDef) {
                continue;
            }

            String classDescriptor = classDefItem.getClassType().getTypeDescriptor();

            //validate that the descriptor is formatted like we expect
            if (classDescriptor.charAt(0) != 'L' ||
                classDescriptor.charAt(classDescriptor.length()-1) != ';') {
                System.err.println("Unrecognized class descriptor - " + classDescriptor + " - skipping class");
                continue;
            }

            //create and initialize the top level string template
            ClassDefinition classDefinition = new ClassDefinition(classDefItem);
            
            try
            {                
	        	ClassDataItem.EncodedMethod[] directMethods = classDefinition.getClassDataItem().getDirectMethods();
	            if (directMethods != null) {
	                for (ClassDataItem.EncodedMethod method : directMethods) {
	                    if (method.codeItem == null || method.codeItem.getInstructions().length == 0) {
	                        continue;
	                    }
	                    
	                    final MethodAnalyzer analyzer = new MethodAnalyzer(method, false);
	                    analyzer.analyze();
	                    final List<AnalyzedInstruction> instructions = analyzer.getInstructions();
	                    Dex2Wala.build(instructions, method.method.getVirtualMethodString()); //!!!!!
	                }
	            }
	            ClassDataItem.EncodedMethod[] virtualMethods = classDefinition.getClassDataItem().getVirtualMethods();
	            if (virtualMethods != null) {
	                for (ClassDataItem.EncodedMethod method : virtualMethods) {
	                    if (method.codeItem == null || method.codeItem.getInstructions().length == 0) {
	                        continue;
	                    }
	                    
	                    final MethodAnalyzer analyzer = new MethodAnalyzer(method, false);
	                    analyzer.analyze();
	                    final List<AnalyzedInstruction> instructions = analyzer.getInstructions();
	                    Dex2Wala.build(instructions, method.method.getVirtualMethodString()); //!!!!!
	                }
	            }

            } catch (Exception ex) {
                System.err.println("\n\nError occured while disassembling class " + classDescriptor.replace('/', '.') + " - skipping class");
                ex.printStackTrace();
            }
        }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final Pattern extJarPattern = Pattern.compile("(?:^|\\\\|/)ext.(?:jar|odex)$");
	private static boolean isExtJar(String dexFilePath) {
	    Matcher m = extJarPattern.matcher(dexFilePath);
	    return m.find();
	}
}
