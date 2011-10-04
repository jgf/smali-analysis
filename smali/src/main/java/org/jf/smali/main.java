/*
 * [The "BSD licence"]
 * Copyright (c) 2010 Ben Gruver (JesusFreke)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.smali;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Analysis.graphs.GraphDumper;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
import org.jf.util.ClassFileNameHandler;
import org.jf.util.ConsoleUtil;
import org.jf.util.smaliHelpFormatter;

/**
 * Main class for smali. It recognizes enough options to be able to dispatch
 * to the right "actual" main.
 */
public class main {

    public static final String VERSION;

    private final static Options basicOptions;
    private final static Options debugOptions;
    private final static Options options;

    static {
        basicOptions = new Options();
        debugOptions = new Options();
        options = new Options();
        buildOptions();

        InputStream templateStream = main.class.getClassLoader().getResourceAsStream("smali.properties");
        Properties properties = new Properties();
        String version = "(unknown)";
        try {
            properties.load(templateStream);
            version = properties.getProperty("application.version");
        } catch (IOException ex) {
        }
        VERSION = version;
    }


    /**
     * This class is uninstantiable.
     */
    private main() {
    }

    /**
     * Run!
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage();
            return;
        }

        boolean sort = false;
        boolean fixStringConst = true;
        boolean fixGoto = true;
        boolean verboseErrors = false;
        boolean oldLexer = false;
        boolean printTokens = false;
        boolean graphCFG = false;   // dump control flow graphs
        boolean graphCDG = false;   // dump control dependence graphs
        boolean graphDOM = false;   // dump dominator trees
        boolean graphIncludeExceptions = false;   // include uncatched exceptions
         
        String outputGraphDir = "./out/"; // use ./out/ if not set
        String outputDexFile = "out.dex";
        String dumpFileName = null;
        String bootClassPath = null; // only used for analysis needed for graph computation
        StringBuffer extraBootClassPathEntries = new StringBuffer();
        List<String> bootClassPathDirs = new ArrayList<String>();
        bootClassPathDirs.add(".");

        String[] remainingArgs = commandLine.getArgs();

        Option[] options = commandLine.getOptions();

        for (int i=0; i<options.length; i++) {
            Option option = options[i];
            String opt = option.getOpt();

            switch (opt.charAt(0)) {
                case 'v':
                    version();
                    return;
                case '?':
                    while (++i < options.length) {
                        if (options[i].getOpt().charAt(0) == '?') {
                            usage(true);
                            return;
                        }
                    }
                    usage(false);
                    return;
                case 'o':
                    outputDexFile = commandLine.getOptionValue("o");
                    break;
                case 'D':
                    dumpFileName = commandLine.getOptionValue("D", outputDexFile + ".dump");
                    break;
                case 'S':
                    sort = true;
                    break;
                case 'C':
                    fixStringConst = false;
                    break;
                case 'G':
                    fixGoto = false;
                    break;
                case 'V':
                    verboseErrors = true;
                    break;
                case 'L':
                    oldLexer = true;
                    break;
                case 'T':
                    printTokens = true;
                    break;
                case 'd':
                    bootClassPathDirs.add(option.getValue());
                    break;
                case 'c':
                    String bcp = commandLine.getOptionValue("c");
                    if (bcp != null && bcp.charAt(0) == ':') {
                        extraBootClassPathEntries.append(bcp);
                    } else {
                        bootClassPath = bcp;
                    }
                    break;
                case 'g': {
                    String[] values = option.getValue().split(",");
                    if (values != null && values.length > 0) {
                        
                        if (values[values.length - 1].contains("=")) {
                            // make two seperate args out of the last arg.
                            // e.g. ['CFG', 'CDG=/tmp'] => ['CFG', 'CDG', '=/tmp']
                            String[] tmp = new String[values.length + 1];
                            System.arraycopy(values, 0, tmp, 0, values.length - 1);
                            final String lastArg = values[values.length - 1];
                            final int indexOfEq = lastArg.indexOf('=');
                            tmp[values.length - 1] = lastArg.substring(0, indexOfEq);
                            tmp[values.length] = lastArg.substring(indexOfEq);
                            values = tmp;
                        }
                        
                        for (int val = 0; val < values.length; val++) {
                            final String value = values[val];
                            if (value.equalsIgnoreCase("CFG")) {
                                graphCFG = true;
                            } else if (value.equalsIgnoreCase("DOM")) {
                                graphDOM = true;
                            } else if (value.equalsIgnoreCase("CDG")) {
                                graphCDG = true;
                            } else if (value.equalsIgnoreCase("EXC")) {
                                graphIncludeExceptions = true;
                            } else if (value.startsWith("=")) {
                                final String dir = value.substring(1);
                                
                                final File testDir = new File(dir);
                                if (!(testDir.exists() && testDir.isDirectory() && testDir.canWrite()) && !testDir.mkdirs()) {
                                    System.err.println("'" + dir + "' is not an existing and writable directory and it could not be created.");
                                    usage();
                                    return;
                                } else {
                                    outputGraphDir = dir;
                                    // append path seperator if needed
                                    if (outputGraphDir.length() > 0 && !outputGraphDir.endsWith("" + File.separatorChar)) {
                                        outputGraphDir += File.separatorChar;
                                    }
                                }
                            } else {
                                System.err.println("Unknown argument in option '-g': " + value);
                                usage();
                                return;
                            }
                        }
                    }
                	} break;
                default:
                    assert false;
            }
        }

        if (remainingArgs.length == 0) {
            usage();
            return;
        }

        try {
            LinkedHashSet<File> filesToProcess = new LinkedHashSet<File>();

            for (String arg: remainingArgs) {
                    File argFile = new File(arg);

                    if (!argFile.exists()) {
                        throw new RuntimeException("Cannot find file or directory \"" + arg + "\"");
                    }

                    if (argFile.isDirectory()) {
                        getSmaliFilesInDir(argFile, filesToProcess);
                    } else if (argFile.isFile()) {
                        filesToProcess.add(argFile);
                    }
            }

            DexFile dexFile = new DexFile();

            boolean errors = false;

            for (File file: filesToProcess) {
                if (!assembleSmaliFile(file, dexFile, verboseErrors, oldLexer, printTokens)) {
                    errors = true;
                }
            }

            if (errors) {
                System.exit(1);
            }


            if (sort) {
                dexFile.setSortAllItems(true);
            }

            if (fixStringConst || fixGoto) {
                fixInstructions(dexFile, fixStringConst, fixGoto);
            }

            dexFile.place();

            final boolean dumpGraphs = graphCDG || graphCFG || graphDOM;
            if (dumpGraphs) {
                ClassPath.ClassPathErrorHandler classPathErrorHandler = new ClassPath.ClassPathErrorHandler() {
                    public void ClassPathError(String className, Exception ex) {
                        System.err.println(String.format("Skipping %s", className));
                        ex.printStackTrace(System.err);
                    }
                };

                String[] extraBootClassPathArray = null;
                if (extraBootClassPathEntries != null && extraBootClassPathEntries.length() > 0) {
                    assert extraBootClassPathEntries.charAt(0) == ':';
                    extraBootClassPathArray = extraBootClassPathEntries.substring(1).split(":");
                }

                String[] bootClassPathDirsArray = new String[bootClassPathDirs.size()];
                for (int i=0; i<bootClassPathDirsArray.length; i++) {
                    bootClassPathDirsArray[i] = bootClassPathDirs.get(i);
                }

                final String dexFilePath = ".";
                
                if (dexFile.isOdex() && bootClassPath == null) {
                    ClassPath.InitializeClassPathFromOdex(bootClassPathDirsArray, extraBootClassPathArray, dexFilePath, dexFile,
                            classPathErrorHandler);
                } else {
                    String[] bootClassPathArray = null;
                    if (bootClassPath != null) {
                        bootClassPathArray = bootClassPath.split(":");
                    }
                    ClassPath.InitializeClassPath(bootClassPathDirsArray, bootClassPathArray, extraBootClassPathArray,
                            dexFilePath, dexFile, classPathErrorHandler);
                }

                File outputGraphFile = new File(outputGraphDir);
                if (!outputGraphFile.exists()) {
                    if (!outputGraphFile.mkdirs()) {
                        System.err.println("Can't create the graph output directory " + outputGraphDir);
                        System.exit(1);
                    }
                }
                
                ClassFileNameHandler graphPrefixNameHandler = new ClassFileNameHandler(outputGraphFile, ".");

                for (ClassDefItem clsDef : dexFile.ClassDefsSection.getItems()) {
                    String classDescriptor = clsDef.getClassType().getTypeDescriptor();
                    
//                    System.out.print("\nWorking on '" + classDescriptor + "': ");
                    
                    //validate that the descriptor is formatted like we expect
                    if (classDescriptor.charAt(0) != 'L' ||
                        classDescriptor.charAt(classDescriptor.length()-1) != ';') {
                        System.err.println("Unrecognized class descriptor - " + classDescriptor + " - skipping class");
                        continue;
                    }

                    File graphPrefixFile = graphPrefixNameHandler.getUniqueFilenameForClass(classDescriptor);
                    File graphDir = graphPrefixFile.getParentFile();

                    if (!graphDir.exists()) {
                        if (!graphDir.mkdirs()) {
                            System.err.println("Unable to create directory " + graphDir.toString() + " - skipping graph output");
                            continue;
                        }
                    }

                    ClassDataItem clsData = clsDef.getClassData();
                    if (clsData == null) {
                        continue;
                    }
                    
                    String filePrefix = graphPrefixFile.toString();
                    GraphDumper gd = new GraphDumper(filePrefix, false, graphCFG, graphDOM, graphCDG, graphIncludeExceptions);
                    
                    if (clsData.getDirectMethods() != null) {
                        for (EncodedMethod em : clsData.getDirectMethods()) {
                            MethodAnalyzer analyze = new MethodAnalyzer(em, false);
                            analyze.analyze();
                            List<AnalyzedInstruction> instructions = analyze.getInstructions();
                            final String mName = em.method.getVirtualMethodString();
                            gd.dump(instructions, mName);
                        }
                    }
                    
                    if (clsData.getVirtualMethods() != null) {
                        for (EncodedMethod em : clsData.getVirtualMethods()) {
                            if (em.codeItem == null) {
                                continue;
                            }
                            
                            MethodAnalyzer analyze = new MethodAnalyzer(em, false);
                            analyze.analyze();
                            List<AnalyzedInstruction> instructions = analyze.getInstructions();
                            final String mName = em.method.getVirtualMethodString();
                            gd.dump(instructions, mName);
                        }
                    }
                }
            }
            
            ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();

            if (dumpFileName != null) {
                out.enableAnnotations(120, true);
            }

            dexFile.writeTo(out);

            byte[] bytes = out.toByteArray();

            DexFile.calcSignature(bytes);
            DexFile.calcChecksum(bytes);

            if (dumpFileName != null) {
                out.finishAnnotating();

                FileWriter fileWriter = new FileWriter(dumpFileName);
                out.writeAnnotationsTo(fileWriter);
                fileWriter.close();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(outputDexFile);

            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (RuntimeException ex) {
            System.err.println("\nUNEXPECTED TOP-LEVEL EXCEPTION:");
            ex.printStackTrace();
            System.exit(2);
        } catch (Throwable ex) {
            System.err.println("\nUNEXPECTED TOP-LEVEL ERROR:");
            ex.printStackTrace();
            System.exit(3);
        }
    }

    private static void getSmaliFilesInDir(File dir, Set<File> smaliFiles) {
        for(File file: dir.listFiles()) {
            if (file.isDirectory()) {
                getSmaliFilesInDir(file, smaliFiles);
            } else if (file.getName().endsWith(".smali")) {
                smaliFiles.add(file);
            }
        }
    }

    private static void fixInstructions(DexFile dexFile, boolean fixStringConst, boolean fixGoto) {
        dexFile.place();

        byte[] newInsns = null;

        for (CodeItem codeItem: dexFile.CodeItemsSection.getItems()) {
            codeItem.fixInstructions(fixStringConst, fixGoto);
        }
    }

    private static boolean assembleSmaliFile(File smaliFile, DexFile dexFile, boolean verboseErrors, boolean oldLexer,
                                             boolean printTokens)
            throws Exception {
        CommonTokenStream tokens;


        boolean lexerErrors = false;
        LexerErrorInterface lexer;

        if (oldLexer) {
            ANTLRFileStream input = new ANTLRFileStream(smaliFile.getAbsolutePath(), "UTF-8");
            input.name = smaliFile.getAbsolutePath();

            lexer = new smaliLexer(input);
            tokens = new CommonTokenStream((TokenSource)lexer);
        } else {
            FileInputStream fis = new FileInputStream(smaliFile.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");

            lexer = new smaliFlexLexer(reader);
            ((smaliFlexLexer)lexer).setSourceFile(smaliFile);
            tokens = new CommonTokenStream((TokenSource)lexer);
        }

        if (printTokens) {
            tokens.getTokens();
            
            for (int i=0; i<tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.getChannel() == smaliLexer.HIDDEN) {
                    continue;
                }

                System.out.println(smaliParser.tokenNames[token.getType()] + ": " + token.getText());
            }
        }

        smaliParser parser = new smaliParser(tokens);
        parser.setVerboseErrors(verboseErrors);

        smaliParser.smali_file_return result = parser.smali_file();

        if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
            return false;
        }

        CommonTree t = (CommonTree) result.getTree();

        CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
        treeStream.setTokenStream(tokens);

        smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);

        dexGen.dexFile = dexFile;
        dexGen.smali_file();

        if (dexGen.getNumberOfSyntaxErrors() > 0) {
            return false;
        }

        return true;
    }


    /**
     * Prints the usage message.
     */
    private static void usage(boolean printDebugOptions) {
        smaliHelpFormatter formatter = new smaliHelpFormatter();
        formatter.setWidth(ConsoleUtil.getConsoleWidth());

        formatter.printHelp("java -jar smali.jar [options] [--] [<smali-file>|folder]*",
                "assembles a set of smali files into a dex file", basicOptions, "");

        if (printDebugOptions) {
            System.out.println();
            System.out.println("Debug Options:");

            StringBuffer sb = new StringBuffer();
            formatter.renderOptions(sb, debugOptions);
            System.out.println(sb.toString());
        }
    }

    private static void usage() {
        usage(true);
    }

    /**
     * Prints the version message.
     */
    private static void version() {
        System.out.println("smali " + VERSION + " (http://smali.googlecode.com)");
        System.out.println("Copyright (C) 2010 Ben Gruver (JesusFreke@JesusFreke.com)");
        System.out.println("BSD license (http://www.opensource.org/licenses/bsd-license.php)");
        System.exit(0);
    }

    @SuppressWarnings("static-access")
    private static void buildOptions() {
        Option versionOption = OptionBuilder.withLongOpt("version")
                .withDescription("prints the version then exits")
                .create("v");

        Option helpOption = OptionBuilder.withLongOpt("help")
                .withDescription("prints the help message then exits. Specify twice for debug options")
                .create("?");

        Option outputOption = OptionBuilder.withLongOpt("output")
                .withDescription("the name of the dex file that will be written. The default is out.dex")
                .hasArg()
                .withArgName("FILE")
                .create("o");

        Option dumpOption = OptionBuilder.withLongOpt("dump-to")
                .withDescription("additionally writes a dump of written dex file to FILE (<dexfile>.dump by default)")
                .hasOptionalArg()
                .withArgName("FILE")
                .create("D");

        Option sortOption = OptionBuilder.withLongOpt("sort")
                .withDescription("sort the items in the dex file into a canonical order before writing")
                .create("S");

        Option noFixStringConstOption = OptionBuilder.withLongOpt("no-fix-string-const")
                .withDescription("Don't replace string-const instructions with string-const/jumbo where appropriate")
                .create("C");

        Option noFixGotoOption = OptionBuilder.withLongOpt("no-fix-goto")
                .withDescription("Don't replace goto type instructions with a larger version where appropriate")
                .create("G");

        Option verboseErrorsOption = OptionBuilder.withLongOpt("verbose-errors")
                .withDescription("Generate verbose error messages")
                .create("V");

        Option oldLexerOption = OptionBuilder.withLongOpt("old-lexer")
                .withDescription("Use the old lexer")
                .create("L");

        Option printTokensOption = OptionBuilder.withLongOpt("print-tokens")
                .withDescription("Print the name and text of each token")
                .create("T");

        Option classPathOption = OptionBuilder.withLongOpt("bootclasspath")
        .withDescription("the bootclasspath jars to use, for analysis. Defaults to " +
                "core.jar:ext.jar:framework.jar:android.policy.jar:services.jar. If the value begins with a " +
                ":, it will be appended to the default bootclasspath instead of replacing it")
        .hasOptionalArg()
        .withArgName("BOOTCLASSPATH")
        .create("c");

        Option classPathDirOption = OptionBuilder.withLongOpt("bootclasspath-dir")
                .withDescription("the base folder to look for the bootclasspath files in. Defaults to the current " +
                        "directory")
                .hasArg()
                .withArgName("DIR")
                .create("d");

        Option dumpGraphOption = OptionBuilder.withLongOpt("dump-graph")
        .hasArg()
        .withArgName("DUMP_GRAPHS")
        .withDescription("write the specificed type(s) of graphs for each method to a .dot file. " +
                "At least one of the options CFG, DOM or CDG have to be specified. All values " +
                "have to be seperated with a ',' with NO SPACE between them.\nValid values are:\n" + 
                "CFG: control flow graph\n" +
                "DOM: dominator tree\n" + 
                "CDG: control dependence graph\n" +
                "EXC: include uncatched exceptions in analysis\n" +
                "=<DIR>: only valid as last option. Sets the output directory to the specified value. " +
                "This will override the default behaviour: " +
                "If not specified otherwise the graph files are put in './out/'.\n" +
                "Some examples of valid options: '-g CFG,DOM=/tmp', '-g CFG' or '-g CDG,EXC'")
        .create("g");

        basicOptions.addOption(versionOption);
        basicOptions.addOption(helpOption);
        basicOptions.addOption(outputOption);

        debugOptions.addOption(dumpOption);
        debugOptions.addOption(sortOption);
        debugOptions.addOption(noFixStringConstOption);
        debugOptions.addOption(noFixGotoOption);
        debugOptions.addOption(verboseErrorsOption);
        debugOptions.addOption(oldLexerOption);
        debugOptions.addOption(printTokensOption);
        debugOptions.addOption(classPathOption);
        debugOptions.addOption(classPathDirOption);
        debugOptions.addOption(dumpGraphOption);

        for (Object option: basicOptions.getOptions()) {
            options.addOption((Option)option);
        }

        for (Object option: debugOptions.getOptions()) {
            options.addOption((Option)option);
        }
    }
}