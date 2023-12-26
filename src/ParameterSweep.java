import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ParameterSweep {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		int numArgs = args.length; // check arguments provided
		
		if (numArgs < 4) { // if too few, notify user and exit
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 4, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		double initialk1 = 0.0; //k1 value to start at
		
		if (numArgs > 4) { // if extra argument for initial k1 value
			try { // check k1 value is valid to use
				double temp = Double.parseDouble(args[4]);
				initialk1 = temp;
				System.out.println("Using initial k1-value of: " + String.format("%.3f", initialk1));
			} catch (NumberFormatException e) {
				System.out.println("Invalid initial k1-value provided.");
				System.out.println("Plese provide double, you provided: " + args[4]);
				System.out.println("Using default value of 0.0");
			}
		}
		
		double initialb = 0.0; // b value to start at
		
		if (numArgs > 5) { // if initial b value provided
			try { // check b value is valid to use
				double temp = Double.parseDouble(args[5]);
				if (temp < 0 || temp > 1.001) {
					System.out.println("Invalid initial b-value provided.");
					System.out.println("Plese provide double between 0 and 1, you provided: " + args[5]);
					System.out.println("Using default value of 0.0");
				} else {
					initialb = temp;
					System.out.println("Using initial b-value of: " + String.format("%.3f", initialb));
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid initial b-value provided.");
				System.out.println("Plese provide double between 0 and 1, you provided: " + args[5]);
				System.out.println("Using default value of 0.0");
			}
		}
		
		double finalk1 = 10.0; // final k1 value to end at 
		
		if (numArgs > 6) { // if extra argument for final k1
			try { // check k1 value is valid to use
				double temp = Double.parseDouble(args[6]);
				finalk1 = temp;
				System.out.println("Using k1-value of: " + String.format("%.3f", finalk1));
			} catch (NumberFormatException e) {
				System.out.println("Invalid final k1-value provided.");
				System.out.println("Plese provide double, you provided: " + args[6]);
				System.out.println("Using default value of 0.0");
			}
		}
		
		double finalb = 1.0; // final b value to end at
		
		if (numArgs > 7) { // if final b value provided
			try { // check b value is valid to use
				double temp = Double.parseDouble(args[7]);
				if (temp < 0 || temp > 1.001) {
					System.out.println("Invalid final b-value provided.");
					System.out.println("Plese provide double between 0 and 1, you provided: " + args[7]);
					System.out.println("Using default value of 1.0");
				} else {
					finalb = temp;
					System.out.println("Using final b-value of: " + String.format("%.3f", finalb));
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid final b-value provided.");
				System.out.println("Plese provide double between 0 and 1, you provided: " + args[7]);
				System.out.println("Using default value of 1.0");
			}
		}
		
		double intervalk1 = 0.1; // k1 interval to increase by
		
		if (numArgs > 8) { // if extra argument for k1 interval
			try { // check k1 value is valid to use
				double temp = Double.parseDouble(args[8]);
				intervalk1 = temp;
				System.out.println("Using k1 interval of: " + String.format("%.3f", intervalk1));
			} catch (NumberFormatException e) {
				System.out.println("Invalid k1 interval provided.");
				System.out.println("Plese provide double, you provided: " + args[8]);
				System.out.println("Using default value of 0.1");
			}
		}
		
		double intervalb = 0.05; // b interval to increase by
		
		if (numArgs > 9) { // if extra argument for k1 interval
			try { // check k1 value is valid to use
				double temp = Double.parseDouble(args[9]);
				intervalb = temp;
				System.out.println("Using b interval of: " + String.format("%.3f", intervalb));
			} catch (NumberFormatException e) {
				System.out.println("Invalid b interval provided.");
				System.out.println("Plese provide double, you provided: " + args[9]);
				System.out.println("Using default value of 0.05");
			}
		}
		
		// parse first 3 arguments
		String indexPath = args[0]; //latimes docs
		String qrelsPath = args[1]; //qrels file
		String queriesFilePath = args[2]; //queries file
		String basePath = args[3]; //new directory for results
		
		// If the path does not exist to retrieve a file from
		if (!IndexEngine.pathExists(indexPath)) {
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + indexPath);
			System.exit(0);
		}
		
		if (!IndexEngine.pathExists(qrelsPath)) { // if qrels file does not exist, inform user
			System.out.println("Error: qrels File not found");
			System.out.println("Please provide an existing file for qrels");
			System.out.println("File provided: " + qrelsPath);
			System.exit(0);
		}
		if (!IndexEngine.pathExists(queriesFilePath)) { // if qrels file does not exist, inform user
			System.out.println("Error: queries File not found");
			System.out.println("Please provide an existing file for qrels");
			System.out.println("File provided: " + queriesFilePath);
			System.exit(0);
		}
		
		// Initial objects for storing data
		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
		HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		// Read lexicon and inverted index
		InvertedMethods.readLexicon(indexPath, lexicon, reverseLexicon);
		InvertedMethods.readInvertedIndex(indexPath, invertedIndex);
		
		// get all docnos
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(indexPath);
		
		// get all doc lengths
		ArrayList<Integer> allDocLengths = InvertedMethods.readDocLengths(indexPath);
		
		String outputText = "b-value"; // text to write to file
		
		// add headers for k1 values to use
		for (double k1 = initialk1; k1-0.001  <= finalk1; k1 += intervalk1) {
			outputText += " " + String.format("%.3f", k1);
		}
		outputText += "\n"; // go to new line
		
		// for each b value to use
		for (double b = initialb; b-0.001 <= finalb; b+=intervalb) {
			// add b-value to file and print to system output to show progress
			outputText += String.format("%.3f", b);
			System.out.println("Using b-value of: " + String.format("%.3f", b));
			
			// for each k1 value to use
			for (double k1 = initialk1; k1-0.001  <= finalk1; k1 += intervalk1) {
				// create reader for queries
				BufferedReader queriesReader = new BufferedReader(new FileReader(queriesFilePath));
				
				// create file name, directories and files
				String thisFileName = "BM25k1=" + String.format("%.3f", k1) + "b=" + String.format("%.3f", b) + ".txt";
				String resultFileDir = basePath + "\\evalScores\\b=" + String.format("%.3f", b);
				String evalFileDir = basePath + "\\results\\b=" + String.format("%.3f", b);
				String resultFilePath = resultFileDir + "\\" + thisFileName;
				String evalFilePath = evalFileDir + "\\" + thisFileName;
				
				// https://www.programiz.com/java-programming/examples/create-and-write-to-file
				if (!IndexEngine.pathExists(evalFileDir)) { // if folders not fully created yet
					// make file structure
					File newDir = new File(evalFileDir);
					newDir.mkdirs(); 
					
					File newDirMetaData = new File(resultFileDir);
					newDirMetaData.mkdirs();
				}
				
				FileWriter searchOutput = new FileWriter(resultFilePath); // new file to write to
				
				// BM25 search for each query
				while (true) { // until the file runs out
					String line = queriesReader.readLine();
					
					if (line == null) {
						break; // if file is out of lines
					}
					
					// read the topic id, and the query string
					int topicID = Integer.parseInt(line.strip());
					line = queriesReader.readLine();
					
					// tokenize the query string and convert to tokenIDs
					String[] tokens = InvertedMethods.tokenize(line, false);
					int[] tokenIDs = InvertedMethods.getSearchTokenIDs(tokens, lexicon);
					
					// create end tag from stemming
					String endTag = " cwibachBM25nostemb=";
					
					// add end tag for b and k1 values
					endTag += String.format("%.3f", b) + "k1=";
					endTag += String.format("%.3f", k1) + "\n";
					
					// Get search result for token ids from query
					String searchResult = BM25.BM25Search(tokenIDs, invertedIndex, topicID, 
							indexPath, k1, b, endTag, allDocnos, allDocLengths);
					
					// Write search result to file
					searchOutput.write(searchResult);
				}
				
				// close search output and reader
				searchOutput.close();
				queriesReader.close();
				
				// evaluate newly created file with evalscore
				String[] evalScoreargs = {resultFilePath, qrelsPath, evalFilePath, "ndcg_10"};
				EvalScore.main(evalScoreargs);
				
				// calculate average accuracy from the evals
				double resultAccuracy = AverageAccuracy.evalAverage(evalFilePath);
				// create result string
				String result = Double.toString(resultAccuracy);
				
				// add to output
				outputText += " " + result;
			}
			outputText += "\n"; // next line
		}
		
		// create output file
		String writeFile = basePath + "\\sweep-accuracies.txt";
		FileWriter allAccuracies = new FileWriter(writeFile);
		
		// write to output file
		allAccuracies.write(outputText);
		
		allAccuracies.close();
		
	}
}