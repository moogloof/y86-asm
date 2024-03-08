import java.util.*;

class Token {
	enum TokenType {
		LABEL,
		DIRECTIVE,
		INSTRUCTION
	}

	private static Hashtable<String, Integer> label_table = new Hashtable<>();
	private static Hashtable<String, Operand[]> operands_table = new Hashtable<>();
	private static Hashtable<String, Integer> instruction_table = new Hashtable<>();

	private TokenType type;
	private String[] words;
	private StringBuilder compiled;

	public Token(TokenType type, String[] words) {
		this.words = words;
		this.type = type;
		this.compiled = new StringBuilder();

		for (int i = 0; i < words.length; i++)
			words[i] = words[i].toLowerCase();
	}

	public static void initCompiler() {
	}

	// Takes in current address and then returns how much to add to that address
	public int compile(int currentAddr) {
		switch (type) {
			case LABEL:
				label_table.put(words[0], currentAddr);
				return 0;
			case DIRECTIVE:
				if (words[0].equals(".pos")) {
					// TODO: Handle case where this may be negative, to error out
					return Integer.decode(words[1]) - currentAddr;
				} else if (words[0].equals(".align")) {
					int alignValue = Integer.decode(words[1]);
					int leftoverPart = currentAddr % alignValue;

					return (alignValue - leftoverPart) % alignValue;
				} else if (words[0].equals(".long")) {
					int fillValue = Integer.decode(words[1]);

					for (int i = 0; i < 4; i++) {
						compiled.append((char)(fillValue & 0xff));
						fillValue /= 0x100;
					}
					return 4;
				} else if (words[0].equals(".quad")) {
					int fillValue = Integer.decode(words[1]);

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
					instruction_int += (op.parse(word[op.instructionOrder]) << op.bitSize);
				}

				for (int i = 0; i < total_size / 8; i++) {
					compiled.append((char)(instruction_int & 0xff));
					instruction_int >>= 8;
				}

				return 0;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "Token{" +
				"type=" + type +
				", words=" + Arrays.toString(words) +
				'}';
	}
}
