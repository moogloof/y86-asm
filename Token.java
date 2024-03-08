import java.util.*;

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
	private StringBuilder compiled;
	private int label_pos;
	private String label_name;

	public Token(TokenType type, String[] words) {
		this.words = words;
		this.type = type;
		this.compiled = new StringBuilder();

		for (int i = 0; i < words.length; i++)
			words[i] = words[i].toLowerCase();

		label_pos = -1;
	}

	public static void initCompiler() {
		// Halt instruction
		instruction_table.put("halt", 0);
		operands_table.put("halt", new Operand[]{});

		// Nop instruction
		instruction_table.put("nop", 0x10);
		operands_table.put("nop", new Operand[]{});

		// Ret instruction
		instruction_table.put("ret", 0x90);
		operands_table.put("ret", new Operand[]{});
	}

	// Takes in current address and then returns how much to add to that address
	public long compile(long currentAddr) {
		switch (type) {
			case LABEL:
				label_table.put(words[0], currentAddr);
				return 0;
			case DIRECTIVE:
				if (words[0].equals(".pos")) {
					// TODO: Handle case where this may be negative, to error out
					return Long.decode(words[1]) - currentAddr;
				} else if (words[0].equals(".align")) {
					long alignValue = Long.decode(words[1]);
					long leftoverPart = currentAddr % alignValue;

					return (alignValue - leftoverPart) % alignValue;
				} else if (words[0].equals(".long")) {
					int fillValue = Integer.decode(words[1]);

					for (int i = 0; i < 4; i++) {
						compiled.append((char)(fillValue & 0xff));
						fillValue /= 0x100;
					}
					return 4;
				} else if (words[0].equals(".quad")) {
					long fillValue = Long.decode(words[1]);

					for (int i = 0; i < 8; i++) {
						compiled.append((char)(fillValue & 0xff));
						fillValue /= 0x100;
					}
					return 8;
				}
			case INSTRUCTION:
				String instruction_type = words[0];
				int instruction_bin = instruction_table.get(instruction_type);
				Operand[] operands_list = operands_table.get(instruction_type);
				int total_size = 0;
				int instruction_int = 0;

				compiled.append((char)instruction_bin);

				for (Operand op : operands_list) {
					total_size += op.bitSize;
					instruction_int <<= op.bitSize;
					instruction_int += (op.parse(words[op.instructionOrder]) << op.bitSize);

					if (op.label_flag) {
						label_name = op.label_name;
						label_pos = (total_size - op.bitSize) / 8;
					}
				}

				for (int i = 0; i < total_size / 8; i++) {
					compiled.append((char)(instruction_int & 0xff));
					instruction_int >>= 8;
				}

				return total_size / 8 + 1;
		}
		return 0;
	}

	public String link() {
		if (label_pos >= 0) {
			long label_value = label_table.get(label_name);

			for (int i = 0; i < 8; i++) {
				compiled.setCharAt(label_pos, (char)(label_value & 0xff));
				label_value >>= 8;
			}
		}

		return compiled.toString();
	}

	@Override
	public String toString() {
		return "Token{" +
				"type=" + type +
				", words=" + Arrays.toString(words) +
				'}';
	}
}
