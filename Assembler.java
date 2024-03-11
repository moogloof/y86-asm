import java.io.*;
import java.util.ArrayList;

public class Assembler {
    private String infile;
    private String outfile;

    public Assembler (String inflile) {
        this.infile = inflile;
    }

    public void assemble() {
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
                System.out.println(ss);
                System.out.println();
            }
            for(Token ss : listy) {
                ByteArrayOutputStream linked_stuff = ss.link();
                try {
                    linked_stuff.writeTo(outfile_stream);
                } catch (IOException e) {
                }
            }
            this.outfile = outfile.getAbsolutePath();
        } catch (FileNotFoundException e) {
        }
    }

    public String getOutfile() {
        return outfile;
    }
}
