import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InvertedMethods{
	public static void main(String args[]) throws IOException, FileNotFoundException {
		String retrievePath = "C:\\Users\\carte\\Documents\\Search Engines\\test1";
		
//		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
//		HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
//		
//		readLexicon(retrievePath, lexicon, reverseLexicon);
//		
//		for (Integer key: reverseLexicon.keySet()) {
//			String value = reverseLexicon.get(key);
//			Integer key2 = lexicon.get(value);
//			System.out.println(key + ": " + value + ": " + key2);
//		}
		
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		readInvertedIndex(retrievePath, invertedIndex);
		
		for (int i = 0; i < 100; i++) {
			ArrayList<Integer> tempArray = invertedIndex.get(i);
			
			System.out.println(i);
			for (int j=0; j<tempArray.size(); j++) {
				System.out.print(tempArray.get(j) + ",");
			}
			System.out.print("\n");
		}
	}
	
	public static String getDocString(String headline, String graphic, String text) {
		// convert headline, graphic and text into one tokenizable string
		
		// String that will become tokens
		String tokenizeString = "";
		
		if (!headline.equals("No headline for this document")) {
			tokenizeString = headline;
		} // if headline exists add to string
		
		if (!text.equals("No text for this document")) {
			tokenizeString = tokenizeString + " " + text;
		} // if text exists, add to string
		
		if (!graphic.equals("No graphic for this document")) {
			tokenizeString = tokenizeString + " " + graphic;
		} // if graphic exists, add to string
		
		// remove paragraph tags
		tokenizeString = tokenizeString.replaceAll("<P>", "");
		tokenizeString = tokenizeString.replaceAll("</P>", "");
		
		return tokenizeString;
	}
	
	public static String[] tokenize(String fullText, boolean stemming) {
		// get all tokens from a string in array
		
		ArrayList<String> tokenList= new ArrayList<String>();
		
		// convert text to lowercase
		fullText = fullText.toLowerCase();
		int start = 0;
		int i;
		
		// go through all text
		for (i = 0; i < fullText.length(); i++) {
			char c = fullText.charAt(i); // get character
			
			// if character is not alphanumeric
			if (!Character.isLetterOrDigit(c)) {
				// if this is not the start of section
				if (start != i) {
					// get substring and add to list
					String token = fullText.substring(start, i);
					if (stemming) { // stem token if stemming enabled
						token = PorterStemmer.stem(token);
					}
					if (!token.equals("")) { //ensure not blank
						tokenList.add(token);
					}
				}
				// move start to next character
				start = i + 1;
			}
		}
		
		// add the final token if there is one extra at the end
		if (start != i) {
			String token = fullText.substring(start, i);
			if (stemming) { //stem token if stemming enabled
				token = PorterStemmer.stem(token);
			}
			if (!token.equals("")) { // ensure not blank
				tokenList.add(token);
			}
		}
		
		//convert arraylist to array
		String[] tokens = new String[tokenList.size()]; 
		tokens = tokenList.toArray(tokens);
		
		return tokens;
	}

	public static int[] TokentoIDs(String[] tokens, HashMap<String, Integer> lexicon, ArrayList<ArrayList<Integer>> invertedIndex) {
		// get list of ids from list of tokens
		
		int[] tokenIDs = new int[1]; //start with new array
		
		int index = 0;
		
		// for each token
		for (String token: tokens) {
			if (index == tokenIDs.length) {
				// ensure it is long enough to hold new item
				tokenIDs = Arrays.copyOf(tokenIDs, index*2);
			}
			
			// if it exists in lexicon, add token id
			if (lexicon.containsKey(token)) {
				tokenIDs[index] = lexicon.get(token);
			} else {
				// get new id, add to lexicon & inverted index
				int newID = lexicon.keySet().size();
				lexicon.put(token, newID);
				tokenIDs[index] = newID;
				invertedIndex.add(new ArrayList<Integer>());
			}
			// next token
			index++;
		}
		
		// shorten array
		tokenIDs = Arrays.copyOf(tokenIDs, index);
		
		return tokenIDs;
	}
	
	public static int[] getSearchTokenIDs(String[] tokens, HashMap<String, Integer> lexicon) {
		// get token ids for search query, does not add missing to lexicon
		
		int[] tokenIDs = new int[tokens.length];
		int index = 0;
		
		for (String token: tokens) {
			
			if (lexicon.containsKey(token)) {
				tokenIDs[index] = lexicon.get(token);
			} else {
				tokenIDs[index] = -1;
			}
			
			index++;
		}
		
		return tokenIDs;
	}
	
	public static HashMap<Integer, Integer> CountWords(int[] tokenIDs){
		// count word occurances in a list of tokenIDs
		
		HashMap<Integer, Integer> wordCounts = new HashMap<Integer, Integer>();
		
		// for each id from the list
		for (int tokenID: tokenIDs) {
			// if its already in, increment, otherwise set to 1
			if (wordCounts.containsKey(tokenID)) {
				wordCounts.put(tokenID, wordCounts.get(tokenID) + 1);
			} else {
				wordCounts.put(tokenID, 1);
			}
		}
		
		return wordCounts;
	}
	
	public static void AddtoPostings(HashMap<Integer, Integer> wordCounts, int docID, ArrayList<ArrayList<Integer>> invertedIndex) {
		// update inverted index with word counts
		
		for (int termId: wordCounts.keySet()) {
			int count = wordCounts.get(termId);
			// for each key set, get the count
			
			// get existing arraylist, add docid and count to end of it
			ArrayList<Integer> tempArray = invertedIndex.get(termId);
			tempArray.add(docID);
			tempArray.add(count);
			invertedIndex.set(termId, tempArray);
		}
	}
	
	public static void readLexicon(String retrievePath, HashMap<String, Integer> lexicon, HashMap<Integer, String> reverseLexicon) 
			throws IOException, FileNotFoundException{
		// read lexicon from file
		
		// get file for lexicon
		String lexiconPath = retrievePath + "\\lexicondoc.txt";
		File lexiconFile = null;
		BufferedReader reader = null;
		
		// try to read file
		try {
			lexiconFile = new File(lexiconPath);
			reader = new BufferedReader(new FileReader(lexiconFile));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Files not found in location provided");
			System.out.println("Please provide location where files were written");
			System.out.println("You provided: " + retrievePath);
			System.exit(0);
		}
		
		// clear lexicon and reverse lexicon for clean read
		lexicon.clear();
		reverseLexicon.clear();
		
		int id = 0;
		
		// read in each word, add to lexicon and reverse in order
		while (true) {
			String term = reader.readLine();
			
			if (term == null) {
				break;
			}
			
			lexicon.put(term, id);
			reverseLexicon.put(id, term);
			
			id++;
		}
		
		reader.close();
	}
	
	public static void readInvertedIndex(String retrievePath, ArrayList<ArrayList<Integer>> invertedIndex) 
			throws IOException, FileNotFoundException{
		// read invertedindex in from file
		
		String invIndexPath = retrievePath + "\\invertedindex.txt";
		File invIndexFile = null;
		BufferedReader reader = null;
		
		// try to read the file from path provided
		try {
			invIndexFile = new File(invIndexPath);
			reader = new BufferedReader(new FileReader(invIndexFile));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Files not found in location provided");
			System.out.println("Please provide location where files were written");
			System.out.println("You provided: " + retrievePath);
			System.exit(0);
		}
		
		// clear existing inverted index
		invertedIndex.clear();
		
		// read through the lines
		while (true) {
			String line = reader.readLine();
			
			if (line == null) {
				break;
			}
			
			// get count of items for this postings list
			int count = Integer.parseInt(line);
			
			ArrayList<Integer> tempArray = new ArrayList<Integer>(count);
			
			// for the next count of lines, add to arraylist
			for (int i = 0; i < count; i++) {
				int value = Integer.parseInt(reader.readLine());
				tempArray.add(value);
			}
			
			invertedIndex.add(tempArray);
		}
		
		reader.close();
		
	}

	public static ArrayList<String> readDocnosList(String retrievePath) throws IOException{
		/*	Create a list of all docnos in order given the path
		 *  Return ArrayList of all Docnos to retrieve docnos from ids
		 */
		ArrayList<String> allDocnos = new ArrayList<String>();
		
		// get file for docnos
		String docnoPath = retrievePath + "\\alldocnos.txt";
		File docnoFile = null;
		BufferedReader reader = null;
		
		try { // ensure file exists
			docnoFile = new File(docnoPath);
			reader = new BufferedReader(new FileReader(docnoFile));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Files not found in location provided");
			System.out.println("Please provide location where files were written");
			System.out.println("You provided: " + retrievePath);
			System.exit(0);
		}
		
		while (true) {
			String line = reader.readLine();
			
			if (line == null) {
				break;
			}
			
			allDocnos.add(line);
		}
		
		reader.close();
		
		return allDocnos;
	}
	
	public static ArrayList<Integer> readDocLengths(String retrievePath) throws IOException {
		/*	Create a list of all docnos in order given the path
		 *  Return ArrayList of all Docnos to retrieve docnos from ids
		 */
		ArrayList<Integer> allDocLengths = new ArrayList<Integer>();
		
		// get file for docnos
		String docnoPath = retrievePath + "\\alldoclengths.txt";
		File docnoFile = null;
		BufferedReader reader = null;
		
		try { // ensure file exists
			docnoFile = new File(docnoPath);
			reader = new BufferedReader(new FileReader(docnoFile));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Files not found in location provided");
			System.out.println("Please provide location where files were written");
			System.out.println("You provided: " + retrievePath);
			System.exit(0);
		}
		
		while (true) {
			String line = reader.readLine();
			
			if (line == null) {
				break;
			}
			
			allDocLengths.add(Integer.parseInt(line));
		}
		
		reader.close();
		
		return allDocLengths;
	}
}