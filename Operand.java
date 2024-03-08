import java.util.*;

abstract class Operand {
	public int bitSize;
	public int instructionOrder;
	public boolean label_flag;
	public String label_name;
	protected static Hashtable<String, Integer> register_table = new Hashtable<>();

	public abstract long parse(String word);

	public Operand() {
		label_flag = false;
	}

	public static void initOperands() {
		register_table.put("eax", 0);
		register_table.put("ecx", 1);
		register_table.put("edx", 2);
		register_table.put("ebx", 3);
		register_table.put("esi", 6);
		register_table.put("edi", 7);
		register_table.put("esp", 4);
		register_table.put("ebp", 5);
	}
}

class FillOperand extends Operand {
	public FillOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public long parse(String word) {
		return 0xf;
	}
}

class RegisterAOperand extends Operand {
	public RegisterAOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public long parse(String word) {
		return register_table.get(word.substring(1, word.indexOf(",")));
	}
}

class RegisterBOperand extends Operand {
	public RegisterBOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public long parse(String word) {
		return register_table.get(word.substring(1));
	}
}

class ImmediateOperand extends Operand {
	public ImmediateOperand(int instructionOrder) {
		bitSize = 64;
		this.instructionOrder = instructionOrder;
	}

	public long parse(String word) {
		String filteredWord = word.replace(",", "");
		try {
			return Long.decode(filteredWord);
		} catch (NumberFormatException e) {
			label_flag = true;
			label_name = filteredWord;
			return 0;
		}
	}
}

class MemoryOperand extends Operand {
	public MemoryOperand(int instructionOrder) {
		bitSize = 68;
		this.instructionOrder = instructionOrder;
	}

	public long parse(String word) {
		String baseLiteral = word.substring(0, word.indexOf('('));
		String registerLiteral = word.substring(word.indexOf('(') + 1, word.indexOf(')'));
		long val = (baseLiteral.equals("")) ? 0 : Long.decode(baseLiteral);
		val = (registerLiteral.equals("")) ? val : (val + register_table.get(registerLiteral.substring(1)));
		return val;
	}
}
