import java.util.ArrayList;
import java.math.BigInteger;
import java.io.*;

public class WordTokenizer {

    private ArrayList<Token> listofTokens = new ArrayList<>();

    public WordTokenizer() {

    }

    public void createTokens(ArrayList<String> wordsList) {
        int n = wordsList.size();
        int i = 0;
        while (i < n) {
            String currentWord = wordsList.get(i);
            if(currentWord.indexOf(".")==0) {
                String[] words = {currentWord,wordsList.get(i+1)};
                Token directive = new Token(Token.TokenType.DIRECTIVE, words);
                listofTokens.add(directive);
                i = i+2;
            } else if (currentWord.contains(":")) {
                String[] words = {currentWord};
                Token label = new Token(Token.TokenType.LABEL, words);
                listofTokens.add(label);
                i++;
            } else {
                if (currentWord.equals("halt") || currentWord.equals("nop") || currentWord.equals("ret")) {
                    String[] words = {currentWord};
                    Token instruction = new Token(Token.TokenType.INSTRUCTION, words);
                    listofTokens.add(instruction);
                    i++;
                } else if (currentWord.indexOf("j") == 0 || currentWord.indexOf("call") == 0 ||
                        currentWord.indexOf("push") == 0 || currentWord.indexOf("pop") == 0) {
                    String[] words = {currentWord,wordsList.get(i+1)};
                    Token instruction = new Token(Token.TokenType.INSTRUCTION, words);
                    listofTokens.add(instruction);
                    i = i+2;
                } else {
                    String[] words = {currentWord,wordsList.get(i+1), wordsList.get(i+2)};
                    Token instruction = new Token(Token.TokenType.INSTRUCTION, words);
                    listofTokens.add(instruction);
                    i = i+3;
                }

            }

        }
    }

    public ArrayList<Token> getListofTokens() {
        return listofTokens;
    }

    public static void main(String[] args) {
        WordSplitter blob = new WordSplitter("./yasTest.txt");
	try {
		File outfile = new File("out.bin");
		FileOutputStream outfile_stream = new FileOutputStream(outfile);
		ArrayList<String> x = blob.splitWords();
		long pos = 0;
		Token.initCompiler();
		Operand.initOperands();

		WordTokenizer tokenizer = new WordTokenizer();
		tokenizer.createTokens(x);
		ArrayList<Token> listy = tokenizer.getListofTokens();
		for(Token ss : listy) {
		    try {
		    	pos += ss.compile(pos);
		    } catch (Exception e) {
			    System.out.println(e.getMessage());
			    return;
		    }
		}
		for(Token ss : listy) {
		    System.out.println(ss);
		    System.out.println();
		}
		for(Token ss : listy) {
		    ByteArrayOutputStream linked_stuff;
		    try {
		    	linked_stuff = ss.link();
		    } catch (Exception e) {
			    System.out.println(e.getMessage());
			    return;
		    }
		    try {
			linked_stuff.writeTo(outfile_stream);
		    } catch (IOException e) {
		    }
		}
	} catch (FileNotFoundException e) {
	}
    }


}
