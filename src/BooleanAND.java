import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BooleanAND {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		int numArgs;
		String retrievePath;
		String queriesFileName;
		String writePath;
		
		numArgs = args.length;
		
		if (numArgs < 3) { // Ensure necessary arguments are provided
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 3, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		retrievePath = args[0];
		queriesFileName = args[1];
		writePath = args[2];
		
		boolean stemming = false;
		
		if (numArgs > 3) {
			if (args[3].equalsIgnoreCase("Stem")) {
				stemming = true;
			} else {
				System.out.println("6th Argument ineffective");
				System.out.println("For stemming please use argument 'Stem'");
				System.out.println("You provided Argument: " + args[5]);
				System.out.println("Continuing without stemming");
			}
		}
		
		// perform search with provided values
		searchDoc(retrievePath, queriesFileName, writePath, stemming);
	}
	
	public static void searchDoc(String retrievePath, String queriesFileName, String writePath, boolean stemming) 
			throws FileNotFoundException, IOException {
		
		// If the path does not exist to retrieve a file from
		if (!IndexEngine.pathExists(retrievePath)) {
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + retrievePath);
			System.exit(0);
		}
		
		// if the file to write to already exists
		if (IndexEngine.pathExists(writePath)) {
			System.out.println("Error: File to Write to already exists");
			System.out.println("Please provide a file name that does not already exist");
			System.out.println("File provided: " + writePath);
			System.exit(0);
		}
		
		String neededDirs = ""; // directories to be made
		String[] directories = writePath.split("\\\\"); //split up by slashes for directories
		
		for (int i = 0; i < directories.length - 2; i ++) {
			neededDirs += directories[i]+"\\"; // create path for files
		}
		neededDirs+=directories[directories.length - 2]; // add last path
		
		new File(neededDirs).mkdirs(); //create directories for file
		
		FileWriter searchOutput = new FileWriter(writePath); // new file to write to
		
		// Initial objects for storing data
		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
		HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		// Read lexicon and inverted index
		InvertedMethods.readLexicon(retrievePath, lexicon, reverseLexicon);
		InvertedMethods.readInvertedIndex(retrievePath, invertedIndex);
		
		File searchFile = null;
		BufferedReader reader = null;
		
		try { // try to read in search file
			searchFile = new File(queriesFileName);
			reader = new BufferedReader(new FileReader(searchFile));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Files not found in location provided");
			System.out.println("Please provide location where files were written");
			System.out.println("You provided: " + retrievePath);
			System.exit(0);
		}
		
		while (true) { // until the file runs out
			String line = reader.readLine();
			
			if (line == null) {
				break; // if file is out of lines
			}
			
			// read the topic id, and the query string
			int topicID = Integer.parseInt(line.strip());
			line = reader.readLine();
			
			// tokenize the query string and convert to tokenIDs
			String[] tokens = InvertedMethods.tokenize(line, stemming);
			int[] tokenIDs = InvertedMethods.getSearchTokenIDs(tokens, lexicon);
			
			// Get search result for token ids from query
			String searchResult = BooleanANDSearch(tokenIDs, invertedIndex, topicID, retrievePath);
			
			// Write search result to file
			searchOutput.write(searchResult);
		}
		
		searchOutput.close(); //close file written to
		
	}
	
	public static String BooleanANDSearch(int[] tokenIDs, ArrayList<ArrayList<Integer>> invertedIndex, int topicID, String retrievePath) 
			throws FileNotFoundException, IOException {
		// return string result of search given index, topicid, and path to retrieve
		
		String result = "";
		
		// Arraylist of arraylist for matches for each token
		ArrayList<ArrayList<Integer>> allMatches = new ArrayList<ArrayList<Integer>>();
		
		// for each token id that exists, retrieve postings list
		for (int searchID: tokenIDs) {
			if (searchID >= 0) {
				allMatches.add(invertedIndex.get(searchID));
			}
		}
		
		// filter results with BooleanAND algorithm
		ArrayList<Integer> finalResults = BooleanANDMatches(allMatches);
		
		// initialize rank as 1, and score as size of results + 1
		int rank = 1;
		int initialScore = finalResults.size() + 1;
		
		// for each result
		for (int id: finalResults) {
			int score = initialScore - rank; // calculate score
			// retrieve docno as string
			String docno = GetFile.getDocNo(Integer.toString(id), retrievePath);
			// string result for query result in TREC format
			result += topicID + " Q0 " + docno + " " + rank + " " + score + " cwibachAND\n";
			rank++; //increase rank
		}
		
		return result;
	}
	
	public static ArrayList<Integer> BooleanANDMatches(ArrayList<ArrayList<Integer>> allMatches) {
		// return arraylist of all docids that appear in all provided posting lists
		
		// https://stackoverflow.com/questions/16184653/sorting-arraylist-of-arrayliststring-in-java
		Collections.sort(allMatches, new Comparator<ArrayList<Integer>> () {
			@Override
			public int compare(ArrayList<Integer> a, ArrayList<Integer> b) {
				return a.size() - b.size();
			}
		}); //sort arraylists by length (shortest to longest)
		
		// initialize arraylist of results
		ArrayList<Integer> finalResult = new ArrayList<Integer>();
		
		if (allMatches.size() == 0) { //if there are no documents at all
			return finalResult;
		}
		
		// extract docids from the first posting list
		for (int i = 0; i < allMatches.get(0).size(); i+=2) {
			finalResult.add(allMatches.get(0).get(i));
		}
		
		// combine first & second, then result & third, result & fourth and so on through all postinglists
		for (int i = 0; i < allMatches.size() - 1; i++) {
			finalResult = MergeLists(finalResult, allMatches.get(i + 1));
		}
		
		return finalResult;
	}
	
	public static ArrayList<Integer> MergeLists(ArrayList<Integer> list1, ArrayList<Integer> list2) {
		// return arraylist of docids that appear in arraylist of docids, and postinglist arraylist
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		int index1 = 0; //set initial index for first list
		int index2 = 0; //set initial index for second list
		
		// while both indexes within lists
		while ((index1 < list1.size()) && (index2 < list2.size())) {
			int value1 = list1.get(index1); //get first value
			int value2 = list2.get(index2); //get second value
			
			if (value1 < value2) { // if 2nd is greater
				index1 += 1; // increase index 1
			} else if (value2 < value1) { //if 1st is greaeter
				index2 += 2; // increase index 2
			} else { // value1 == value2
				result.add(value1); // add to results
				index1 += 1; //increase both indexes
				index2 += 2;
			}
		}
		
		return result;
	}
}