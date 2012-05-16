package org.jf.baksmali.Interface;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.InlineMethodResolver;
import org.jf.dexlib.Code.Analysis.SyntheticAccessorResolver;
import org.jf.dexlib.Interface.DexAnalysis;
import org.jf.dexlib.Interface.DexClass;
import org.jf.dexlib.Interface.DexMethod;
import org.jf.dexlib.Interface.DexProgram;

public class BakSmaliAnalysis implements DexAnalysis<BakSmaliAnalysis.BakSmaliInput> {

	private final Config conf;

	public BakSmaliAnalysis(final Config conf) {
		this.conf = conf;
	}
	
	public static class BakSmaliInput implements DexAnalysis.Input {
		public final String programDexFile;
		
		public BakSmaliInput(String programDexFile) {
			this.programDexFile = programDexFile;
		}
	}

	public DexProgram analyze(final BakSmaliInput in) throws DexAnalysisException {
		return analyze(in.programDexFile);
	}
	
	public DexProgram analyze(final String programDexFile) throws DexAnalysisException {
		String bootClassPath = null;
		final StringBuffer extraBootClassPathEntries = new StringBuffer();

		if ("".equals(conf.androidJars)) {
			bootClassPath = null;
		} else if (conf.androidJars != null && conf.androidJars.charAt(0) == ':') {
			extraBootClassPathEntries.append(conf.androidJars);
		} else {
			bootClassPath = conf.androidJars;
		}

		try {
			final File dexFileFile = new File(programDexFile);
			if (!dexFileFile.exists()) {
				final String msg ="Can't find the file " + programDexFile + " (" + dexFileFile.getAbsolutePath() + ")"; 
				conf.out.println(msg);
				throw new DexAnalysisException(msg);
			}

			// Read in and parse the dex file
			final DexFile dexFile = new DexFile(dexFileFile, true, false);

			final String extraBootClassPath = extraBootClassPathEntries.toString();
			final String[] classPathDirs = { "." };

			ClassPath.ClassPathErrorHandler classPathErrorHandler = null;

			try {
				String[] extraBootClassPathArray = null;
				if (extraBootClassPath != null && extraBootClassPath.length() > 0) {
					assert extraBootClassPath.charAt(0) == ':';
					extraBootClassPathArray = extraBootClassPath.substring(1).split(":");
				}

				if (dexFile.isOdex() && bootClassPath == null) {
					// ext.jar is a special case - it is typically the 2nd jar
					// in the boot class path, but it also
					// depends on classes in framework.jar (typically the 3rd
					// jar in the BCP). If the user didn't
					// specify a -c option, we should add framework.jar to the
					// boot class path by default, so that it
					// "just works"
					if (extraBootClassPathArray == null	&& isExtJar(dexFileFile.getPath())) {
						extraBootClassPathArray = new String[] { "framework.jar" };
					}
					ClassPath.InitializeClassPathFromOdex(classPathDirs, extraBootClassPathArray, dexFileFile.getPath(), dexFile,
							classPathErrorHandler);
				} else {
					String[] bootClassPathArray = null;
					if (bootClassPath != null) {
						bootClassPathArray = bootClassPath.split(":");
					}
					ClassPath.InitializeClassPath(classPathDirs, bootClassPathArray, extraBootClassPathArray, dexFileFile.getPath(),
							dexFile, classPathErrorHandler);
				}
			} catch (final Exception ex) {
				final String msg = "Error occured while loading boot class path files. Aborting.";
				conf.out.println("\n\n" + msg);
				ex.printStackTrace(conf.out);
				throw new DexAnalysisException(msg, ex);
			}

			final DexProgram dexProg = new DexProgram(programDexFile, new SyntheticAccessorResolver(dexFile));

			// sort the classes, so that if we're on a case-insensitive file
			// system and need to handle classes with file
			// name collisions, then we'll use the same name for each class, if
			// the dex file goes through multiple
			// baksmali/smali cycles for some reason. If a class with a
			// colliding name is added or removed, the filenames
			// may still change of course
			final ArrayList<ClassDefItem> classDefItems = new ArrayList<ClassDefItem>(dexFile.ClassDefsSection.getItems());
			Collections.sort(classDefItems, new Comparator<ClassDefItem>() {
				public int compare(ClassDefItem classDefItem1, ClassDefItem classDefItem2) {
					return classDefItem1.getClassType().getTypeDescriptor().compareTo(classDefItem1.getClassType().getTypeDescriptor());
				}
			});

			for (final ClassDefItem classDefItem : classDefItems) {
				/**
				 * The path for the disassembly file is based on the package
				 * name The class descriptor will look something like:
				 * Ljava/lang/Object; Where the there is leading 'L' and a
				 * trailing ';', and the parts of the package name are separated
				 * by '/'
				 */

				// If we are analyzing the bytecode, make sure that this class
				// is loaded into the ClassPath. If it isn't
				// then there was some error while loading it, and we should
				// skip it
				ClassPath.ClassDef classDef = ClassPath.getClassDef(classDefItem.getClassType(), false);
				if (classDef == null || classDef instanceof ClassPath.UnresolvedClassDef) {
					continue;
				}

				final String classDescriptor = classDefItem.getClassType().getTypeDescriptor();

				// validate that the descriptor is formatted like we expect
				if (classDescriptor.charAt(0) != 'L' || classDescriptor.charAt(classDescriptor.length() - 1) != ';') {
					conf.out.println("Unrecognized class descriptor - "	+ classDescriptor + " - skipping class");
					continue;
				}

				// create and initialize the top level string template
				final ClassDefinition classDefinition = new ClassDefinition(classDefItem);
				final DexClass dexClass = new DexClass(classDefItem, classDef);
				dexProg.addClass(dexClass);

				try {
					final ClassDataItem.EncodedMethod[] directMethods = classDefinition.getClassDataItem().getDirectMethods();
					if (directMethods != null) {
						for (ClassDataItem.EncodedMethod method : directMethods) {
							if (method.codeItem == null	|| method.codeItem.getInstructions().length == 0) {
								continue;
							}

							final DexMethod dexMethod = DexMethod.build(method, conf.deodex, conf.inlineResolver);
							dexClass.addMethod(dexMethod);
						}
					}
					
					final ClassDataItem.EncodedMethod[] virtualMethods = classDefinition.getClassDataItem().getVirtualMethods();
					if (virtualMethods != null) {
						for (ClassDataItem.EncodedMethod method : virtualMethods) {
							if (method.codeItem == null || method.codeItem.getInstructions().length == 0) {
								continue;
							}

							final DexMethod dexMethod = DexMethod.build(method, conf.deodex, conf.inlineResolver);
							dexClass.addMethod(dexMethod);
						}
					}
				} catch (Exception ex) {
					conf.out.println("\n\nError occured while disassembling class "	+ classDescriptor.replace('/', '.')	+ " - skipping class");
					ex.printStackTrace(conf.out);
				}
			}

			return dexProg;
		} catch (IOException e) {
			throw new DexAnalysisException(e);
		}
	}
	
	public static class Config {
		public String androidJars = "data/core.jar:data/ext.jar:data/framework.jar:data/android.policy.jar:data/services.jar";
		public PrintStream out = System.out;
		public boolean deodex = false;
		public InlineMethodResolver inlineResolver = null;
		
		public String toString() {
			final StringBuilder sb = new StringBuilder("DexAnalysis configuration:\n");
			final Class<? extends Config> thisClass = getClass();
			
			for (final java.lang.reflect.Field f : thisClass.getFields()) {
				try {
					sb.append(f.getName() + " = ");
					final Class<?> fType = f.getType();

					if (fType.isPrimitive()) {
						final String  n = fType.getName();
						if (n.equals("boolean")) {
							sb.append(f.getBoolean(this) + "\n");
						} else if (n.equals("byte")) {
							sb.append(f.getByte(this) + "\n");
						} else if (n.equals("char")) {
							sb.append(f.getChar(this) + "\n");
						} else if (n.equals("double")) {
							sb.append(f.getDouble(this) + "\n");
						} else if (n.equals("float")) {
							sb.append(f.getFloat(this) + "\n");
						} else if (n.equals("int")) {
							sb.append(f.getInt(this) + "\n");
						} else if (n.equals("long")) {
							sb.append(f.getLong(this) + "\n");
						} else if (n.equals("short")) {
							sb.append(f.getShort(this) + "\n");
						} else {
							throw new IllegalStateException("unknown primitive type: " + fType.getName());
						}
					} else {
						final Object val = f.get(this);
						sb.append((val == null ? "null" : val.toString()) + "\n");
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
			
			return sb.toString();
		}

	}
	
	private static final Pattern extJarPattern = Pattern.compile("(?:^|\\\\|/)ext.(?:jar|odex)$");
	private static boolean isExtJar(String dexFilePath) {
	    Matcher m = extJarPattern.matcher(dexFilePath);
	    return m.find();
	}

}
