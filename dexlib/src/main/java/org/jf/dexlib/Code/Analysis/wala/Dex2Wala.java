package org.jf.dexlib.Code.Analysis.wala;

import java.util.List;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

import com.ibm.wala.classLoader.JavaLanguage.JavaInstructionFactory;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;

public class Dex2Wala {

	private List<SSAInstruction> instructions;
	
	public static List<SSAInstruction> build(final List<AnalyzedInstruction> instructions, final String name) {
		Dex2Wala dex2wala = new Dex2Wala();
		dex2wala.run(instructions, name);
		
		return dex2wala.getInstructions();
    }

	private List<SSAInstruction> getInstructions() {
		return instructions;
	}

	private void run(List<AnalyzedInstruction> instructions, String name) {
		final SSAInstructionFactory ssa = new JavaInstructionFactory();
		
		System.out.println(getClass().toString() + " implement me!");
	}

}
