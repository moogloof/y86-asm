import java.util.regex.Pattern;
import java.util.Hashtable;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;

// Exception to handle invalid directives
class InvalidDirectiveException extends Exception {
	public InvalidDirectiveException(String error_msg) {
		super(error_msg);
	}
}
// Bad label exception
class BadLabelException extends Exception {
	public BadLabelException(String error_msg) {
		super(error_msg);
	}
}
// Undefined instruction exception
class UndefinedInstructionException extends Exception {
	public UndefinedInstructionException(String error_msg) {
		super(error_msg);
	}
}
// Undefined label exception
class UndefinedLabelException extends Exception {
	public UndefinedLabelException(String error_msg) {
		super(error_msg);
	}
}

class Token {
	enum TokenType {
		LABEL,
		DIRECTIVE,
		INSTRUCTION
	}

	private static Hashtable<String, Long> label_table = new Hashtable<>();
	private static Hashtable<String, Operand[]> operands_table = new Hashtable<>();
	private static Hashtable<String, Integer> instruction_table = new Hashtable<>();

	private TokenType type;
	private String[] words;
	private ByteArrayOutputStream compiled;
	private int label_pos;
	private String label_name;

	public Token(TokenType type, String[] words) {
		this.words = words;
		this.type = type;
		this.compiled = new ByteArrayOutputStream();

		label_pos = -1;
	}

	public static void initCompiler() {
		// Halt instruction
		instruction_table.put("halt", 0);
		operands_table.put("halt", new Operand[]{});

		// Nop instruction
		instruction_table.put("nop", 0x10);
		operands_table.put("nop", new Operand[]{});

		// Rrmovl instruction
		instruction_table.put("rrmovl", 0x20);
		operands_table.put("rrmovl", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmovle instruction
		instruction_table.put("cmovle", 0x21);
		operands_table.put("cmovle", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmovl instruction
		instruction_table.put("cmovl", 0x22);
		operands_table.put("cmovl", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmove instruction
		instruction_table.put("cmove", 0x23);
		operands_table.put("cmove", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmovne instruction
		instruction_table.put("cmovne", 0x24);
		operands_table.put("cmovne", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmovge instruction
		instruction_table.put("cmovge", 0x25);
		operands_table.put("cmovge", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Cmovg instruction
		instruction_table.put("cmovg", 0x26);
		operands_table.put("cmovg", new Operand[]{new RegisterAOperand(1), new RegisterBOperand(2)});

		// Ret instruction
		instruction_table.put("ret", 0x90);
		operands_table.put("ret", new Operand[]{});
	}

	// Takes in current address and then returns how much to add to that address
	public long compile(long currentAddr) throws IncorrectSyntaxException, BadImmediateException, InvalidRegisterException, InvalidDirectiveException, BadLabelException, UndefinedInstructionException, ArrayIndexOutOfBoundsException {
		switch (type) {
			case LABEL:
				if (!Pattern.matches("[a-zA-Z0-9_]+:", words[0])) {
					throw new BadLabelException("label name should be alphanumeric or _");
				}
				label_table.put(words[0], currentAddr);
				return 0;
			case DIRECTIVE:
				if (words[0].equals(".pos")) {
					long fill_len = 0;

					try {
						fill_len = Long.decode(words[1]) - currentAddr;
					} catch (NumberFormatException e) {
						throw new InvalidDirectiveException("must have valid address number for position");
					}

					for (int i = 0; i < fill_len; i++) {
						compiled.write((byte)0);
					}

					return fill_len;
				} else if (words[0].equals(".align")) {
					long alignValue;
					long fill_len;

					try {
						alignValue = Long.decode(words[1]);
					} catch (NumberFormatException e) {
						throw new InvalidDirectiveException("must have valid align number");
					}

					fill_len = currentAddr % alignValue;
					fill_len = (alignValue - fill_len) % alignValue;

					for (int i = 0; i < fill_len; i++) {
						compiled.write((byte)0);
					}

					return fill_len;
				} else if (words[0].equals(".long")) {
					int fillValue;

					try {
						fillValue = Integer.decode(words[1]);
					} catch (NumberFormatException e) {
						throw new InvalidDirectiveException("must have valid 32 bit number");
					}

					for (int i = 0; i < 4; i++) {
						compiled.write((byte)(fillValue & 0xff));
						fillValue >>= 8;
					}
					return 4;
				} else if (words[0].equals(".quad")) {
					long fillValue;

					try {
						fillValue = Long.decode(words[1]);
					} catch (NumberFormatException e) {
						throw new InvalidDirectiveException("must have valid 64 bit number");
					}

					for (int i = 0; i < 8; i++) {
						compiled.write((byte)(fillValue & 0xff));
						fillValue >>= 8;
					}
					return 8;
				}
			case INSTRUCTION:
				String instruction_type = words[0];
				int instruction_bin;

				try {
					instruction_bin = instruction_table.get(instruction_type);
				} catch (NullPointerException e) {
					throw new UndefinedInstructionException("no instruction of the name \"" + instruction_type + "\"");
				}

				Operand[] operands_list = operands_table.get(instruction_type);
				int total_size = 8;

				// Instruction code comes first
				compiled.write((byte)instruction_bin);

				// Patch together the byte streams coming from operands
				byte last_instruction_buffer = 0;

				for (Operand op : operands_list) {
					byte[] op_result = op.parse(words[op.instructionOrder]).toByteArray();
					int op_result_i = 0;

					if (op.label_flag) {
						label_name = op.label_name;
						label_pos = (total_size + op.label_offset) / 8;
					}

					if (total_size % 8 == 0) {
						for (int i = 0; i < op_result.length - 1; i++) {
							compiled.write(op_result[i]);
						}

						if (op.bitSize % 8 == 0) {
							compiled.write(op_result[op_result.length - 1]);
						} else {
							last_instruction_buffer = op_result[op_result.length - 1];
						}
					} else {
						compiled.write(last_instruction_buffer + op_result[0]);

						for (int i = 1; i < op_result.length; i++) {
							compiled.write(op_result[i]);
						}

						// Not necessary, but makes it easier to debug for future devs
						// AKA when you add some instructions outside of the standard ones with weird alignment
						last_instruction_buffer = 0;
					}

					total_size += op.bitSize;
				}

				return total_size / 8;
		}
		return 0;
	}

	public ByteArrayOutputStream link() throws UndefinedLabelException {
		byte[] compiled_arr = compiled.toByteArray();

		if (label_pos >= 0) {
			long label_value;

			try {
				label_value = label_table.get(label_name);
			} catch (NullPointerException e) {
				throw new UndefinedLabelException("label with name \"" + label_name + "\" is undefined");
			}

			for (int i = 0; i < 8; i++) {
				compiled_arr[label_pos + i] = (byte)(label_value & 0xff);
				label_value >>= 8;
			}
		}

		ByteArrayOutputStream bin_compiled = new ByteArrayOutputStream();
		bin_compiled.write(compiled_arr, 0, compiled_arr.length);

		return bin_compiled;
	}

	@Override
	public String toString() {
		return "Token{" +
				"type=" + type +
				", words=" + Arrays.toString(words) +
				'}';
	}
}
