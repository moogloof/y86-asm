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
	}
}
