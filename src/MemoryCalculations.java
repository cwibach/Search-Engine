import java.io.IOException;
import java.util.ArrayList;

public class MemoryCalculations {
	public static void main(String[] args) throws IOException {
		/*
		 * Calculate values needed for memory use as specified
		 */
		int numArgs = args.length; // number of arguments
		
		if (numArgs < 2) { // if insufficent arguments
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide at least 2, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		String calcType = args[0]; // what calculation to perform
		String retrievePath = args[1]; // location of files
		
		if (!IndexEngine.pathExists(retrievePath)) {
			// ensure path exists to retrieve files from
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + retrievePath);
			System.exit(0);
		}
		
		if (calcType.equalsIgnoreCase("NumDocs")) { // number of documents
			calcNumDocs(retrievePath);
		} else if (calcType.equalsIgnoreCase("NumWords")) { // number of terms/tokens
			calcNumWords(retrievePath);
		} else if (calcType.equalsIgnoreCase("AvgDocs")) { // average documents per word
			calcAvgDocs(retrievePath);
		} else if (calcType.equalsIgnoreCase("All")) { // all of the above
			calcAll(retrievePath);
		} else { // none were provided
			System.out.println("Invalid Calculation type provided");
			System.out.println("Please provide one of: NumDocs, NumWords, AvgDocs, All");
			System.out.println("You provided: " + calcType);
			System.exit(0);
		}
	}
	
	public static void calcNumDocs(String retrievePath) throws IOException {
		/*
		 * Count the number of docs using the list of docnos
		 */
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(retrievePath);
		
		int numDocs = allDocnos.size();
		
		System.out.println("Total Number of Docs: " + numDocs);
	}
	
	public static void calcNumWords(String retrievePath) throws IOException {
		/*
		 * Count the number of words using the inverted Index
		 */
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		InvertedMethods.readInvertedIndex(retrievePath, invertedIndex);
		
		int numWords = invertedIndex.size();
		
		System.out.println("Total Number of Words: " + numWords);
	}
	
	public static void calcAvgDocs(String retrievePath) throws IOException {
		/*
		 * Calculate the average docs per word by iterating through the inverted index
		 */
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		InvertedMethods.readInvertedIndex(retrievePath, invertedIndex);
		int countWords = 0;
		int sumDocs = 0;
		
		for (ArrayList<Integer> wordDocs: invertedIndex) { // for each word in index
			int countDocs = wordDocs.size() / 2; // count words by dividing length by 2
			countWords++; 
			sumDocs += countDocs; // add documents for this
		}
		
		double averageDocs = (double)sumDocs / (double)countWords;
		
		System.out.println("Average Docs with Each Word: " + averageDocs);
	}
	
	public static void calcAll(String retrievePath) throws IOException {
		/*
		 * Calculate number of docs, number of words and average docs per word
		 * Iterate through inverted index, and count length of docnos list
		 */
		ArrayList<String> allDocnos = InvertedMethods.readDocnosList(retrievePath);
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		int numDocs = allDocnos.size();
		InvertedMethods.readInvertedIndex(retrievePath, invertedIndex); // read index
		
		int countWords = 0;
		int sumDocs = 0;
		
		for (ArrayList<Integer> wordDocs: invertedIndex) { // for each word in index
			int countDocs = wordDocs.size() / 2; // count words by dividing length by 2
			countWords++; 
			sumDocs += countDocs; // add documents for this
		}
		
		double averageDocs = (double)sumDocs / (double)countWords;
		
		System.out.println("Total Number of Docs: " + numDocs);
		System.out.println("Total Number of Words: " + countWords);
		System.out.println("Average Docs with Each Word: " + averageDocs);
	}
}