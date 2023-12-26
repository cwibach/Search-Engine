import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GetFile {
	public static void main(String[] args) throws FileNotFoundException, IOException{
		// retrieve file from id given
		
		int numArgs;
		String retrievePath;
		String idType;
		String idGiven;
		
		numArgs = args.length;
		
		// ensure correct arguments given
		if (numArgs < 3) {
			System.out.println("Insufficent arguments provided.");
			System.out.println("Plese provide 3, you provided: " + numArgs);
			for (int i=0; i<numArgs; i++) {
				System.out.println("Argument " + (i+1) + ": " + args[i]);
			}
			System.exit(0);
		}
		
		retrievePath = args[0];
		idType = args[1].toLowerCase();
		idGiven = args[2];
		
		// retrieve file
		String resultString = retrieveFile(retrievePath, idType, idGiven);
		
		System.out.print(resultString);
	}
	
	public static String retrieveFile(String retrievePath, String idType, String idGiven) 
			throws FileNotFoundException, IOException{
		// retrieve the file from the designated path
		
		String docno = null;
		
		// if it is type id
		if (idType.equals("id")) {
			try {
				int tempId = Integer.parseInt(idGiven);
				// get the id and ensure it is positive integer
				if (tempId < 0) {
					System.out.println("Error: Invalid id given");
					System.out.println("Id must be a positive integer");
					System.out.println("Id given was: " + idGiven);
					System.exit(0);
				}
			} catch (Exception e) {
				System.out.println("Error: Invalid id given");
				System.out.println("Id must be a positive integer");
				System.out.println("Id given was: " + idGiven);
				System.exit(0);
			}
			
			// get the docno from the id
			docno = getDocNo(idGiven, retrievePath);
			
		} else if (idType.equals("docno")) {
			// if its docno use what's given
			docno = idGiven;
			
		} else {
			// inform user of invalid id type
			System.out.println("Invalid ID Type given: " + idType);
			System.out.println("Please use 'id' or 'docno'");
			System.exit(0);
		}
		
		if (!IndexEngine.pathExists(retrievePath)) {
			// ensure path exists to retrieve files from
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + retrievePath);
			System.exit(0);
		}
		
		// parse the date
		String[] date = IndexEngine.parseDocno(docno);
		
		// get file paths for metadata and info
		String datePath = "\\" + date[0] + "\\" + date[1] + "\\" + date[2] + "\\";
		String metaDataPath = retrievePath + "\\metadata" + datePath + docno + ".txt";
		String docPath = retrievePath + "\\fulldocs" + datePath + docno + ".txt";
		
		// https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
		File metaData = null;
		File fullDoc = null;
		BufferedReader metaDataReader = null;
		BufferedReader fullDocReader = null;
		
		// get files for metadata and full document
		try {
			metaData = new File(metaDataPath);
			fullDoc = new File(docPath);
			metaDataReader = new BufferedReader(new FileReader(metaData));
			fullDocReader = new BufferedReader(new FileReader(fullDoc));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Could not find files requested");
			System.out.println("Please ensure docno exists in files");
			System.out.println("Docno provided: " + docno);
			System.exit(0);
		}
		
		String fullResult = "";
		
		// add info from metadata to string result
		for (int i=0;i<4;i++) {
			String line = metaDataReader.readLine();
			fullResult = fullResult + line + "\n";
		}
		metaDataReader.close();
		
		// add full document to string result
		fullResult = fullResult + "Full Document:\n";
	
		while (true) {
			String line = fullDocReader.readLine();
			
			if (line == null) {
				break;
			}
			
			fullResult = fullResult + line;
		}
		fullDocReader.close();
		
		return fullResult;
	}
	
	public static String[] retrieveFileParts(String retrievePath, String docno) throws IOException {
		
		if (!IndexEngine.pathExists(retrievePath)) {
			// ensure path exists to retrieve files from
			System.out.println("Error: Path to retrieve files does not exist");
			System.out.println("Please provide correct path to files, or use IndexEngine to create");
			System.out.println("Path provided: " + retrievePath);
			System.exit(0);
		}
		
		// parse the date
		String[] date = IndexEngine.parseDocno(docno);
		
		// get file paths for metadata and info
		String datePath = "\\" + date[0] + "\\" + date[1] + "\\" + date[2] + "\\";
		String metaDataPath = retrievePath + "\\metadata" + datePath + docno + ".txt";
		String docPath = retrievePath + "\\fulldocs" + datePath + docno + ".txt";
		
		// https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
		File metaData = null;
		File fullDoc = null;
		BufferedReader metaDataReader = null;
		BufferedReader fullDocReader = null;
				
		// get files for metadata and full document
		try {
			metaData = new File(metaDataPath);
			fullDoc = new File(docPath);
			metaDataReader = new BufferedReader(new FileReader(metaData));
			fullDocReader = new BufferedReader(new FileReader(fullDoc));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Could not find files requested");
			System.out.println("Please ensure docno exists in files");
			System.out.println("Docno provided: " + docno);
			System.exit(0);
		}		
		
		// get internal id, docno, dateline and headline
		@SuppressWarnings("unused")
		String internalId = metaDataReader.readLine();
		@SuppressWarnings("unused")
		String foundDocno = metaDataReader.readLine();
		String dateLine = metaDataReader.readLine();
		String headline = metaDataReader.readLine();
		metaDataReader.close();
		
		// get date and headline values
		dateLine = dateLine.substring(6);
		headline = headline.substring(10);
		
		// read in rest of text
		String fullResult = "";
		while (true) {
			String line = fullDocReader.readLine();
			
			if (line == null) {
				break;
			}
			
			fullResult = fullResult + line;
		}
		fullDocReader.close();
		
		// make array for file parts and return it
		String[] fileParts = new String[3];
		fileParts[0] = dateLine;
		fileParts[1] = headline;
		fileParts[2] = fullResult;
		return fileParts;
	}
	
	public static String getDocNo(String idGiven, String retrievePath) throws FileNotFoundException, IOException {
		// get docno string from the id given and the path with files
		
		int id = Integer.parseInt(idGiven);
		
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
		
		// make list of all docnos
		ArrayList<String> docnos = new ArrayList<String>();
		
		for (int i = 0; i <= id; i++) {
			String line = reader.readLine();
			
			if (line == null) {
				break;
			}
			
			docnos.add(line);
		}
		
		reader.close();
		
		// if id is too large
		if (id > docnos.size()) {
			System.out.println("Invalid ID provided, too high");
			System.out.println("Max ID is: " + docnos.size());
			System.out.println("You provided ID: " + id);
			System.exit(0);
		}
		
		// get value at id index
		return docnos.get(id);
	}
}
