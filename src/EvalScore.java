import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;

public class EvalScore {
	public static void main(String[] args) 
			throws FileNotFoundException, IOException {
		int numArgs = args.length; // check arguments provided
		
		if (numArgs < 3) { // if too few, notify user and exit
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 3, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		// parse first 3 arguments
		String resultPath = args[0];
		String qrelsPath = args[1];
		String writeDest = args[2];
		
		if (pathExists(writeDest)) { // if file to write to already exists inform user
			System.out.println("Error: File to Write to already exists");
			System.out.println("Please provide a file name that does not already exist");
			System.out.println("File provided: " + writeDest);
			System.exit(0);
		}
		
		if (!pathExists(resultPath)) { // if results file does not exist
			System.out.println("Error: Results File not found");
			System.out.println("Please provide an existing file for results");
			System.out.println("File provided: " + resultPath);
			System.exit(0);
		}
		
		if (!pathExists(qrelsPath)) { // if qrels file does not exist, inform user
			System.out.println("Error: qrels File not found");
			System.out.println("Please provide an existing file for qrels");
			System.out.println("File provided: " + qrelsPath);
			System.exit(0);
		}
		
		HashSet<String> resultModes = new HashSet<String>(); // create set of modes
		
		for (int i = 3; i < numArgs; i++) { // for arguments past 3rd
			String resultType = args[i];
			
			if (resultType.equalsIgnoreCase("ap")) { // if user wants ap
				if (!resultModes.contains("ap")) {
					resultModes.add("ap");
				}
			} else if (resultType.equalsIgnoreCase("p_10")) { // if user wants p@10
				if (!resultModes.contains("p_10")) {
					resultModes.add("p_10");
				}
			} else if (resultType.equalsIgnoreCase("ndcg_10")) { // if user wants ndcg@10
				if (!resultModes.contains("ndcg_10")) {
					resultModes.add("ndcg_10");
				}
			} else if (resultType.equalsIgnoreCase("ndcg_1000")) { // if user wants ndcg@1000
				if (!resultModes.contains("ndcg_1000")) {
					resultModes.add("ndcg_1000");
				}
			} else if (resultType.equalsIgnoreCase("all")) { // if user wants all accuracies
				resultModes.clear();
				resultModes.add("ap");
				resultModes.add("p_10");
				resultModes.add("ndcg_10");
				resultModes.add("ndcg_1000");
				break; // exit, as this includes all
			}
		}
		
		if (resultModes.size() < 1) { // if no accuracies were provided inform user
			System.out.println("Please provide at least 1 mode of results");
			System.out.println("Possible result modes are:");
			System.out.println("ap for average precision");
			System.out.println("p_10 for precision@10");
			System.out.println("ndcg_10 for ndcg@10");
			System.out.println("ndcg_1000 for ndcg@1000");
			System.out.println("all for all of the above options");
		} else { // if at least one provided, calculate everything
			calculateResults(writeDest, qrelsPath, resultPath, resultModes);
		}
	}
	
	private static void calculateResults(String writePath, String qrelsPath, String resultPath, HashSet<String> resultModes) 
			throws IOException, FileNotFoundException {
		/*
		 * Calculate all accuracies for given measures with paths for all files
		 */
		
		BufferedReader resultFile = new BufferedReader(new FileReader(resultPath)); //file with results
		
		HashMap<Integer, ArrayList<ResultScore>> userResults = makeUserResultMap(resultFile); // make user results
		resultFile.close();
		
		if (userResults == null) { // if user results is null
			System.out.println("ERROR with formatting"); // inform user of error
			System.exit(0);
		}
		
		BufferedReader qrelsFile = new BufferedReader(new FileReader(qrelsPath)); // file with qrels

		HashMap<Integer, String> relevantResults = null; // relevantResults map, topic to string with all docnos
		
		relevantResults = findRelevant(qrelsFile); // create map of relevant results
		qrelsFile.close();
		
		String neededDirs = ""; // directories to be made
		String[] directories = writePath.split("\\\\"); //split up by slashes for directories
		
		for (int i = 0; i < directories.length - 2; i ++) {
			neededDirs += directories[i]+"\\"; // create path for files
		}
		neededDirs+=directories[directories.length - 2]; // add last path
		
		new File(neededDirs).mkdirs(); //create directories for file
		
		FileWriter resultOutput = new FileWriter(writePath); // new file to write to
		writeHeader(resultOutput, resultModes); // create headers for accuracies
		
		// sorted set documentation: https://www.geeksforgeeks.org/sortedset-java-examples/
		SortedSet<Integer> topics = new TreeSet<Integer>();
		
		// sort set of topics, so topics are in order
		for (Integer topic: relevantResults.keySet()) {
			topics.add(topic);
		}
		
		for (Integer topic: topics) { // for each topic in order
			resultOutput.write(topic + "\t\t"); // write topic to file
			
			// calculate and write numeric results
			numericResults(resultOutput, userResults.get(topic), resultModes, relevantResults.get(topic));
			
			resultOutput.write("\n");
		}
		
		resultOutput.close();
	}
	
	private static HashMap<Integer, ArrayList<ResultScore>> makeUserResultMap(BufferedReader resultFile) 
			throws IOException{
		/*
		 * Create HashMap of UserResults
		 * Return map of topics, and list of Results for this topic in order of score
		 */
		
		HashMap<Integer, ArrayList<ResultScore>> userResults = new HashMap<Integer, ArrayList<ResultScore>>();
		boolean properFormat = true; // everything is properly formatted
		
		while (true) { // until end of file
			String line = resultFile.readLine();
			
			if (line == null) { // if end of file, leave
				break;
			}
			
			// split line at spaces
			String[] lineSplit = line.split("\s+");
			Integer topic = 0; // initialize topic, docno and score
			String docno = null;
			double score = 0.0;
			
			// if length is not 6
			if (lineSplit.length != 6) {
				// show user line and length of line
				System.out.println(line);
				System.out.println("Line tokens: " + lineSplit.length);
				properFormat = false; // invalid format
			}
			
			try { // try to parse tokens
				topic = Integer.parseInt(lineSplit[0]); //topic is integer at 1st
				docno = lineSplit[2]; // docno is at 2nd
				
				@SuppressWarnings("unused") // rank is never used again
				int rank = Integer.parseInt(lineSplit[3]); // rank is integer at 3rd
				
				score = Double.parseDouble(lineSplit[4]); // score is double at 4th
				
			} catch (NumberFormatException e) { // if any invalid numbers
				System.out.print(line + "\n"); // print out invalid line
				properFormat = false; // invalid format
			}
			
			if (!properFormat) { // if format is invalid
				return null; //return a null map
			}
			
			// create new resut score with score and docno
			ResultScore newResult = new ResultScore(score, docno);
			
			if (userResults.containsKey(topic)) { // if topic already exists
				ArrayList<ResultScore> topicResults = userResults.get(topic); // get the arraylist
				insertResult(newResult, topicResults); // insert the result in thearraylist
				
			} else {// if topic does not exist
				ArrayList<ResultScore> topicResults = new ArrayList<ResultScore>(); // new arraylist
				topicResults.add(newResult); //add result
				userResults.put(topic, topicResults); // put the result in the arraylist
			}
		}
		
		return userResults; // return map with topics and all results
	}

	public static void insertResult(ResultScore newResult, ArrayList<ResultScore> allResults) {
		/*
		 * Insert result into arraylist to maintain descending order by score
		 */
		if (allResults.size() == 0) {
			allResults.add(0, newResult);
			return;
		}
		
		int left = 0; // left index
		int right = allResults.size() - 1; //right index
		int curIndex = 0; //index to check
		boolean complete = false; //complete search for index to insert at
		
		while (!complete) { //while index not yet found
			curIndex = (left + right) / 2; //set current index to middle of left and right
			ResultScore competitor = allResults.get(curIndex); // get competitor at this index
			
			if (newResult.compare(competitor)) { // if this is greater
				if (right == left) { // if no more to search
					complete = true; // set complete
				} else {
					right = curIndex; //set right index to index checked
				}
			} else { // this is less than competitor
				if (right == left) { // if index found
					curIndex += 1; // move index one to right
					complete = true; // mark as complete
				} else {
					if (left == curIndex) { // if left index was checked
						left = right; // move left to right
					} else {
						left = curIndex; // otherwise set left to what was checked
					}
				}
				
			}
		}
		
		// add result at index found
		allResults.add(curIndex, newResult);
	}
	
	private static void writeHeader(FileWriter output, HashSet<String> resultModes) 
			throws IOException {
		/*
		 * Write header to file
		 */
		output.write("Topic # \t"); //write topic header
		
		if (resultModes.contains("ap")) { // average precision
			output.write("Average Precision \t");
		}
		if (resultModes.contains("p_10")) { // precision @ 10
			output.write("Precision @ 10 \t\t");
		} 
		if (resultModes.contains("ndcg_10")) { //ndcg @10
			output.write("NDCG @ 10 \t\t");
		}
		if (resultModes.contains("ndcg_1000")) { // ndcg @1000
			output.write("NDCG @ 1000");
		}
		
		output.write("\n");
	}
	
	private static HashMap<Integer, String> findRelevant(BufferedReader qrelsFile) 
			throws IOException{
		/*	Creates hashmap mapping topic id to string of all relevant docnos
		 * 	Relevant docnos separated by spaces for future parsing
		 * 	Input: Buffered Reader of qrelsFile
		 * 	Output: HashMap for topic ids to string of relevant docnos
		 */
		
		HashMap<Integer, String> relevantResults = new HashMap<Integer, String>();
		
		while (true) { // until file runs out of lines
			String line = qrelsFile.readLine();
			if (line == null) {
				break;
			}
			
			// split line at spaces
			String[] lineSplit = line.split(" ");
			// get relevancy of document
			String relevant = lineSplit[3];
			
			if (relevant.equals("1")) { // if document is  relevant
				// find topic and docno
				Integer topic = Integer.parseInt(lineSplit[0]);
				String docno = lineSplit[2];
				
				// if first time for topic
				if (!relevantResults.keySet().contains(topic)) {
					relevantResults.put(topic, docno);
					
				// if topic already exists, add to string
				} else {
					String docnos = relevantResults.get(topic);
					docnos = docnos + " " + docno;
					relevantResults.replace(topic, docnos);
				}
			}
		}
	
		return relevantResults;
	}
	
	private static void numericResults(FileWriter output, ArrayList<ResultScore> userResults, HashSet<String> resultModes, String actualResults) 
			throws IOException{
		/*
		 * Calculate numeric results for all required accuracies
		 */
		String[] posResults = actualResults.split(" "); // split relevant results by spaces
		HashSet<String> searchResults = new HashSet<String>(); // create set of relevant results
		
		for (String result: posResults) { // add all relevant results to set
			searchResults.add(result);
		}
		
		if (resultModes.contains("ap")) { // average precision
			if (userResults == null) { // if no user results, use 0
				output.write("0.00000\t\t\t");
			} else { // calculate average precision
				String result = MeasureCalculations.averagePrecision(1000, searchResults, userResults);
				output.write(result + "\t\t\t");
			}
		}
		
		if (resultModes.contains("p_10")) { // precision @ 10
			if (userResults == null) { // if no user results, use 0
				output.write("0.00000\t\t\t");
			} else { //calculate precision @ 10
				String result = MeasureCalculations.precision(10, searchResults, userResults);
				output.write(result + "\t\t\t");
			}
		} 
		
		if (resultModes.contains("ndcg_10")) { //ndcg @ 10
			if (userResults == null) { // if no user results, use 0
				output.write("0.00000\t\t\t");
			} else { // calculate ndcg@10
				String result = MeasureCalculations.ndcg(10, searchResults, userResults);
				output.write(result + "\t\t\t");
			}
		}
		
		if (resultModes.contains("ndcg_1000")) { //ndcg @ 1000
			if (userResults == null) { // if no user results, use 0
				output.write("0.00000");
			} else { //calculate ndcg @1000
				String result = MeasureCalculations.ndcg(1000, searchResults, userResults);
				output.write(result);
			}
		}
	}
	
	public static boolean pathExists(String directory) {
		/* Check if a path exists for a given directory
		 *  Return true or false accordingly
		 */
		Path writePath = Paths.get(directory);
		
		return Files.exists(writePath);
	}
}