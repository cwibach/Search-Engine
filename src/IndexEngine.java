import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet; // needed for shortened section for testing
import java.util.ArrayList;

public class IndexEngine {
	public static void main(String[] args) throws IOException, FileNotFoundException {
		// TODO: Add vector length counting for Cosine Similarity
		// TODO: Allow Stemming option for all searching
		// TODO: Allow Command Line Searching for BooleanAND and Cosine
		
		
		/* Take in location of file and location to write files to
		 * New location must not already exist
		 * Create files for full docs and for docs metadata
		 * Create file with all docnos
		 */
		int numArgs = args.length; // check arguments provided
		
		if (numArgs < 2) { // if to few, notify user and exit
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 2, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		// set paths to retrieve and write to/from
		String retrievePath = args[0];
		String writeDestination = args[1];
		boolean stemming = false;
		
		String specialInput = null;
		
		if (numArgs > 2) { // argument for shortened data
			specialInput = args[2];
		}
		
		if (numArgs > 3) { //argument for stemming
			if (args[3].equalsIgnoreCase("Stem")) {
				stemming = true; //check if stemming enabled
			} else {
				System.out.println("3rd Argument ineffective");
				System.out.println("For stemming please use argument 'Stem'");
				System.out.println("You provided Argument: " + args[2]);
				System.out.println("Continuing without stemming");
			}
		}

		
		// https://www.educative.io/answers/how-to-check-if-a-file-or-folder-exists-in-java
		if (pathExists(writeDestination)) { // if path already exists for new files
			System.out.println("Directory for new files already exists.");
			System.out.println("Please provide a directory that does not exist.");
			System.out.println("You provided: " + writeDestination);
			System.exit(0);
		}
		
		// make folders to write everything into
		File baseDir = new File(writeDestination);
		baseDir.mkdirs();	
		
		InputStream fileStreamer = null;
		InputStream gzipStreamer = null;
		Reader decoder = null;
		BufferedReader buffered = null;
		
		try {
			// https://stackoverflow.com/questions/1080381/gzipinputstream-reading-line-by-line
			// create reader for gzip file provided
			fileStreamer = new FileInputStream(retrievePath);
			gzipStreamer = new GZIPInputStream(fileStreamer);
			decoder = new InputStreamReader(gzipStreamer, "US-ASCII");
			buffered = new BufferedReader(decoder);
		} catch (FileNotFoundException e) {
			System.out.println("Error: .gz file not found");
			System.out.println("Please ensure .gz file exists and path is correct");
			System.out.println("File provided: " + retrievePath);
			System.exit(0);
		}
		
		HashMap<String, String> months = getMonthsMap();
		
		
		// hashset of docnos for short version of indexes
		HashSet<String> shortDocnos = null; 
		
		if (specialInput.equalsIgnoreCase("short")) {
			shortDocnos = new HashSet<String>();
			shortDocnos.add("LA040289-0005");
			shortDocnos.add("LA040289-0006");
			shortDocnos.add("LA040289-0007");
			shortDocnos.add("LA040289-0008");
			shortDocnos.add("LA040289-0009");
			shortDocnos.add("LA040289-0010");
			shortDocnos.add("LA040289-0022");
		}

		// create new file for docnos
		FileWriter docNos = new FileWriter(writeDestination + "\\alldocnos.txt");
		
		// create new file for docnos
		FileWriter docLengths = new FileWriter(writeDestination + "\\alldoclengths.txt");
		
		int internalID = 0; // internal id incrementing up from 0
		
		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
		HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
		
		// Switch to arraylist
		ArrayList<ArrayList<Integer>> invertedIndex = new ArrayList<ArrayList<Integer>>();
		
		while (true){ // until told to break
			String line = buffered.readLine();
			
			if (line == null) {
				break; // if no more lines in document, break
			} else {
				line = line.toLowerCase(); //convert to lower case
			}
			
			if (line.toLowerCase().contains("<doc>")) { // if it starts a document
				String docString = ""; //begin new string
				
				while (line.toLowerCase().indexOf("</doc>") == -1){ // until the end of doc
					docString  = docString + line; // add to string
					line = buffered.readLine(); //read next line
				}
				docString  = docString + line; // add final line
				
				// get metadata from full string
				String docnoString = getMetaData("docno", docString);
				String docHeadline = getMetaData("headline", docString);
				String docText = getMetaData("text", docString);
				String docGraphic = getMetaData("graphic", docString);
				docHeadline = docHeadline.replace("<P>", "");
				docHeadline = docHeadline.replace("</P>", "");
				
				// way to shorten program with special input
				if (specialInput.equalsIgnoreCase("short")){
					if (!shortDocnos.contains(docnoString)) { // if not in small set
						continue; // go to next doc
					}
				}
				
				// get array of [yy/mm/dd] for date of document
				String[] date = parseDocno(docnoString);
				String wordMonth = months.get(date[1]);
				String dateString = wordMonth + " " + date[2] + ", 19" + date[0];
				
				// get the path for files: providedpath\(docs or metadata)\yy\mm\dd\docno.txt
				String datePath = "\\" + date[0] + "\\" + date[1] + "\\" + date[2] + "\\";
				
				String dirPath = writeDestination + "\\fulldocs" + datePath;
				String fullDocPath = dirPath + docnoString + ".txt";
				
				String dirPathMetaData = writeDestination + "\\metadata" + datePath;
				String fullMetaDataPath = dirPathMetaData + docnoString + ".txt";
				
				// Assemble string for metadatadoc
				String metaDataText = "Internal ID: " + internalID + "\n";
				metaDataText = metaDataText + "Docno: " + docnoString + "\n";
				metaDataText = metaDataText + "Date: " + dateString + "\n";
				metaDataText = metaDataText + "Headline: " + docHeadline + "\n";
				
				// https://www.programiz.com/java-programming/examples/create-and-write-to-file
				if (!pathExists(dirPath)) { // if folders not fully created yet
					// make file structure (yy/mm/dd)
					File newDir = new File(dirPath);
					newDir.mkdirs(); 
					
					File newDirMetaData = new File(dirPathMetaData);
					newDirMetaData.mkdirs();
				}
				
				// write doc and metadata to files in appropriate directories
				writeNewFile(fullDocPath, docString);
				writeNewFile(fullMetaDataPath, metaDataText);
				
				docNos.write(docnoString + "\n"); // add docno to file of all docnos in order
				
				// Assemble string to tokenize
				String tokenizeString = InvertedMethods.getDocString(docHeadline, docGraphic, docText);
				
				// get list of tokens
				String[] docTokens = InvertedMethods.tokenize(tokenizeString, stemming);
				
				// get token ids from list of tokens
				int[] tokenIDs = InvertedMethods.TokentoIDs(docTokens, lexicon, invertedIndex);
				
				// write length of doc to file
				int lengthOfDoc = tokenIDs.length;
				docLengths.write(lengthOfDoc + "\n");
				
				// count words
				HashMap<Integer, Integer> wordCounts = InvertedMethods.CountWords(tokenIDs);
				
				// add to inverted methods
				InvertedMethods.AddtoPostings(wordCounts, internalID, invertedIndex);
				
				internalID++; // increase internal id
				
//				if (internalID > 200) { // simple way to limit size for testing
//					
//					break;
//				}
			}
		}
		
		// create reverse lexicon
		for (String token: lexicon.keySet()) {
			reverseLexicon.put(lexicon.get(token), token);
		}
		
		// create new file for lexicon
		FileWriter writeLexicon = new FileWriter(writeDestination + "\\lexicondoc.txt");
		
		// write lexicon to file
		for (int i = 0; i < lexicon.keySet().size(); i++) {
			writeLexicon.write(reverseLexicon.get(i) + "\n");
		}
		
		// create new file for invertedIndex
		FileWriter writeInvertedIndex = new FileWriter(writeDestination + "\\invertedindex.txt");
		
		// write inverted index to file
		for (int i=0; i < invertedIndex.size(); i++) {
			ArrayList<Integer> tempArray = invertedIndex.get(i);
			int size = tempArray.size();
			writeInvertedIndex.write(size + "\n");
			for (int j=0; j <size; j++) {
				writeInvertedIndex.write(tempArray.get(j) + "\n");
			}
		}
		
		// close all writers and buffers
		docNos.close();
		docLengths.close();
		writeLexicon.close();
		writeInvertedIndex.close();
		buffered.close();
		System.out.println("Success!");
	}
	
	public static boolean pathExists(String directory) {
		/* Check if a path exists for a given directory
		 *  Return true or false accordingly
		 */
		Path writePath = Paths.get(directory);
		
		return Files.exists(writePath);
	}
	
	public static String[] parseDocno(String docno) {
		/* Get the date of file from the docno
		 * Return string used in file locations
		 */
		try {
			String month = docno.substring(2, 4);
			String day = docno.substring(4,6);
			String year = docno.substring(6,8);
			if (day.charAt(0) == '0') {
				day = day.substring(1);
			}
			
			String[] date = {year, month, day};
			return date;
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Error: Docno is invalid");
			System.out.println("Please provide a valid docno");
			System.out.println("Docno provided: " + docno);
			System.exit(0);
		}
		return null;
	}
	
	public static String getMetaData(String tag, String fullText) {
		/* Get metadata for any tag in the text
		 * Return metadata inside tags
		 */
		
		String tempFull = fullText.toLowerCase();
		if (!tempFull.contains(tag)) {
			return ("No " + tag + " for this document");
		}
		
		int startIndex = tempFull.indexOf("<" + tag + ">") + 2 + tag.length();
		int endIndex = tempFull.indexOf("</" + tag + ">");
		
		if (endIndex == -1) {
			return ("No " + tag + " for this document");
		}
		
		String subString = fullText.substring(startIndex, endIndex).trim();
		return subString;
	}
	
	public static HashMap<String, String> getMonthsMap(){
		// hashmap for months from numbers
		
		HashMap<String, String> months = new HashMap<String, String>();
		months.put("01", "January");
		months.put("02", "February");
		months.put("03", "March");
		months.put("04", "April");
		months.put("05", "May");
		months.put("06", "June");
		months.put("07", "July");
		months.put("08", "August");
		months.put("09", "September");
		months.put("10", "October");
		months.put("11", "November");
		months.put("12", "December");
		
		return months;
	}
	
	public static void writeNewFile(String destination, String contents) throws IOException {
		/* Create and write to a file with given destination and text
		 * Return nothing
		 */
		FileWriter newWriter = new FileWriter(destination);
		newWriter.write(contents);
		newWriter.close();
	}
}



