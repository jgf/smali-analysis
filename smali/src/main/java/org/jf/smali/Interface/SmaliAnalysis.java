package org.jf.smali.Interface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Interface.DexAnalysis;
import org.jf.dexlib.Interface.DexClass;
import org.jf.dexlib.Interface.DexMethod;
import org.jf.dexlib.Interface.DexProgram;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;
import org.jf.util.AnalysisUtil;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class SmaliAnalysis implements DexAnalysis<SmaliAnalysis.SmaliInput> {

	private final SmaliConfig conf;
	
	public SmaliAnalysis(final SmaliConfig conf) {
		this.conf = conf;
	}

	public DexProgram analyze(final SmaliInput input) throws DexAnalysisException {
		final DexFile dexFile = assembleDexFile(conf, input);

		initializeLibraryClassPaths(conf, dexFile);

        final DexProgram dexProg = new DexProgram(input.toString(), dexFile);
        
        for (final ClassDefItem clsDef : dexFile.ClassDefsSection.getItems()) {
            final String classDescriptor = clsDef.getClassType().getTypeDescriptor();
            
            //validate that the descriptor is formatted like we expect
            if (classDescriptor.charAt(0) != 'L' ||
                classDescriptor.charAt(classDescriptor.length()-1) != ';') {
                conf.out.println("Unrecognized class descriptor - " + classDescriptor + " - skipping class");
                continue;
            }

            final ClassDataItem clsData = clsDef.getClassData();
            if (clsData == null) {
                continue;
            }
            
            final DexClass dexClass = new DexClass(clsDef);
            dexProg.addClass(dexClass);
            
            if (clsData.getDirectMethods() != null) {
                for (final EncodedMethod em : clsData.getDirectMethods()) {
                    final MethodAnalyzer analyze = new MethodAnalyzer(em, false, null);
                    analyze.analyze();
                    final List<AnalyzedInstruction> instructions = analyze.getInstructions();
                    final DexMethod dexMethod = new DexMethod(instructions, em);
                    dexClass.addMethod(dexMethod);
                }
            }
            
            if (clsData.getVirtualMethods() != null) {
                for (final EncodedMethod em : clsData.getVirtualMethods()) {
                    if (em.codeItem == null) {
                        continue;
                    }
                    
                    final MethodAnalyzer analyze = new MethodAnalyzer(em, false, null);
                    analyze.analyze();
                    final List<AnalyzedInstruction> instructions = analyze.getInstructions();
                    final DexMethod dexMethod = new DexMethod(instructions, em);
                    dexClass.addMethod(dexMethod);
                }
            }
        }
        
		return dexProg;
	}

	private static void initializeLibraryClassPaths(final SmaliConfig conf, final DexFile dexFile) {
		final ClassPath.ClassPathErrorHandler classPathErrorHandler = new ClassPath.ClassPathErrorHandler() {
			public void ClassPathError(String className, Exception ex) {
				conf.out.println(String.format("Skipping %s", className));
				ex.printStackTrace(conf.out);
			}
		};
	
		String[] extraBootClassPathArray = null;
		if (conf.androidJars != null && conf.androidJars.length() > 0) {
			extraBootClassPathArray = (conf.androidJars.charAt(0) == ':' 
					? conf.androidJars.substring(1).split(":")
					: conf.androidJars.split(":"));
		}
	
		String[] bootClassPathDirsArray = new String[conf.bootClassPathDirs.size()];
		for (int i = 0; i < bootClassPathDirsArray.length; i++) {
			bootClassPathDirsArray[i] = conf.bootClassPathDirs.get(i);
		}
	
		if (dexFile.isOdex() && conf.bootClassPath == null) {
			ClassPath.InitializeClassPathFromOdex(bootClassPathDirsArray, extraBootClassPathArray, conf.dexFilePath,
				dexFile, classPathErrorHandler);
		} else {
			String[] bootClassPathArray = null;
			if (conf.bootClassPath != null) {
				bootClassPathArray = conf.bootClassPath.split(":");
			}
			ClassPath.InitializeClassPath(bootClassPathDirsArray, bootClassPathArray, extraBootClassPathArray,
				conf.dexFilePath, dexFile, classPathErrorHandler);
		}
	}
	
	private static DexFile assembleDexFile(final SmaliConfig conf, final SmaliInput input) throws DexAnalysisException {
        Opcode.updateMapsForApiLevel(conf.apiLevel);

        final DexFile dexFile = new DexFile();

        if (conf.apiSet && conf.apiLevel >= 14) {
            dexFile.HeaderItem.setVersion(36);
        }
        
        if (conf.apiSet && conf.apiLevel >= 14) {
            dexFile.HeaderItem.setVersion(36);
        }

        boolean errors = false;

        for (final File file: input) {
            try {
				errors |= !assembleSmaliFile(conf, file, dexFile);
			} catch (final IOException e) {
				e.printStackTrace(conf.out);
				throw new DexAnalysisException(e);
			} catch (final RecognitionException e) {
				e.printStackTrace(conf.out);
				throw new DexAnalysisException(e);
			}
        }

        if (errors) {
        	throw new DexAnalysisException("There were errors during assembly.");
        }

        if (conf.sort) {
            dexFile.setSortAllItems(true);
        }

        if (conf.fixJumbo || conf.fixGoto) {
            fixInstructions(dexFile, conf.fixJumbo, conf.fixGoto);
        }

        dexFile.place();
        
        return dexFile;
	}
	
	/** 
	 * copied from smali.main - should be moved to a common utility class 
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	private static boolean assembleSmaliFile(final SmaliConfig conf, final File smaliFile, final DexFile dexFile)
			throws IOException, RecognitionException {
		CommonTokenStream tokens;

		LexerErrorInterface lexer;

		if (conf.oldLexer) {
			ANTLRFileStream input = new ANTLRFileStream(smaliFile.getAbsolutePath(), "UTF-8");
			input.name = smaliFile.getAbsolutePath();

			lexer = new smaliLexer(input);
			tokens = new CommonTokenStream((TokenSource) lexer);
		} else {
			FileInputStream fis = new FileInputStream(smaliFile.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(fis, "UTF-8");

			lexer = new smaliFlexLexer(reader);
			((smaliFlexLexer) lexer).setSourceFile(smaliFile);
			tokens = new CommonTokenStream((TokenSource) lexer);
		}

		if (conf.printTokens) {
			tokens.getTokens();

			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				if (token.getChannel() == smaliLexer.HIDDEN) {
					continue;
				}

				conf.out.println(smaliParser.tokenNames[token.getType()] + ": " + token.getText());
			}
		}

		smaliParser parser = new smaliParser(tokens);
		parser.setVerboseErrors(conf.verboseErrors);
		parser.setAllowOdex(conf.allowOdex);
		parser.setApiLevel(conf.apiLevel);

		smaliParser.smali_file_return result = parser.smali_file();

		if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
			return false;
		}

		final CommonTree t = (CommonTree) result.getTree();

		final CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
		treeStream.setTokenStream(tokens);

		smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);

		dexGen.dexFile = dexFile;
		dexGen.smali_file();

		return dexGen.getNumberOfSyntaxErrors() <= 0;
	}
	
	/** 
	 * copied from smali.main - should be moved to a common utility class 
	 */
    private static void fixInstructions(final DexFile dexFile, final boolean fixJumbo, final boolean fixGoto) {
        dexFile.place();

        for (CodeItem codeItem: dexFile.CodeItemsSection.getItems()) {
            codeItem.fixInstructions(fixJumbo, fixGoto);
        }
    }

	public static class SmaliInput implements DexAnalysis.Input, Iterable<File> {

		private final LinkedHashSet<File> filesToProcess = new LinkedHashSet<File>();
		
		public boolean addFile(final String fileName) {
			final File f = new File(fileName);

			return addFile(f);
		}
		
		public boolean addFile(final File file) {
			if (file.isDirectory()) {
				return addSmaliFilesInDir(file);
			} else {
				return filesToProcess.add(file);
			}
		}
		
		public boolean addFiles(final String[] files) {
			boolean change = false;
			
			for (final String file : files) {
				change |= addFile(file);
			}
			
			return change;
		}

		public boolean addFiles(final File[] files) {
			boolean change = false;
			
			for (final File file : files) {
				change |= addFile(file);
			}
			
			return change;
		}

		public Iterator<File> iterator() {
			return Collections.unmodifiableSet(filesToProcess).iterator();
		}
		
		public int getNumberOfFiles() {
			return filesToProcess.size();
		}
		
	    private boolean addSmaliFilesInDir(final File dir) {
	    	boolean change = false;
	    	
	        for(final File file: dir.listFiles()) {
	            if (file.isDirectory()) {
	                change |= addSmaliFilesInDir(file);
	            } else if (file.getName().endsWith(".smali")) {
	                change |= filesToProcess.add(file);
	            }
	        }
	        
	        return change;
	    }
	    
	    public String toString() {
	    	return filesToProcess.toString();
	    }
	}
	
	public static class SmaliConfig {

		public String androidJars = "data/core.jar:data/ext.jar:data/framework.jar:data/android.policy.jar:data/services.jar";
		public List<String> bootClassPathDirs = new LinkedList<String>();
		public String bootClassPath = null;
		public boolean apiSet = false;
		public int apiLevel = 14;
		public PrintStream out = System.out;
		public boolean verboseErrors = false;
		public boolean oldLexer = false;
		public boolean printTokens = false;
		public boolean allowOdex = true;
		public boolean sort = true;
		public boolean fixJumbo = true;
		public boolean fixGoto = true;
        public String dexFilePath = ".";
		
        public SmaliConfig() {
        	bootClassPathDirs.add(".");
        }
        
		public String toString() {
			final StringBuilder sb = new StringBuilder("SmaliAnalysis configuration:\n");
			AnalysisUtil.writeAllFieldsToBuffer(this, sb);
			
			return sb.toString();
		}

	}

}
