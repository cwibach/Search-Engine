import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CosineSearch {
	public static void main(String[] args) throws FileNotFoundException, IOException{
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
		
		if (numArgs > 3) { // if argument for stemming provide, ensure correct argument
			if (args[3].equalsIgnoreCase("Stem")) {
				stemming = true;
			} else {
				System.out.println("3rd Argument ineffective");
				System.out.println("For stemming please use argument 'Stem'");
				System.out.println("You provided Argument: " + args[5]);
				System.out.println("Continuing without stemming");
			}
		}
		
		searchDoc(retrievePath, queriesFileName, writePath, stemming);
	}
	
	public static void searchDoc(String retrievePath, String queriesFileName, 
			String writePath, boolean stemming) 
			throws FileNotFoundException, IOException{
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
			
			// Create end tag according to stemming
			String endTag = " cwibachCOSINE";
			if (stemming) {
				endTag += "stem";
			} else {
				endTag += "nostem";
			}
			
			endTag += "\n";
			
			// Get search result for token ids from query
			String searchResult = CosineSearching(tokenIDs, invertedIndex, topicID, retrievePath, endTag);
			
			// Write search result to file
			searchOutput.write(searchResult);
		}
		
		searchOutput.close(); //close file written to
	}
	
	public static String CosineSearching(int[] tokenIDs, ArrayList<ArrayList<Integer>> invertedIndex, 
			int topicID, String retrievePath, String endTag) 
			throws FileNotFoundException, IOException {
		String result = "";
		
		// get all docnos
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(retrievePath);
		
		// get all doc lengths
		ArrayList<Integer> allDocLengths = InvertedMethods.readDocLengths(retrievePath);
		
		int numDocs = allDocnos.size();
		
		// HashMap for scores per document
		HashMap<Integer, Double> docScores = new HashMap<Integer, Double>();
		
		// for each token id that exists, retrieve list and perform search
		for (int searchID: tokenIDs) {
			if (searchID >= 0) {
				ArrayList<Integer> queryDocs = invertedIndex.get(searchID);
				
				// calculate score term at a time
				termCosine(queryDocs, numDocs, allDocLengths,  docScores);
			}
		}
		
		// get results
		ArrayList<ResultScore> queryResults = new ArrayList<ResultScore>();
		
		// for each document from results
		for (Integer document: docScores.keySet()) {
			double score = docScores.get(document);
			String docno = allDocnos.get(document);
			
			// get values and create resultscore for document
			ResultScore docResult = new ResultScore(score, docno);
			
			// insert into sorted arraylist
			EvalScore.insertResult(docResult, queryResults);
		}
		
		// return top 1000 results (or number that exist)
		for (int rank = 1; rank <= 1000; rank++) {
			if (rank > queryResults.size()) {
				break;
			}
			
			// get key values for result
			ResultScore docResult = queryResults.get(rank-1);
			String docno = docResult.getDocno();
			double score = docResult.getScore();
			
			// create line for results file
			result += topicID + " Q0 " + docno + " " + rank + " " + score + endTag;
		}
		
		return result;
	}
	
	public static void termCosine(ArrayList<Integer> queryDocs, int numDocs, 
			ArrayList<Integer> docLengths, HashMap<Integer, Double> docScores) {
		/*
		 * Calculate cosine value for document term
		 * Use list of documents from inverted index, total number of docs
		 * Lengths of documents and map of document scores
		 */
		int index = 0;
		int numWithTerm = queryDocs.size() / 2; //num docs with term
		
		while (index < queryDocs.size()) { // for each doc with term
			int docID = queryDocs.get(index); // get id
			int termCount = queryDocs.get(index+1); // get term frequency
			int docLength = docLengths.get(docID); // get doc lengths
			
			// calculate cosine value
			double fullValue = (1+ Math.log(termCount))*Math.log(1 + ((double)numDocs / (double)numWithTerm))/(double)docLength;
			
			// if document already in dictionary, add to score
			if (docScores.containsKey(docID)) {
				double curScore = docScores.get(docID);
				curScore += fullValue;
				docScores.replace(docID, curScore);
			} else { // if not in dictionary, add to dictionary
				docScores.put(docID, fullValue);
			}
			
			// increase index by 2
			index+=2;
		}
	}
}