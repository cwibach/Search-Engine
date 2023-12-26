import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BM25 {
	public static void main(String[] args) throws FileNotFoundException, IOException{
		int numArgs;
		String retrievePath;
		String queriesFileName;
		String writePath;
		double k1 = 1.2;
		double b = 0.75;
		
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
		
		if (numArgs > 3) { // if extra argument for k1
			try { // check k1 value is valid to use
				double temp = Double.parseDouble(args[3]);
				k1 = temp;
				System.out.println("Using k1-value of: " + String.format("%.3f", k1));
			} catch (NumberFormatException e) {
				System.out.println("Invalid k1-value provided.");
				System.out.println("Plese provide double, you provided: " + args[3]);
				System.out.println("Using default value of 1.2");
			}
		}
		
		if (numArgs > 4) { // if b value provided
			try { // check b value is valid to use
				double temp = Double.parseDouble(args[4]);
				if (temp < 0 || temp > 1.001) {
					System.out.println("Invalid b-value provided.");
					System.out.println("Plese provide double between 0 and 1, you provided: " + args[4]);
					System.out.println("Using default value of 0.75");
				} else {
					b = temp;
					System.out.println("Using B-value of: " + String.format("%.3f", b));
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid b-value provided.");
				System.out.println("Plese provide double between 0 and 1, you provided: " + args[4]);
				System.out.println("Using default value of 0.75");
			}
		}
		
		boolean stemming = false;
		
		if (numArgs > 5) { // if stemming parameter provided
			if (args[5].equalsIgnoreCase("Stem")) { //check if argument asks for stemming
				stemming = true;
			} else {
				System.out.println("6th Argument ineffective");
				System.out.println("For stemming please use argument 'Stem'");
				System.out.println("You provided Argument: " + args[5]);
				System.out.println("Continuing without stemming");
			}
		}
		
		searchDoc(retrievePath, queriesFileName, writePath, k1, b, stemming);
	}
	
	public static void searchDoc(String retrievePath, String queriesFileName, 
			String writePath, double k1, double b, boolean stemming) 
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
		
		// get all docnos
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(retrievePath);
		
		// get all doc lengths
		ArrayList<Integer> allDocLengths = InvertedMethods.readDocLengths(retrievePath);

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
			
			// create end tag from stemming
			String endTag = " cwibachBM25";
			if (stemming) {
				endTag += "stemb=";
			} else {
				endTag += "nostemb=";
			}
			
			// add end tag for b and k1 values
			endTag += Double.toString(b) + "k1=";
			endTag += Double.toString(k1) + "\n";
			
			// Get search result for token ids from query
			String searchResult = BM25Search(tokenIDs, invertedIndex, topicID, retrievePath, k1, b, endTag, allDocnos, allDocLengths);
			
			// Write search result to file
			searchOutput.write(searchResult);
		}
		
		searchOutput.close(); //close file written to
	}
	
	public static String BM25Search(int[] tokenIDs, ArrayList<ArrayList<Integer>> invertedIndex, 
			int topicID, String retrievePath, double k1, double b, String endTag,
			ArrayList<String> allDocnos, ArrayList<Integer> allDocLengths) 
			throws FileNotFoundException, IOException {
		
		String result = "";
		
		// calculate average document length
		int numDocs = allDocnos.size();
		int totalLength = 0;
		for (int docLength: allDocLengths) {
			totalLength += docLength;
		}
		
		double avgLength = (double)totalLength/(double)numDocs;
		
		// HashMap for scores per document
		HashMap<Integer, Double> docScores = new HashMap<Integer, Double>();
		
		// for each token id that exists, retrieve list and perform search
		for (int searchID: tokenIDs) {
			if (searchID >= 0) {
				ArrayList<Integer> queryDocs = invertedIndex.get(searchID);
				
				// calculate BM25 term at a time
				termBM25(queryDocs, numDocs, allDocLengths, avgLength, docScores, k1, b);
			}
		}
		
		// create arraylist of results
		ArrayList<ResultScore> queryResults = new ArrayList<ResultScore>();
		
		for (Integer document: docScores.keySet()) {
			// for each document with at least 1 term from query
			// get score and docno
			double score = docScores.get(document);
			String docno = allDocnos.get(document);
			
			// create resultScore
			ResultScore docResult = new ResultScore(score, docno);
			
			// insert into sorted arraylist
			EvalScore.insertResult(docResult, queryResults);
		}
		
		// for first thousand in results (unless < 1000)
		for (int rank = 1; rank <= 1000; rank++) {
			if (rank > queryResults.size()) {
				break;
			}
			
			// get the score and docno
			ResultScore docResult = queryResults.get(rank-1);
			String docno = docResult.getDocno();
			double score = docResult.getScore();
			
			// write out file
			result += topicID + " Q0 " + docno + " " + rank + " " + score + endTag;
		}
		
		return result;
	}
	
	public static void termBM25(ArrayList<Integer> queryDocs, int numDocs, ArrayList<Integer> docLengths, 
			double avgLength, HashMap<Integer, Double> docScores, double k1, double b) {
		/*Calculate BM25 value with list of docs with term frequency, total num docs, list of doc lengths, average lengths
		 * the map of scores, k1 value and b value
		 * 
		 */
		int index = 0;
		int numWithTerm = queryDocs.size() / 2; // get number with term
		
		while (index < queryDocs.size()) { // for each of the docs with the term
			int docID = queryDocs.get(index); // get doc id
			int termCount = queryDocs.get(index+1); //get count
			int docLength = docLengths.get(docID); // get doc length
			
			// k value
			double k = k1 * ((1-b) + b*((double)docLength / (double)avgLength));
			
			// BM25 term document value
			double fullValue = ((double)termCount / (k+(double)termCount))*
					Math.log(((double)numDocs - (double)numWithTerm + 0.5)/((double)numWithTerm + 0.5));
			
			// if already in dictionary, update value
			if (docScores.containsKey(docID)) {
				double curScore = docScores.get(docID);
				curScore += fullValue;
				docScores.replace(docID, curScore);
			} else { //if not, add it to dictionary
				docScores.put(docID, fullValue);
			}
			
			// increase index by 2
			index+=2;
		}
	}
}