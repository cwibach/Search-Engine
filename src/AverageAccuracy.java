import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AverageAccuracy {
	public static double evalAverage(String readPath) throws IOException, FileNotFoundException {
		/*
		 * Calculate average accuraccy across all queries for evalscores with a single
		 * accuracy measure only
		 */
		double totalAccuracy = 0.0;
		int queries = 0;
		
		BufferedReader resultFile = new BufferedReader(new FileReader(readPath)); //file with results
		
		@SuppressWarnings("unused") // read in and ignore header line
		String headerLine = resultFile.readLine();
		
		while (true) { // for each line in results
			String line = resultFile.readLine();
			
			if (line == null) { // if end of file, exit
				break;
			}
			
			// split by tabs
			String[] lineSplit = line.split("\t");
			String doubleString = lineSplit[lineSplit.length-1]; // get last value
			queries += 1; // increase number of queries
			totalAccuracy += Double.parseDouble(doubleString);
		}
		
		resultFile.close(); // close file
		
		double average = totalAccuracy / (double)queries; // average accuracy
		
		return average;
	}
}