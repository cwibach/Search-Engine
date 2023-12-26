import java.util.ArrayList;
import java.util.HashSet;
import java.text.DecimalFormat;

public class MeasureCalculations {
	// https://mkyong.com/java/java-display-double-in-2-decimal-points/
	private static final DecimalFormat df = new DecimalFormat("0.00000");
	
	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
	
	public static String averagePrecision(int maxIndex, HashSet<String> searchResults, ArrayList<ResultScore> userResults) {
		/*
		 * Calculate average precision given:
		 * 	The maximum index to count until
		 * 	The set of relevant results
		 * 	The list of returned results in order
		 */
		
		int numRelevant = searchResults.size(); // number of relevant results
		int relevantFound = 0; // number of relevant found
		double precision = 0.0; // precision so far
		double sumPrecisions = 0.0; // sum of precisions at relevant so far
		
		for (int i = 1; i <= maxIndex; i++) { // for each result until max index
			if (i > userResults.size()) { // if the end of results is found
				break; // exit
			}
			
			String result = userResults.get(i - 1).getDocno(); // get the docno
			
			if (searchResults.contains(result)) { // if it is relevant
				relevantFound++; // increase number found
				precision = (double)relevantFound / (double)i; // calculate precision
				sumPrecisions += precision; // add precision to sum
			}
		}
		
		// calculate average precision
		double avgPrecision = sumPrecisions / (double) numRelevant;
		
		return df.format(avgPrecision); // return formatted acuracy
	}
	
	public static String precision(int maxIndex, HashSet<String> searchResults, ArrayList<ResultScore> userResults) {
		/*
		 * Calculate precision at a certain index given:
		 * 	The index to measure at
		 * 	The set of relevant results
		 * 	The list of returned results in order
		 */
		
		int relevantCount = 0; // number relevant found
		double precision = 0.0; // precision so far
		
		for (int i = 0; i < maxIndex; i++) { // for each result in order up to max
			
			if (i == userResults.size()) { // if end of results found
				precision = (double)relevantCount / (double)i; // calculate precision
				return df.format(precision); // return formatted precision
			}
			
			String result = userResults.get(i).getDocno(); // get docno
			if (searchResults.contains(result)) { // check if relevant
				relevantCount++; // increment relevant number if so
			}
		}
		
		precision = (double)relevantCount / (double)maxIndex; // calculate precision
		return df.format(precision); // return formatted precision
	}
	
	public static String ndcg(int maxIndex, HashSet<String> searchResults, ArrayList<ResultScore> userResults) {
		/*
		 * Calculate NDCG accuracy given:
		 * 	The maximum index to count until
		 * 	The set of relevant results
		 * 	The list of returned results in order
		 */
		
		int relevantCount = Math.min(searchResults.size(), maxIndex); // get relevant number up to max index
		double idealDCG = 0.0; // value for ideal DCG
		double actualDCG = 0.0; // value for actual DCG
		
		for (int i = 0; i < maxIndex; i++) { // up until the max index
			double indexValue = 1.0 / (Math.log((double)(i+2)) / Math.log(2.0));
			
			if (i < relevantCount) { // if under the max number relevant
				idealDCG += indexValue; // increase the indexValue
			}
			
			if (i < userResults.size()) { // if within the size of results
				String result = userResults.get(i).getDocno(); // get docno
				
				if (searchResults.contains(result)) { //if relevant
					actualDCG += indexValue; //increase actual value
				}
			}
		}
		
		// normalize accuracy
		double ndcg = actualDCG/idealDCG;
		
		return df.format(ndcg); //return formatted accuracy
	}
}