package org.jf.smali.Interface;

import org.jf.dexlib.Interface.DexAnalysis;
import org.jf.dexlib.Interface.DexProgram;

public class SmaliAnalysis implements DexAnalysis<SmaliAnalysis.SmaliInput> {

	public static class SmaliInput implements DexAnalysis.Input {
		
	}

	public DexProgram analyze(final SmaliInput program) throws DexAnalysisException {
//        ClassPath.ClassPathErrorHandler classPathErrorHandler = new ClassPath.ClassPathErrorHandler() {
//            public void ClassPathError(String className, Exception ex) {
//                System.err.println(String.format("Skipping %s", className));
//                ex.printStackTrace(System.err);
//            }
//        };
//
//        String[] extraBootClassPathArray = null;
//        if (extraBootClassPathEntries != null && extraBootClassPathEntries.length() > 0) {
//            assert extraBootClassPathEntries.charAt(0) == ':';
//            extraBootClassPathArray = extraBootClassPathEntries.substring(1).split(":");
//        }
//
//        String[] bootClassPathDirsArray = new String[bootClassPathDirs.size()];
//        for (int i=0; i<bootClassPathDirsArray.length; i++) {
//            bootClassPathDirsArray[i] = bootClassPathDirs.get(i);
//        }
//
//        final String dexFilePath = ".";
//        
//        if (dexFile.isOdex() && bootClassPath == null) {
//            ClassPath.InitializeClassPathFromOdex(bootClassPathDirsArray, extraBootClassPathArray, dexFilePath, dexFile,
//                    classPathErrorHandler);
//        } else {
//            String[] bootClassPathArray = null;
//            if (bootClassPath != null) {
//                bootClassPathArray = bootClassPath.split(":");
//            }
//            ClassPath.InitializeClassPath(bootClassPathDirsArray, bootClassPathArray, extraBootClassPathArray,
//                    dexFilePath, dexFile, classPathErrorHandler);
//        }
//
//        File outputGraphFile = new File(outputGraphDir);
//        if (!outputGraphFile.exists()) {
//            if (!outputGraphFile.mkdirs()) {
//                System.err.println("Can't create the graph output directory " + outputGraphDir);
//                System.exit(1);
//            }
//        }
//        
//        ClassFileNameHandler graphPrefixNameHandler = new ClassFileNameHandler(outputGraphFile, ".");
//
//        for (ClassDefItem clsDef : dexFile.ClassDefsSection.getItems()) {
//            String classDescriptor = clsDef.getClassType().getTypeDescriptor();
//            
////            System.out.print("\nWorking on '" + classDescriptor + "': ");
//            
//            //validate that the descriptor is formatted like we expect
//            if (classDescriptor.charAt(0) != 'L' ||
//                classDescriptor.charAt(classDescriptor.length()-1) != ';') {
//                System.err.println("Unrecognized class descriptor - " + classDescriptor + " - skipping class");
//                continue;
//            }
//
//            File graphPrefixFile = graphPrefixNameHandler.getUniqueFilenameForClass(classDescriptor);
//            File graphDir = graphPrefixFile.getParentFile();
//
//            if (!graphDir.exists()) {
//                if (!graphDir.mkdirs()) {
//                    System.err.println("Unable to create directory " + graphDir.toString() + " - skipping graph output");
//                    continue;
//                }
//            }
//
//            ClassDataItem clsData = clsDef.getClassData();
//            if (clsData == null) {
//                continue;
//            }
//            
//            String filePrefix = graphPrefixFile.toString();
//            GraphDumper gd = new GraphDumper(filePrefix, false, graphCFG, graphDOM, graphCDG, graphIncludeExceptions);
//            
//            if (clsData.getDirectMethods() != null) {
//                for (EncodedMethod em : clsData.getDirectMethods()) {
//                    MethodAnalyzer analyze = new MethodAnalyzer(em, false, null);
//                    analyze.analyze();
//                    List<AnalyzedInstruction> instructions = analyze.getInstructions();
//                    final String mName = em.method.getVirtualMethodString();
//                    gd.dump(instructions, mName);
//                }
//            }
//            
//            if (clsData.getVirtualMethods() != null) {
//                for (EncodedMethod em : clsData.getVirtualMethods()) {
//                    if (em.codeItem == null) {
//                        continue;
//                    }
//                    
//                    MethodAnalyzer analyze = new MethodAnalyzer(em, false, null);
//                    analyze.analyze();
//                    List<AnalyzedInstruction> instructions = analyze.getInstructions();
//                    final String mName = em.method.getVirtualMethodString();
//                    gd.dump(instructions, mName);
//                }
//            }
//        }
        
		return null;
	}
	
}
