import java.util.Arrays;

class Token {
	enum TokenType {
		LABEL,
		DIRECTIVE,
		INSTRUCTION
	}

	private TokenType type;
	private String[] words;

	public Token(TokenType type, String[] words) {
		this.words = words;
		this.type = type;
	}

	// Takes in current address and then returns how much to add to that address
	int compile(int currentAddr) {
		switch (type) {
			case LABEL:
				return 0;
			case DIRECTIVE:
				break;
			case INSTRUCTION:
				break;
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
