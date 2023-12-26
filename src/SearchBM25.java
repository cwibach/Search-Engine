import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SearchBM25 {
	public static void main(String args[]) throws FileNotFoundException, IOException {
		int numArgs = args.length;
		String retrievePath;
		
		if (numArgs < 1) { // Ensure necessary arguments are provided
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 1, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		retrievePath = args[0];
		
		// If the path does not exist to retrieve a file from
		if (!IndexEngine.pathExists(retrievePath)) {
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + retrievePath);
			System.exit(0);
		}

		search(retrievePath);
	}
	
	public static void search(String retrievePath) throws FileNotFoundException, IOException {
		
		// Initial objects for storing data
		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
		HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		// Read lexicon and inverted index
		InvertedMethods.readLexicon(retrievePath, lexicon, reverseLexicon);
		InvertedMethods.readInvertedIndex(retrievePath, invertedIndex);
		
		// get all docnos
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(retrievePath);
		
		// get all doc lengths
		ArrayList<Integer> allDocLengths = InvertedMethods.readDocLengths(retrievePath);
		
		// read user input
		Scanner input = new Scanner(System.in);
		
		// search but don't stem
		boolean searching = true;
		boolean stemming = false;
		
		// until the searching is completed
		while (searching) {
			// get user query
			String userQuery = userSearch(input);
			
			// https://javarevisited.blogspot.com/2012/04/how-to-measure-elapsed-execution-time.html#axzz8K6fYJazw 
			long startTime = System.currentTimeMillis();
			
			// tokenize the query string and convert to tokenIDs
			String[] tokens = InvertedMethods.tokenize(userQuery, stemming);
			
			// if no query provided
			if (tokens.length == 0) {
				System.out.println("Please enter a search with at least one term");
				continue;
			}
			
			// convert query tokens to ids
			int[] tokenIDs = InvertedMethods.getSearchTokenIDs(tokens, lexicon);
			
			// get results from search in ordered list
			ArrayList<ResultScore> searchResult = 
					searchQuery(tokenIDs, invertedIndex, retrievePath, allDocnos, allDocLengths);
			
			// if no results found inform user
			if (searchResult.size() == 0) {
				System.out.println("Sorry no results found :( \n");
			} else {
				// print top 10 results found
				printTop10(searchResult, tokenIDs, retrievePath, lexicon, invertedIndex);
			}
			
			// calculate time taken and inform user
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println("Time taken to retrieve: " + elapsedTime/(double)1000 + " seconds!");
			
			// find user's next step
			String nextStep = userNextStep(input);
			
			// logic for next option
			if (nextStep.equals("Q")) {
				// exit if user enters q
				searching = false;
			} else if (nextStep.equals("N")) {
				// for new query just continue
			} else {
				// user wants to retrieve doc, so do so
				String afterStep = docRequested(nextStep, searchResult, retrievePath, input);
				
				// get next step, and exit if user wants to exit
				if (afterStep.equals("Q")) {
					searching = false;
				}
			}
		}
		
		// close input to end program
		input.close();
	}
	
	public static void printTop10(ArrayList<ResultScore> searchResult, int[] tokenIDs, String retrievePath, 
			HashMap<String, Integer> lexicon, ArrayList<ArrayList<Integer>> invertedIndex) 
			throws IOException {
		/*
		 * Print the top 10 results nicely from list of results
		 * Takes ordered list, token IDs, path to retrieve from, lexicon and inverted index
		 */
		
		// number to return, in case of less than 10 results
		int finalCount = Math.min(10, searchResult.size());
		
		// for each of the top x results (usually 10)
		for (int i = 0; i < finalCount; i++) {
			// get document from list
			ResultScore curDoc = searchResult.get(i);
			
			// increase result number
			int resultNum = i + 1;
			// get the docno
			String docno = curDoc.getDocno();
			
			// get headline, dateline and full text from docno
			String[] fileParts = GetFile.retrieveFileParts(retrievePath, docno);
			
			String headline = fileParts[1].strip(); // get document headline
			
			String dateLine = fileParts[0].strip(); // get document dateline
			
			String docFull = fileParts[2].strip(); // get document Text
			
			// get text from full
			String docText = IndexEngine.getMetaData("text", docFull);
			// get graphic from full
			String docGraphic = IndexEngine.getMetaData("graphic", docFull);
			
			// combine text and graphic for excerpts searching
			String finalText = InvertedMethods.getDocString("", docGraphic, docText).strip();
			
			// get excerpts from final text
			String docExcerpts = getDocExcerpts(finalText, tokenIDs, lexicon, invertedIndex); // get doc excerpts
			
			// if no headline exists
			if (headline.equalsIgnoreCase("No headline for this document")) {
				// use first 50 chars of excerpts instead
				headline = docExcerpts.substring(0, Math.min(49, docExcerpts.length()-1)) + "...";
			}
			
			// print header line
			System.out.println(resultNum + ". " + headline + "; (" + dateLine + ")");
			System.out.print(docExcerpts); // print excerpts
			System.out.print("("+ docno+")\n\n"); // print docno at end with newline
		}
	}
	
	public static String getDocExcerpts(String docText, int[] queryTokenIDs, 
			HashMap<String, Integer> lexicon, ArrayList<ArrayList<Integer>> invertedIndex){
		/*
		 * Get doc excerpts from text given the search terms, lexicon and inverted index
		 */
		
		// list of all sentences, token sentences and sorted by score
		ArrayList<String> fullSentences = new ArrayList<String>();
		ArrayList<int[]> tokenSentences = new ArrayList<int[]>();
		ArrayList<ResultScore> sortedSentences = new ArrayList<ResultScore>();
		
		// initialize variable for iterating through
		int startSentence = 0;
		int i;
		
		// until end of document
		for (i = 0; i < docText.length(); i++) {
			char c = docText.charAt(i); // check character for end of sentence
			if ((c == '!') || (c == '.') || (c == '?')) {
				// split as sentence same as tokenization
				if (startSentence != i) {
					// take sentenct and remove whitespace
					String sentence = docText.substring(startSentence, i) + c;
					sentence = sentence.strip();
					
					// if sufficiently long
					if (sentence.length() > 10) {
						// ensure first character not stop character
						char firstChar = sentence.charAt(0);
						if (((firstChar == '!') || (firstChar == '.') || (firstChar == '?'))) {
							sentence = sentence.substring(1).strip();
						}
						// add to list of full sentences
						fullSentences.add(sentence);
					}
					
					// go to next sentence
					startSentence = i+1;
				}
			}
		}
		
		// add the final sentence if there is one extra at the end
		if (startSentence != i) {
			String sentence = docText.substring(startSentence);
			sentence = sentence.strip();
			
			// if sufficiently long
			if (sentence.length() > 10) {
				// ensure first character not stop character
				char firstChar = sentence.charAt(0);
				if (((firstChar == '!') || (firstChar == '.') || (firstChar == '?'))) {
					sentence = sentence.substring(1).strip();
				}
				// add to list of full sentences
				fullSentences.add(sentence);
			}
		}
		
		// for each sentence
		for (String sentence: fullSentences) {
			// tokenize it and get ids, add to tokenized list
			String[] sentenceTokens = InvertedMethods.tokenize(sentence, false);
			int[] sentenceTokenIDs = InvertedMethods.TokentoIDs(sentenceTokens, lexicon, invertedIndex);
			tokenSentences.add(sentenceTokenIDs);
		}
		
		// for each sentence left
		for (i = 0; i < fullSentences.size(); i++) {
			// get a temporary docno of position
			String tempDocno = Integer.toString(i);
			int lScore = Math.max(0, 2-i); // lscore
			int kScore = 0; // kscore
			int matchStreak = 0; // streak for k score
			
			// map finds of each term in query
			HashMap<Integer, Integer> queryFinds = new HashMap<Integer, Integer>();
			
			// Assemble hashmap for count of each query term found
			for (int queryToken: queryTokenIDs) {
				if (!queryFinds.containsKey(queryToken)) {
					queryFinds.put(queryToken, 0);
				}
			}
			
			// for each token in sentence
			for (int tokenID: tokenSentences.get(i)) {
				// if token is from query
				if (queryFinds.containsKey(tokenID)) {
					matchStreak += 1; // streak gets longer
					kScore = Math.max(matchStreak, kScore); //check increasing of k-score
					
					// get current occurences and increment by 1
					int curCount = queryFinds.get(tokenID);
					queryFinds.replace(tokenID, curCount + 1);
				} else {
					matchStreak = 0; // reset streak
				}
			}
			
			int cScore = 0; // c score
			int dScore = 0; // dscore
			
			// for each term from query
			for (int queryToken: queryFinds.keySet()) {
				// get count of finds in sentence
				int count = queryFinds.get(queryToken);
				if (count > 0) { // if exists
					dScore += 1; // increase d by 1
					cScore += count; // increase c by count
				}
			}
			
			// add up scores for total
			int vScore = lScore + kScore + cScore + dScore;
			
			// create result score with temp docno and vscore
			ResultScore sentenceResult = new ResultScore(vScore, tempDocno);
			// insert into sorted arraylist
			EvalScore.insertResult(sentenceResult, sortedSentences);
		}
		
		// create string for snippet
		String queryBiasSnippet = "";
		
		// take the first 2, or first if there is only 1
		for (i = 0; i < Math.min(2, sortedSentences.size()); i++) {
			// get the next sentence key info
			ResultScore nextBest = sortedSentences.get(i);
			// get the index it is in the lists
			int index = Integer.parseInt(nextBest.getDocno());
			String sentence = fullSentences.get(index);
			
			// remove starting quotation mark
			if (sentence.charAt(0) == '"') {
				sentence = sentence.substring(1);
			}
			
			// add to snippet with trailing space
			queryBiasSnippet += sentence.strip() + " ";
		}
		
		// return final snippet
		return queryBiasSnippet;
	}
	
	public static String docRequested(String docNumString, ArrayList<ResultScore> searchResult, 
			String retrievePath, Scanner input) 
			throws FileNotFoundException, IOException {
		/*
		 * Handle user asking for doc retrieval
		 */
		// get doc number
		int docNum = Integer.parseInt(docNumString);
		
		// get result score from search resullt for docno
		ResultScore docRequest = searchResult.get(docNum - 1);
		
		// arguments for getfile
		String[] getFileArgs = {retrievePath, "docno", docRequest.getDocno()};
		
		// header for result
		System.out.println("Document Number: " + docNum + " from results: \n\n" );
		
		// getfile
		GetFile.main(getFileArgs);
		System.out.println("\n");
		
		// get user next step
		String nextStep = userNextStep(input);
		
		// return if user wants new query or quit, repeat otherwise
		if (nextStep.equals("Q") || nextStep.equals("N")) {
			return nextStep;
		} else {
			return docRequested(nextStep, searchResult, retrievePath, input);
		}
	}
	
	public static String userSearch(Scanner input) {
		// ask user for query
		System.out.println("Please enter your search query: ");
		
		// read and return user input
		String result = input.nextLine();
		
		return result;
	}
	
	public static String userNextStep(Scanner input) {
		/*
		 * Ask user for next step from:
		 * A number for that doc from results
		 * N for a new query
		 * Q to quit
		 */
		
		// inform user of options
		System.out.println("Please choose next step, options are:");
		System.out.println("\t The Number of a document to view the full document");
		System.out.println("\t 'N' for a new query");
		System.out.println("\t 'Q' to quit the program");
		
		// read user input
		String result = input.nextLine().strip();
		
		// return q or n if it is user result
		if (result.equalsIgnoreCase("N")) {
			return "N";
		} else if (result.equalsIgnoreCase("Q")) {
			return "Q";
		}
		
		try {
			// try to get number from other input
			int docNumber = Integer.parseInt(result);
			
			// ensure number is from 1 to 10, tell user if not
			if ((docNumber < 1) || (docNumber > 10)) {
				System.out.println("Please use a number between 1 and 10");
				// repeat if invalid
				return userNextStep(input);
			} else {
				// return number from user
				return result;
			}
		} catch (NumberFormatException e) {
			// inform user input invalid, repeat
			System.out.println("Invalid argument provided");
			return userNextStep(input);
		}
	}

	public static ArrayList<ResultScore> searchQuery(int[] tokenIDs, ArrayList<ArrayList<Integer>> invertedIndex, 
			String retrievePath, ArrayList<String> allDocnos, ArrayList<Integer> allDocLengths) {
		/*
		 * search through a query from the user
		 */
		
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
				BM25.termBM25(queryDocs, numDocs, allDocLengths, avgLength, docScores, 1.2, 0.75);
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
		
		
		return queryResults;
	}
}