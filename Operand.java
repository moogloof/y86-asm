import java.util.regex.Pattern;
import java.util.Hashtable;
import java.io.ByteArrayOutputStream;

abstract class Operand {
	public int bitSize;
	public int instructionOrder;
	public boolean label_flag;
	public int label_offset;
	public String label_name;
	protected static Hashtable<String, Integer> register_table = new Hashtable<>();

	public abstract ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException;

	public Operand() {
		label_flag = false;
		label_offset = 0;
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

/*
 * Custom exceptions for errors
 */
class IncorrectSyntaxException extends Exception {
	public IncorrectSyntaxException(String error_msg) {
		super(error_msg);
	}
}

class InvalidRegisterException extends Exception {
	public InvalidRegisterException(String error_msg) {
		super(error_msg);
	}
}

class BadImmediateException extends Exception {
	public BadImmediateException(String error_msg) {
		super(error_msg);
	}
}

/*
 * Operator implementations
 */
class FillAOperand extends Operand {
	public FillAOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream fill_output = new ByteArrayOutputStream();
		fill_output.write((byte)0xf0);
		return fill_output;
	}
}

class FillBOperand extends Operand {
	public FillBOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream fill_output = new ByteArrayOutputStream();
		fill_output.write((byte)0xf);
		return fill_output;
	}
}

class RegisterAOperand extends Operand {
	public RegisterAOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream reg_output = new ByteArrayOutputStream();
		int reg_num;

		// Guarantees format
		if (!Pattern.matches("%[a-z]{3},", word)) {
			throw new IncorrectSyntaxException("argument needs to be of the form \"%reg,\"");
		}

		try {
			reg_num = register_table.get(word.substring(1, 4).toLowerCase());
		} catch (NullPointerException e) {
			throw new InvalidRegisterException("invalid register");
		}

		reg_output.write((byte)reg_num << 4);

		return reg_output;
	}
}

class RegisterBOperand extends Operand {
	public RegisterBOperand(int instructionOrder) {
		bitSize = 4;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream reg_output = new ByteArrayOutputStream();
		int reg_num;

		// Guarantees format
		if (!Pattern.matches("%[a-zA-Z]{3}", word)) {
			throw new IncorrectSyntaxException("argument needs to be of the form \"%reg\"");
		}

		// Register name itself should be case insensitive
		try {
			reg_num = register_table.get(word.substring(1).toLowerCase());
		} catch (NullPointerException e) {
			throw new InvalidRegisterException("invalid register");
		}

		reg_output.write((byte)reg_num);

		return reg_output;
	}
}

class ImmediateOperand extends Operand {
	public ImmediateOperand(int instructionOrder) {
		bitSize = 64;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream imm_output = new ByteArrayOutputStream();
		long imm_num = 0;

		// Check if a label
		if (Pattern.matches("[a-zA-Z]+,", word)) {
			label_flag = true;
			label_name = word.substring(0, word.length() - 1);
		} else {
			// Guarantees format for immediate
			if (!Pattern.matches("\\$.+,", word)) {
				throw new IncorrectSyntaxException("argument needs to be of the form \"$imm,\"");
			}

			try {
				imm_num = Long.decode(word.substring(1, word.length() - 1));
			} catch (NumberFormatException e) {
				throw new BadImmediateException("the immediate number is in an invalid format");
			}
		}

		for (int i = 0; i < 8; i++) {
			imm_output.write((byte)(imm_num & 0xff));
			imm_num >>= 8;
		}

		return imm_output;
	}
}

class MemoryAOperand extends Operand {
	public MemoryAOperand(int instructionOrder) {
		bitSize = 68;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream mem_output = new ByteArrayOutputStream();
		int reg_num = 0;
		long imm_num = 0;

		// Check if label, reg
		// or imm, reg
		// or just reg
		if (Pattern.matches("[a-zA-Z]+\\(%[a-zA-Z]{3}\\),", word)) {
			label_flag = true;
			label_name = word.substring(0, word.indexOf("("));
			label_offset = 4;
		} else if (Pattern.matches(".*\\(%[a-zA-Z]{3}\\),", word)) {
			String imm_str = word.substring(0, word.indexOf("("));

			if (imm_str != null && !imm_str.equals("")) {
				try {
					imm_num = Long.decode(imm_str);
				} catch (NumberFormatException e) {
					throw new BadImmediateException("the immediate base is in an invalid format");
				}
			}
		} else {
			throw new IncorrectSyntaxException("argument needs to be of the form \"imm(%reg)\"");
		}

		// Check if valid register
		try {
			reg_num = register_table.get(word.substring(word.indexOf("%") + 1, word.indexOf(")")).toLowerCase());
		} catch (NullPointerException e) {
			throw new InvalidRegisterException("invalid register");
		}

		// Flush first byte first
		mem_output.write((byte)reg_num);

		// Flush rest of imm
		for (int i = 0; i < 8; i++) {
			mem_output.write((byte)(imm_num & 0xff));
			imm_num >>= 8;
		}

		return mem_output;
	}
}

class MemoryBOperand extends Operand {
	public MemoryBOperand(int instructionOrder) {
		bitSize = 68;
		this.instructionOrder = instructionOrder;
	}

	public ByteArrayOutputStream parse(String word) throws IncorrectSyntaxException, InvalidRegisterException, BadImmediateException {
		ByteArrayOutputStream mem_output = new ByteArrayOutputStream();
		int reg_num = 0;
		long imm_num = 0;

		// Check if label, reg
		// or imm, reg
		// or just reg
		if (Pattern.matches("[a-zA-Z]+\\(%[a-zA-Z]{3}\\)", word)) {
			label_flag = true;
			label_name = word.substring(0, word.indexOf("("));
			label_offset = 4;
		} else if (Pattern.matches(".*\\(%[a-zA-Z]{3}\\)", word)) {
			String imm_str = word.substring(0, word.indexOf("("));

			if (imm_str != null && !imm_str.equals("")) {
				try {
					imm_num = Long.decode(imm_str);
				} catch (NumberFormatException e) {
					throw new BadImmediateException("the immediate base is in an invalid format");
				}
			}
		} else {
			throw new IncorrectSyntaxException("argument needs to be of the form \"imm(%reg)\"");
		}

		try {
			reg_num = register_table.get(word.substring(word.indexOf("%") + 1, word.indexOf(")")).toLowerCase());
		} catch (NullPointerException e) {
			throw new InvalidRegisterException("invalid register");
		}

		// Flush first byte first
		mem_output.write((byte)reg_num);

		// Flush rest of imm
		for (int i = 0; i < 8; i++) {
			mem_output.write((byte)(imm_num & 0xff));
			imm_num >>= 8;
		}

		return mem_output;
	}
}
