package org.jf.dexlib.Code.Analysis.wala;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

import com.ibm.wala.classLoader.JavaLanguage.JavaInstructionFactory;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IComparisonInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.shrikeBT.IUnaryOpInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;

public class Dex2Wala {

	private List<SSAInstruction> instructions;
	
	private Dex2Wala() {
		instructions = new LinkedList<SSAInstruction>();
	}
	
	public static List<SSAInstruction> build(final List<AnalyzedInstruction> instructions, final String name) {
		System.out.println(name);
		Dex2Wala dex2wala = new Dex2Wala();
		dex2wala.run(instructions, name);
		
		return dex2wala.getInstructions();
    }

	private List<SSAInstruction> getInstructions() {
		return instructions;
	} 

	private void run(List<AnalyzedInstruction> instructions, String name) {
		final SSAInstructionFactory ssa = new JavaInstructionFactory();
		
		for(AnalyzedInstruction inst : instructions) {
			switch (inst.getInstruction().opcode) {
			case ADD_DOUBLE:
			case ADD_DOUBLE_2ADDR:
			case ADD_FLOAT:
			case ADD_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case ADD_INT:
			case ADD_INT_2ADDR:
			case ADD_INT_LIT16:
			case ADD_INT_LIT8:
			case ADD_LONG:
			case ADD_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
			case AGET:
			case AGET_BOOLEAN:
			case AGET_BYTE:
			case AGET_CHAR:
			case AGET_OBJECT:
			case AGET_SHORT:
			case AGET_WIDE:
				System.out.println("Was ist AGET?");
				break;
			case AND_INT:
			case AND_INT_2ADDR:
			case AND_INT_LIT16:
			case AND_INT_LIT8:
			case AND_LONG:
			case AND_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.AND, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case APUT:
			case APUT_BOOLEAN:
			case APUT_BYTE:
			case APUT_CHAR:
			case APUT_OBJECT:
			case APUT_SHORT:
			case APUT_WIDE:
				System.out.println("Was ist APUT?");
				break;
			case ARRAY_LENGTH:
				this.instructions.add(ssa.ArrayLengthInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1))); //Reihenfolge der Parameter
				break;
			case CHECK_CAST: //WElches?
				this.instructions.add(ssa.CheckCastInstruction(inst.getInstructionIndex(),
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2))); //Reihenfolge?
				break;
			case CMP_LONG:
				this.instructions.add(ssa.ComparisonInstruction(inst.getInstructionIndex(), IComparisonInstruction.Operator.CMP,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2))); //Reihenfolge?
				break;
			case CMPG_DOUBLE:
			case CMPG_FLOAT:
				this.instructions.add(ssa.ComparisonInstruction(inst.getInstructionIndex(), IComparisonInstruction.Operator.CMPG,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2))); //Reihenfolge?
				break;
			case CMPL_DOUBLE:
			case CMPL_FLOAT:
				this.instructions.add(ssa.ComparisonInstruction(inst.getInstructionIndex(), IComparisonInstruction.Operator.CMPL,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2))); //Reihenfolge?
				break;
			case CONST:
			case CONST_16:
			case CONST_4:
			case CONST_CLASS:
			case CONST_HIGH16:
			case CONST_STRING:
			case CONST_STRING_JUMBO:
			case CONST_WIDE:
			case CONST_WIDE_16:
			case CONST_WIDE_32:
			case CONST_WIDE_HIGH16:
				System.out.println("Was ist CONST?");
				break;
			case DIV_DOUBLE:
			case DIV_DOUBLE_2ADDR:
			case DIV_FLOAT:
			case DIV_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case DIV_INT:
			case DIV_INT_2ADDR:
			case DIV_INT_LIT16:
			case DIV_INT_LIT8:
			case DIV_LONG:
			case DIV_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case DOUBLE_TO_FLOAT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Double, TypeReference.Float, false));//Reihenfolge der Parameter
				break;
			case DOUBLE_TO_INT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Double, TypeReference.Int, false));//Reihenfolge der Parameter
				break;
			case DOUBLE_TO_LONG:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Double, TypeReference.Long, false));//Reihenfolge der Parameter
				break;
			case EXECUTE_INLINE:
			case EXECUTE_INLINE_RANGE:
				System.out.println("Was ist Execute?");
				break;
			case FILL_ARRAY_DATA:
			case FILLED_NEW_ARRAY:
			case FILLED_NEW_ARRAY_RANGE:
				System.out.println("Was ist Fill?");
				break;
			case FLOAT_TO_DOUBLE:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Float, TypeReference.Double, false));//Reihenfolge der Parameter
				break;
			case FLOAT_TO_INT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Float, TypeReference.Int, false));//Reihenfolge der Parameter
				break;
			case FLOAT_TO_LONG:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Float, TypeReference.Long, false));//Reihenfolge der Parameter
				break;
			case GOTO:
			case GOTO_16:
			case GOTO_32:
				this.instructions.add(ssa.GotoInstruction(inst.getInstructionIndex()));
				break;
			case IF_EQ:
			case IF_EQZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.EQ,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_GE:
			case IF_GEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GE,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_GT:
			case IF_GTZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GT,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_LE:
			case IF_LEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LE,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_LT:
			case IF_LTZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LT,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_NE:
			case IF_NEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.NE,
						null, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IGET:
			case IGET_BOOLEAN:
			case IGET_BYTE:
			case IGET_CHAR:
			case IGET_OBJECT:
			case IGET_OBJECT_QUICK:
			case IGET_OBJECT_VOLATILE:
			case IGET_QUICK:
			case IGET_SHORT:
			case IGET_VOLATILE:
			case IGET_WIDE:
			case IGET_WIDE_QUICK:
			case IGET_WIDE_VOLATILE:
				this.instructions.add(ssa.GetInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),inst.getParameterRegister(1), null)); //Reihenfolge Referenz
				break;
			case INSTANCE_OF:
				this.instructions.add(ssa.InstanceofInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),inst.getParameterRegister(1), TypeReference.Unknown));  //Reihenfolge TypRef
				break;
			case INT_TO_BYTE:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Byte, false));//Reihenfolge der Parameter
				break;
			case INT_TO_CHAR:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Char, false));//Reihenfolge der Parameter
				break;
			case INT_TO_DOUBLE:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Double, false));//Reihenfolge der Parameter
				break;
			case INT_TO_FLOAT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Float, false));//Reihenfolge der Parameter
				break;
			case INT_TO_LONG:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Long, false));//Reihenfolge der Parameter
				break;
			case INT_TO_SHORT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Int, TypeReference.Short, false));//Reihenfolge der Parameter
				break;
			case INVOKE_DIRECT:
			case INVOKE_DIRECT_EMPTY:
			case INVOKE_DIRECT_RANGE:
			case INVOKE_INTERFACE:
			case INVOKE_INTERFACE_RANGE:
			case INVOKE_STATIC:
			case INVOKE_STATIC_RANGE:
			case INVOKE_SUPER:
			case INVOKE_SUPER_QUICK:
			case INVOKE_SUPER_QUICK_RANGE:
			case INVOKE_SUPER_RANGE:
			case INVOKE_VIRTUAL:
			case INVOKE_VIRTUAL_QUICK:
			case INVOKE_VIRTUAL_QUICK_RANGE:
			case INVOKE_VIRTUAL_RANGE:
				System.out.println("Invoke " + inst.getInstruction().opcode);
				break;
			case IPUT:
			case IPUT_BOOLEAN:
			case IPUT_BYTE:
			case IPUT_CHAR:
			case IPUT_OBJECT:
			case IPUT_OBJECT_QUICK:
			case IPUT_OBJECT_VOLATILE:
			case IPUT_QUICK:
			case IPUT_SHORT:
			case IPUT_VOLATILE:
			case IPUT_WIDE:
			case IPUT_WIDE_QUICK:
			case IPUT_WIDE_VOLATILE:
				this.instructions.add(ssa.PutInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), null)); //Reihenfolge FieldREfe
				break;
			case LONG_TO_DOUBLE:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Long, TypeReference.Double, false));//Reihenfolge der Parameter
				break;
			case LONG_TO_FLOAT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Long, TypeReference.Float, false));//Reihenfolge der Parameter
				break;
			case LONG_TO_INT:
				this.instructions.add(ssa.ConversionInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), TypeReference.Long, TypeReference.Int, false));//Reihenfolge der Parameter
				break;
			case MONITOR_ENTER:
				this.instructions.add(ssa.MonitorInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), true));
				break;
			case MONITOR_EXIT:
				this.instructions.add(ssa.MonitorInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), false));
				break;
			case MOVE:
			case MOVE_16:
			case MOVE_EXCEPTION:
			case MOVE_FROM16:
			case MOVE_OBJECT:
			case MOVE_OBJECT_16:
			case MOVE_OBJECT_FROM16:
			case MOVE_RESULT:
			case MOVE_RESULT_OBJECT:
			case MOVE_RESULT_WIDE:
			case MOVE_WIDE:
			case MOVE_WIDE_16:
			case MOVE_WIDE_FROM16:
				System.out.println("Was ist move?"); //!!!
				break;
			case MUL_DOUBLE:
			case MUL_DOUBLE_2ADDR:
			case MUL_FLOAT:
			case MUL_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case MUL_INT:
			case MUL_INT_2ADDR:
			case MUL_INT_LIT16:
			case MUL_INT_LIT8:
			case MUL_LONG:
			case MUL_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case NEG_DOUBLE:
			case NEG_FLOAT:
			case NEG_INT:
			case NEG_LONG:
				this.instructions.add(ssa.UnaryOpInstruction(inst.getInstructionIndex(), IUnaryOpInstruction.Operator.NEG, inst.getParameterRegister(0), inst.getParameterRegister(1)));
				break;
			case NEW_ARRAY:
				System.out.println("Was ist new Array?"); //!!!
				break;
			case NEW_INSTANCE:
				System.out.println("Was ist new instance?"); //!!!
				break;
			case NOP:
				System.out.println("Nop gibts nicht"); //!!!
				break;
			case NOT_INT:
			case NOT_LONG:
				System.out.println("Was ist NOT"); //!!!
				break;
			case OR_INT:
			case OR_INT_2ADDR:
			case OR_INT_LIT16:
			case OR_INT_LIT8:
			case OR_LONG:
			case OR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.OR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case PACKED_SWITCH:
//				this.instructions.add(ssa.SwitchInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1),
//						inst.getParameterRegister(1)));
				System.out.println("Array Register!");
				break;
			case REM_DOUBLE:
			case REM_DOUBLE_2ADDR:
			case REM_FLOAT:
			case REM_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case REM_INT:
			case REM_INT_2ADDR:
			case REM_INT_LIT16:
			case REM_INT_LIT8:
			case REM_LONG:
			case REM_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case RETURN:
			case RETURN_OBJECT:
			case RETURN_VOID:
			case RETURN_WIDE:
				this.instructions.add(ssa.ReturnInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), false));
				break;
			case RSUB_INT:
			case RSUB_INT_LIT8:
				System.out.println("Was ist RSUB?"); //!!!
				break;
			case SGET:
			case SGET_BOOLEAN:
			case SGET_BYTE:
			case SGET_CHAR:
			case SGET_OBJECT:
			case SGET_OBJECT_VOLATILE:
			case SGET_SHORT:
			case SGET_VOLATILE:
			case SGET_WIDE:
			case SGET_WIDE_VOLATILE:
				this.instructions.add(ssa.GetInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1), null)); //Reihenfolge Fieldref
				break;
			case SHL_INT:
			case SHL_INT_2ADDR:
			case SHL_INT_LIT8:
			case SHL_LONG:
			case SHL_LONG_2ADDR:
			case SHR_INT:
			case SHR_INT_2ADDR:
			case SHR_INT_LIT8:
			case SHR_LONG:
			case SHR_LONG_2ADDR:
				System.out.println("Shift gibt es nicht!"); //!!!
				break;
			case SPARSE_SWITCH:
				System.out.println("siehe PACKED_SWITCH"); //!!!
				break;
			case SPUT:
			case SPUT_BOOLEAN:
			case SPUT_BYTE:
			case SPUT_CHAR:
			case SPUT_OBJECT:
			case SPUT_OBJECT_VOLATILE:
			case SPUT_SHORT:
			case SPUT_VOLATILE:
			case SPUT_WIDE:
			case SPUT_WIDE_VOLATILE:
				this.instructions.add(ssa.PutInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1), null)); //Reihenfolge Fieldref
				break;
			case SUB_DOUBLE:
			case SUB_DOUBLE_2ADDR:
			case SUB_FLOAT:
			case SUB_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case SUB_INT:
			case SUB_INT_2ADDR:
			case SUB_LONG:
			case SUB_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case THROW:
				this.instructions.add(ssa.ThrowInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0)));
				break;
			case USHR_INT:
			case USHR_INT_2ADDR:
			case USHR_INT_LIT8:
			case USHR_LONG:
			case USHR_LONG_2ADDR:
				System.out.println("Siehe Shift"); //!!!
				break;
			case XOR_INT:
			case XOR_INT_2ADDR:
			case XOR_INT_LIT16:
			case XOR_INT_LIT8:
			case XOR_LONG:
			case XOR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.XOR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;				
			default:
				System.out.println("ToDO " + inst.getInstruction().opcode);
				break;
			}
		}
		
		System.out.println(this.instructions);
	}

}
