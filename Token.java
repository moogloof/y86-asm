import java.util.Arrays;

class Token {
	enum TokenType {
		LABEL,
		DIRECTIVE,
		INSTRUCTION
	}

	private static Hashtable<String, Integer> register_table = new Hashtable<>();
	private static Hashtable<String, Integer> label_table = new Hashtable<>();

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
		register_table.put("eax", 0);
		register_table.put("ecx", 1);
		register_table.put("edx", 2);
		register_table.put("ebx", 3);
		register_table.put("esi", 6);
		register_table.put("edi", 7);
		register_table.put("esp", 4);
		register_table.put("ebp", 5);
	}

	// Takes in current address and then returns how much to add to that address
	int compile(int currentAddr) {
		switch (type) {
			case LABEL:
				label_table.put(words[0], currentAddr);
				return 0;
				break;
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
				break;
			case INSTRUCTION:
				// TODO: make this more efficient, but it may work for now
				if (words[0].equals("halt")) {
					compiled.append('\0');
					return 1;
				} else if (words[0].equals("nop")) {
					compiled.append((char)0x10);
					return 1;
				} else if (words[0].equals("rrmovl")) {
					int rA = register_table.get(words[1].substring(1, words[1].length() - 1));
					int rB = register_table.get(words[2].substring(1));

					// TODO: error out for incorrect syntax
					compiled.append((char)0x20);
					compiled.append((char)((rA << 4) + rB));
					return 2;
				} else if (words[0].equals("irmovl")) {
					int fillValue = Integer.decode(words[1].substring(1, words[1].length() - 1));
					int rB = register_table.get(words[2].substring(1));

					compiled.append((char)0x30);
					compiled.append((char)(0xf0 + rB));
					for (int i = 0; i < 8; i++) {
						compiled.append((char)(fillValue & 0xff));
						fillValue /= 0x100;
					}
				}
				// TODO: Error out here
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
