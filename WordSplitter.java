import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files
public class WordSplitter {
    private String filePath;

    public WordSplitter(String fileName) {
        filePath = fileName;
    }

    public ArrayList<String> splitWords() throws FileNotFoundException {
        ArrayList<String> wordsList = new ArrayList<>();
        File myObj = new File(filePath); //"./yasTest.txt" #comments
	if (!myObj.exists()) {
		throw new FileNotFoundException("Assembly file at " + filePath + " not found.");
	}
        Scanner myReader = new Scanner(myObj);

        while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if(data.contains("#")) {data = data.substring(0,data.indexOf("#"));}
        String[] arr = data.split("\s");
        for ( String ss : arr) {
            String wee = ss.replaceAll("\\s","");
            if(!wee.equals("")) {
                wordsList.add(wee);
            }
        }
        }
        myReader.close();
	return wordsList;
    }

    public static void main(String[] args) {
    }
}
