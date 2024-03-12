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
		File myObj = new File(filePath); //"./yasTest.txt"
		//throw exception if file not  found
		if (!myObj.exists()) {
			throw new FileNotFoundException("Assembly file at " + filePath + " not found.");
		}
		Scanner myReader = new Scanner(myObj);

		// loop through each line
		while (myReader.hasNextLine()) {
		String data = myReader.nextLine();
		if(data.contains("#")) {data = data.substring(0,data.indexOf("#"));} // to make sure we can have comments
		String[] arr = data.split("\\s"); //split by any white space
		for ( String ss : arr) {
			String wee = ss.replaceAll("\\s",""); // replace all white space with nothing
			if(!wee.equals("")) { //make sure there are no extra white spaces
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
