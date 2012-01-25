package org.jf.dexlib.Code.Analysis.wala;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.LiteralInstruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.JavaLanguage.JavaInstructionFactory;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IComparisonInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch;
import com.ibm.wala.shrikeBT.IInvokeInstruction.IDispatch;
import com.ibm.wala.shrikeBT.IShiftInstruction;
import com.ibm.wala.shrikeBT.IUnaryOpInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

public class Dex2Wala {

	private List<SSAInstruction> instructions;
	private SymbolTable symbols;
	
	private Dex2Wala() {
		instructions = new LinkedList<SSAInstruction>();
		symbols = new SymbolTable(12);
	}
	
	public static List<SSAInstruction> build(final List<AnalyzedInstruction> instructions, final String name) {
		System.out.println("Dex2Wala: " + name);
		Dex2Wala dex2wala = new Dex2Wala();
		dex2wala.run(instructions, name);
		
		return dex2wala.getInstructions();
    }

	private List<SSAInstruction> getInstructions() {
		return instructions;
	} 

	private void run(List<AnalyzedInstruction> instructions, String name) {
		final SSAInstructionFactory ssa = new JavaInstructionFactory();
		TypeIdItem ti;
		TypeReference trf, trc;
		FieldIdItem fi;
		FieldReference fr;
		MethodIdItem mi;
		MethodReference mr;
		CallSiteReference cr;
		int regCount, startReg;
		int params[];
		int programCounter = 0;
		
		for(AnalyzedInstruction inst : instructions) {
			if (inst.getInstruction().opcode.odexOnly())
			{
				System.out.println("odex");
				continue;
			}
			System.out.println(inst.getInstruction().opcode);
			switch (inst.getInstruction().opcode) {
			case ADD_DOUBLE:
			case ADD_FLOAT:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case ADD_FLOAT_2ADDR:
			case ADD_DOUBLE_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), false)); //Reihenfolge der Parameter
				break;
			case ADD_INT:
			case ADD_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case ADD_INT_LIT16:
			case ADD_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Literal
				break;
			case ADD_INT_2ADDR:
			case ADD_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.ADD, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case AGET_BOOLEAN:
				this.instructions.add(ssa.ArrayLoadInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Boolean));
			case AGET_BYTE:
				this.instructions.add(ssa.ArrayLoadInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Byte));
			case AGET_CHAR:
				this.instructions.add(ssa.ArrayLoadInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Char));
			case AGET_SHORT:
				this.instructions.add(ssa.ArrayLoadInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Short));
			case AGET:
			case AGET_OBJECT:
			case AGET_WIDE:
				this.instructions.add(ssa.ArrayLoadInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.JavaLangObject)); //Declared Type?
				break;
			case AND_INT:
			case AND_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.AND, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case AND_INT_LIT16:
			case AND_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.AND, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Literal
				break;
			case AND_INT_2ADDR:
			case AND_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.AND, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case APUT_BOOLEAN:
				this.instructions.add(ssa.ArrayStoreInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Boolean));
			case APUT_BYTE:
				this.instructions.add(ssa.ArrayStoreInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Byte));
			case APUT_CHAR:
				this.instructions.add(ssa.ArrayStoreInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Char));
			case APUT_SHORT:
				this.instructions.add(ssa.ArrayStoreInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.Short));
			case APUT:
			case APUT_OBJECT:
			case APUT_WIDE:
				this.instructions.add(ssa.ArrayStoreInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),
						inst.getParameterRegister(1), inst.getParameterRegister(2), TypeReference.JavaLangObject)); //Declared Type
				break;
			case ARRAY_LENGTH:
				this.instructions.add(ssa.ArrayLengthInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1))); //Reihenfolge der Parameter
				break;
			case CHECK_CAST:
				ti = (TypeIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, ti.getTypeDescriptor());
				this.instructions.add(ssa.CheckCastInstruction(inst.getInstructionIndex(), inst.getDestinationRegister(), inst.getParameterRegister(0), trf));
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
			case CONST_HIGH16:
			case CONST_4:
			case CONST_WIDE:
			case CONST_WIDE_16:
			case CONST_WIDE_32:
			case CONST_WIDE_HIGH16:
				long l = ((LiteralInstruction) inst.getInstruction()).getLiteral();
				symbols.getConstant(l);
				break;
			case CONST_CLASS:
				ti = (TypeIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				String s = ti.getTypeDescriptor();
				symbols.getConstant(s);
				break;
			case CONST_STRING:
			case CONST_STRING_JUMBO:
				StringIdItem it = (StringIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				String st = it.getStringValue();
				symbols.getConstant(st);
				break;
			case DIV_DOUBLE:
			case DIV_FLOAT:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case DIV_DOUBLE_2ADDR:
			case DIV_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), false)); //Reihenfolge der Parameter
				break;
			case DIV_INT:
			case DIV_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case DIV_INT_2ADDR:
			case DIV_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case DIV_INT_LIT16:
			case DIV_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.DIV, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
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
//			case EXECUTE_INLINE:
//			case EXECUTE_INLINE_RANGE:
//				break;
			case FILL_ARRAY_DATA:
			case FILLED_NEW_ARRAY:
			case FILLED_NEW_ARRAY_RANGE:
//				ssa.ArrayStoreInstruction(iindex, arrayref, index, value, declaredType);
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
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.EQ,
						TypeReference.Int, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_EQZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.EQ,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
				break;
			case IF_GE:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GE,
						TypeReference.Int, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_GEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GE,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
			case IF_GT:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GT,
						TypeReference.Int, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_GTZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.GT,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
				break;
			case IF_LE:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LE,
						TypeReference.Int, inst.getParameterRegister(0),inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_LEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LE,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
				break;
			case IF_LT:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LT,
						TypeReference.Int, inst.getParameterRegister(0), inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_LTZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.LT,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
				break;
			case IF_NE:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.NE,
						TypeReference.Int, inst.getParameterRegister(0), inst.getParameterRegister(1))); //Parameter, Z, Typ Referenze
				break;
			case IF_NEZ:
				this.instructions.add(ssa.ConditionalBranchInstruction(inst.getInstructionIndex(), IConditionalBranchInstruction.Operator.NE,
						TypeReference.Int, inst.getParameterRegister(0), symbols.getConstant(0))); //Parameter, Z, Typ Referenze
				break;
			case IGET:
			case IGET_BOOLEAN:
			case IGET_BYTE:
			case IGET_CHAR:
			case IGET_OBJECT:
			case IGET_SHORT:
			case IGET_WIDE:
//			case IGET_VOLATILE:
//			case IGET_OBJECT_VOLATILE:
//			case IGET_WIDE_VOLATILE:
//			case IGET_OBJECT_QUICK:
//			case IGET_QUICK:
//			case IGET_WIDE_QUICK:
			case INSTANCE_OF:
				if (((InstructionWithReference) inst.getInstruction()).getReferencedItem() instanceof TypeIdItem) {
					ti = (TypeIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
					trf = TypeReference.findOrCreate(ClassLoaderReference.Application, ti.getTypeDescriptor());
				} else {
					fi = (FieldIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
					trf = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getFieldType().getTypeDescriptor());
				}
				this.instructions.add(ssa.InstanceofInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0),inst.getParameterRegister(1), trf));  //Reihenfolge TypRef
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
//			case INVOKE_DIRECT_EMPTY:
			case INVOKE_SUPER:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.SPECIAL);
				regCount = ((FiveRegisterInstruction) inst.getInstruction()).getRegCount();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = inst.getParameterRegister(i);
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, inst.getParameterRegister(regCount), cr));
				break;
			case INVOKE_INTERFACE:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.INTERFACE);
				regCount = ((FiveRegisterInstruction) inst.getInstruction()).getRegCount();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = inst.getParameterRegister(i);
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, inst.getParameterRegister(regCount), cr));
				break;
			case INVOKE_STATIC:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.STATIC);
				regCount = ((FiveRegisterInstruction) inst.getInstruction()).getRegCount();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = inst.getParameterRegister(i);
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, inst.getParameterRegister(regCount), cr));
				break;
			case INVOKE_VIRTUAL:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.VIRTUAL);
				regCount = ((FiveRegisterInstruction) inst.getInstruction()).getRegCount();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = inst.getParameterRegister(i);
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, inst.getParameterRegister(regCount), cr));
				break;
			case INVOKE_DIRECT_RANGE:
			case INVOKE_SUPER_RANGE:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.SPECIAL);
				regCount = ((RegisterRangeInstruction) inst.getInstruction()).getRegCount();
				startReg = ((RegisterRangeInstruction) inst.getInstruction()).getStartRegister();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = startReg++;
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, startReg, cr));
				break;
			case INVOKE_INTERFACE_RANGE:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.INTERFACE);
				regCount = ((RegisterRangeInstruction) inst.getInstruction()).getRegCount();
				startReg = ((RegisterRangeInstruction) inst.getInstruction()).getStartRegister();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = startReg++;
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, startReg, cr));
				break;
			case INVOKE_VIRTUAL_RANGE:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.VIRTUAL);
				regCount = ((RegisterRangeInstruction) inst.getInstruction()).getRegCount();
				startReg = ((RegisterRangeInstruction) inst.getInstruction()).getStartRegister();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = startReg++;
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, startReg, cr));
				break;
			case INVOKE_STATIC_RANGE:
				mi = (MethodIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				mr = MethodReference.findOrCreate(ClassLoaderReference.Application, mi.getContainingClass().getTypeDescriptor(),
						mi.getMethodName().getStringValue(), "()V");
				cr = CallSiteReference.make(programCounter, mr, Dispatch.STATIC);
				regCount = ((RegisterRangeInstruction) inst.getInstruction()).getRegCount();
				startReg = ((RegisterRangeInstruction) inst.getInstruction()).getStartRegister();
				params = new int[regCount - 1];
				for (int i = 0; i < regCount - 1; i++) {
					params[i] = startReg++;
				}
				this.instructions.add(ssa.InvokeInstruction(inst.getInstructionIndex(), params, startReg, cr));
				break;
//			case INVOKE_SUPER_QUICK:
//			case INVOKE_VIRTUAL_QUICK:
//				break; //Quick
//			case INVOKE_SUPER_QUICK_RANGE:
//			case INVOKE_VIRTUAL_QUICK_RANGE:
//				break; //Quick
			case IPUT:
			case IPUT_BOOLEAN:
			case IPUT_BYTE:
			case IPUT_CHAR:
			case IPUT_OBJECT:
//			case IPUT_OBJECT_VOLATILE:
			case IPUT_SHORT:
//			case IPUT_VOLATILE:
			case IPUT_WIDE:
//			case IPUT_WIDE_VOLATILE:
				fi = (FieldIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trc = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getContainingClass().getTypeDescriptor());
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getFieldType().getTypeDescriptor());
				fr = FieldReference.findOrCreate(trc, Atom.findOrCreateAsciiAtom(fi.getFieldName().getStringValue()), trf);
				this.instructions.add(ssa.PutInstruction(inst.getInstructionIndex(), inst.getParameterRegister(1),
						inst.getParameterRegister(0), fr)); //Reihenfolge FieldREfe
				break;
//			case IPUT_OBJECT_QUICK:
//			case IPUT_QUICK:
//			case IPUT_WIDE_QUICK:
//				break;
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
			case MOVE_FROM16:
			case MOVE_OBJECT:
			case MOVE_OBJECT_16:
			case MOVE_OBJECT_FROM16:
			case MOVE_WIDE:
			case MOVE_WIDE_16:
			case MOVE_WIDE_FROM16:
				params = new int[1];
				params[0] = inst.getParameterRegister(1);
				if(params[0] == 0) {
					params[0]++;
				}
				this.instructions.add(ssa.PhiInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), params));
			case MOVE_RESULT:
			case MOVE_RESULT_OBJECT:
			case MOVE_RESULT_WIDE:
			case MOVE_EXCEPTION:
				params = new int[1];
				params[0] = 1;
				this.instructions.add(ssa.PhiInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), params));
				break;
			case MUL_DOUBLE:
			case MUL_FLOAT:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case MUL_DOUBLE_2ADDR:
			case MUL_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), false)); //Reihenfolge der Parameter
				break;
			case MUL_INT:
			case MUL_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case MUL_INT_2ADDR:
			case MUL_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case MUL_INT_LIT16:
			case MUL_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.MUL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
				break;
			case NEG_DOUBLE:
			case NEG_FLOAT:
			case NEG_INT:
			case NEG_LONG:
				this.instructions.add(ssa.UnaryOpInstruction(inst.getInstructionIndex(), IUnaryOpInstruction.Operator.NEG, inst.getParameterRegister(0), inst.getParameterRegister(1)));
				break;
			case NEW_ARRAY:
				int[] a = new int[1];
				a[0] = inst.getParameterRegister(1);
				ti = (TypeIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, ti.getTypeDescriptor());
				this.instructions.add(ssa.NewInstruction(inst.getInstructionIndex(), inst.getDestinationRegister(), NewSiteReference.make(programCounter, trf), a)); //Programm Counter?
				break;
			case NEW_INSTANCE:
				ti = (TypeIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, ti.getTypeDescriptor());
				this.instructions.add(ssa.NewInstruction(inst.getInstructionIndex(), inst.getDestinationRegister(), NewSiteReference.make(programCounter, trf))); //Programm Counter?
				break;
			case NOP:
				System.out.println("NOP"); //!!!
				break;
			case NOT_INT:
			case NOT_LONG:
				System.out.println("Was ist NOT"); //!!!
				break;
			case OR_INT:
			case OR_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.OR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case OR_INT_2ADDR:
			case OR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.OR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case OR_INT_LIT16:
			case OR_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.OR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
				break;
			case PACKED_SWITCH:
//				this.instructions.add(ssa.SwitchInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), inst.getParameterRegister(1),
//						inst.getParameterRegister(1)));
				System.out.println("Array Register!"); //Offset
				break;
			case REM_DOUBLE:
			case REM_FLOAT:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case REM_DOUBLE_2ADDR:
			case REM_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), false)); //Reihenfolge der Parameter
				break;
			case REM_INT:
			case REM_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case REM_INT_2ADDR:
			case REM_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case REM_INT_LIT16:
			case REM_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.REM, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
				break;
			case RETURN:
			case RETURN_OBJECT:
			case RETURN_WIDE:
				this.instructions.add(ssa.ReturnInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), false));
				break;
			case RETURN_VOID:
				this.instructions.add(ssa.ReturnInstruction(inst.getInstructionIndex()));
				break;
			case RSUB_INT:
			case RSUB_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
				break;
			case SGET:
			case SGET_BOOLEAN:
			case SGET_BYTE:
			case SGET_CHAR:
			case SGET_OBJECT:
			case SGET_SHORT:
			case SGET_WIDE:
//			case SGET_VOLATILE:
//			case SGET_WIDE_VOLATILE:
//			case SGET_OBJECT_VOLATILE:
				fi = (FieldIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trc = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getContainingClass().getTypeDescriptor());
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getFieldType().getTypeDescriptor());
				fr = FieldReference.findOrCreate(trc, Atom.findOrCreateAsciiAtom(fi.getFieldName().getStringValue()), trf);
				this.instructions.add(ssa.GetInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), fr)); //Fieldref
				break;
			case SHL_INT:
			case SHL_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true));
				break;
			case SHL_INT_2ADDR:
			case SHL_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true));
				break;
			case SHL_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHL, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true));
				break;
			case SHR_INT:
			case SHR_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true));
				break;
			case SHR_INT_2ADDR:
			case SHR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true));
				break;
			case SHR_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.SHR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true));
				break;
			case SPARSE_SWITCH:
				System.out.println("siehe PACKED_SWITCH"); //!!!
				break;
			case SPUT:
			case SPUT_BOOLEAN:
			case SPUT_BYTE:
			case SPUT_CHAR:
			case SPUT_OBJECT:
//			case SPUT_OBJECT_VOLATILE:
			case SPUT_SHORT:
//			case SPUT_VOLATILE:
			case SPUT_WIDE:
//			case SPUT_WIDE_VOLATILE:
				fi = (FieldIdItem) ((InstructionWithReference) inst.getInstruction()).getReferencedItem();
				trc = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getContainingClass().getTypeDescriptor());
				trf = TypeReference.findOrCreate(ClassLoaderReference.Application, fi.getFieldType().getTypeDescriptor());
				fr = FieldReference.findOrCreate(trc, Atom.findOrCreateAsciiAtom(fi.getFieldName().getStringValue()), trf);
				this.instructions.add(ssa.PutInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0), fr)); //Reihenfolge Fieldref
				break;
			case SUB_DOUBLE:
			case SUB_FLOAT:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), false)); //Reihenfolge der Parameter
				break;
			case SUB_DOUBLE_2ADDR:
			case SUB_FLOAT_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), false)); //Reihenfolge der Parameter
				break;
			case SUB_INT:
			case SUB_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;
			case SUB_INT_2ADDR:
			case SUB_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.SUB, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;
			case THROW:
				this.instructions.add(ssa.ThrowInstruction(inst.getInstructionIndex(), inst.getParameterRegister(0)));
				break;
			case USHR_INT:
			case USHR_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.USHR, false, true,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true));
				break;
			case USHR_INT_2ADDR:
			case USHR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.USHR, false, true,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true));
				break;
			case USHR_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IShiftInstruction.Operator.USHR, false, true,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true));
				break;
			case XOR_INT:
			case XOR_LONG:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.XOR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(2), true)); //Reihenfolge der Parameter
				break;	
			case XOR_INT_2ADDR:
			case XOR_LONG_2ADDR:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.XOR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), inst.getParameterRegister(0), true)); //Reihenfolge der Parameter
				break;	
			case XOR_INT_LIT16:
			case XOR_INT_LIT8:
				this.instructions.add(ssa.BinaryOpInstruction(inst.getInstructionIndex(), IBinaryOpInstruction.Operator.XOR, false, false,
						inst.getParameterRegister(0), inst.getParameterRegister(1), symbols.getConstant(((LiteralInstruction) inst.getInstruction()).getLiteral()), true)); //Reihenfolge der Parameter
				break;				
			default:
				System.out.println("ToDO " + inst.getInstruction().opcode);
				break;
			}
			
			programCounter++;
		}
		
		System.out.println(this.instructions);
	}

}
