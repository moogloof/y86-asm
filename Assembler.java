import java.io.*;
import java.util.ArrayList;

public class Assembler {
    private String infile;
    private String outfile;

    public Assembler (String inflile) {
        this.infile = inflile;
    }

    public boolean assemble() {
        WordSplitter blob = new WordSplitter(infile);
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
                pos += ss.compile(pos);
            }
            for(Token ss : listy) {
                ByteArrayOutputStream linked_stuff = ss.link();
                try {
                    linked_stuff.writeTo(outfile_stream);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            this.outfile = outfile.getAbsolutePath();
	    return true;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IncorrectSyntaxException e) {
            System.out.println(e.getMessage());
        } catch (BadImmediateException e) {
            System.out.println(e.getMessage());
        } catch (InvalidRegisterException e) {
            System.out.println(e.getMessage());
        } catch (InvalidDirectiveException e) {
            System.out.println(e.getMessage());
        } catch (BadLabelException e) {
            System.out.println(e.getMessage());
        } catch (UndefinedInstructionException e) {
            System.out.println(e.getMessage());
        } catch (UndefinedLabelException e) {
            System.out.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }

	return false;
    }

    public String getOutfile() {
        return outfile;
    }
}
