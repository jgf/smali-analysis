package org.jf.dexlib.Interface;


/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 * 
 */
public interface DexAnalysis<I extends DexAnalysis.Input> {

	DexProgram analyze(I program) throws DexAnalysisException;

	public interface Input {}
	
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

}
