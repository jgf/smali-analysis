package org.jf.dexlib.Interface;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexAnalysis {

	public static class Config {
		public String androidJars;
	}
	
	public static class DexAnalysisException extends Exception {

		private static final long serialVersionUID = 4473332448782956303L;
		
		public DexAnalysisException() {
			super();
		}

		public DexAnalysisException(final String message) {
			super(message);
		}

		public DexAnalysisException(final Throwable cause) {
			super(cause);
		}

		public DexAnalysisException(final String message, final Throwable cause) {
			super(message, cause);
		}

	}
	
	private final Config conf;

	public DexAnalysis(final Config conf) {
		this.conf = conf;
	}
	
	public DexProgram analyze(final String programDexFile) {
		return null;
	}
	
}
