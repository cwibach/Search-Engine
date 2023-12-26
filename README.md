# Search-Engine
A Search Engine designed and tested with LAtimes data from 1989 and 1990, includes BM25, Cosine Similarity, and Boolean AND Search. Can evaluate TREC search results with Precision, Average Precision, and NDCG.
Originally created for a class, it was improved to fix all errors identified throughout, as well as to add the option of stemming for all searches and to allow command line searching for all search methods.

## Installation ##
Java is needed to run the program. No external libraries need to be installed.

Cloning the repository allows the programs to be run locally from the command line.

```bash
git clone https://github.com/UWaterloo-MSCI-541/msci-541-f23-hw5-cwibach.git
```

## Compiling Programs ##
All programs are inside the src folder. All java files should be compiled before running, in the following order, as some use methods defined in previous files.

``` bash
javac ResultScore.java
javac InvertedMethods.java
javac MeasureCalculations.java
javac MemoryCalculations.java
javac PorterStemmer.java
javac AverageAccuracy.java
javac GetFile.java
javac IndexEngine.java
javac EvalScore.java
javac BooleanAND.java
javac BM25.java
javac CosineSearch.java
javac ParameterSweep.java
javac SearchBM25.java
javac SearchBAND.java
javac SearchCosine.java
```

## Running Key Programs ##
For running search from the command line, only two of the programs need to be run: IndexEngine.java & the chosen searching program (SearchBM25.java, SearchBAND.java, or SearchCosine.java), in this order
For running the program, it is best to navigate to the same folder as for compiling the programs, "src".


### IndexEngine.java ###
IndexEngine needs a compressed .gz file to run and create the inverted index. It was created with LAtimes data for testing, but this was only used for the course and is not able to be shared. If you wish to test the code, it is recommended that an appropriate file with documents for searching is obtained first. This program does the inversion in memory, and is designed for collections of around 100-200 thousand documents, more and it may cause issues due to memory restrictions and take a very long time.

IndexEngine.java takes 2 to 4 arguments, any less than 2 and the program will inform the user of the error, the third and fourth arguments are optional parameters.
1. The compressed .gz file to read in's location, file must exist and be in .gz format
2. The directory to create all documents and files in, directory must not already exist
3. The word "short" if only a small subset of files should be used (case-insensitive), any other value will be ignored. It is recommended NOT to shorten the search
4. The word "stem" to use the porter stemmer when creating the datastructures (case-insensitive), any other input will be ignored. It is recommended NOT to stem for the search
   
If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes missing files or pre-existing file directories. The paths used may not work on your computer, so please ensure the directories and file names match what is used on your device.

```bash
java IndexEngine.java "C:\Users\carte\Documents\Search Engines\latimes.gz" "C:\Users\carte\Documents\Search Engines\latimes docs"
```

or

``` bash
java IndexEngine.java "C:\Users\carte\Documents\Search Engines\latimes.gz" "C:\Users\carte\Documents\Search Engines\short latimes docs" short stem
```

This program takes some time to run, so please be patient as it has to go through the full document creating many documents and folders.

### SearchBM25.java, SearchBAND.java, SearchCosine.java ###
These programs take 1 argument only, this is the directory where all files are created with the inverted index (same as 2nd arg in IndexEngine.java) Any less and the user will be informed of the error.

``` bash
java SearchBM25.java "C:\users\carte\documents\search engines\latimes docs"
```

Once the program is running, it will take a few seconds to construct the necessary data from the stored index, then it will prompt the user for a search query. 
Once receiving a search query it will provide the top 10 (or less, if less than 10 results exist) for the query, with headline, date, and docno of each, as well as a query-biased snippet to inform the user of the document contents.

```bash
1. SHORT TAKES; CRONKITE FOCUSES ON DINOSAURS; (September 10, 1990)
Cronkite, 73, spoke with owners of the Bynum Rock Shop, who found the bones of baby dinosaurs that led to discovery of a key fossil site. Former CBS anchorman Walter Cronkite says he has something in common with every "red-blooded human being" -- a fascination with dinosaurs. (LA091090-0132)
```

After the search is returned the user is provided 3 options:
1. Type an integer from 1 to 10 to get that full document and metadata returned to the console
2. Type 'N' to perform a new query
3. Type 'Q' to exit the program

These are all case-insensitive, and the system will help the user if they mistype or forget. After retrieving one of the docs they will be given the opportunity to choose another option.
If the user types a query with no results, or with no query terms, an error message will inform them and they will be prompted for a new query or to continue otherwise.


## Other Programs ##
Other programs are also included due to having pre-created methods, and allowing more future functionality if desired.
These are also located in the "src" folder and some have prerequisites as outlined below.
GetFile.java: Needs IndexEngine.java
BM25.java: Needs IndexEngine.java & a file with search queries
EvalScore.java: Needs BM25.java & a file with qrels (relevancy scores)

### GetFile.java ###
GetFile.java takes 3 arguments, any less and the program will inform the user of the error, any more and the extras will be ignored.
1. The folder location with all the created folders and docs (should be the same as the 2nd argument in IndexEngine.java)
2. What type of id is provided, either "id" or "docno" (case-insensitive)
3. The id provided which should be a numeric id, or a docno for a specific document

If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes errors such as invalid ids, or invalid file paths. The paths used may not work on your computer, so please ensure the directories and file names match what is used on your device.

```bash
java GetFile.java "C:\Users\carte\Documents\Search Engines\latimes docs" id 35313

java GetFile.java "C:\Users\carte\Documents\Search Engines\latimes docs" docno LA030290-0001
```

This program will return the metadata (id, docno, date, headline) as well as the full document (including all tags) to the command line

### BooleanAND.java ###
BooleanAND.java takes 3 or 4 arguments, the first 3 are required and will result in errors if not provided, the 4th is optional.
1. The folder location with the created inverted index & lexicon
2. The file location with the search terms
3. The file location to write search results to
4. The word "stem" if the search query should be stemmed (case-insensitive)

If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes error such as invalid folder or file paths. The paths used as samples may not work on your computer, so please ensure the directories and file names match what is used on your device.

```bash
java BooleanAND.java "C:\Users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\BAND-results.txt"

java BooleanAND.java "C:\Users\carte\Documents\Search Engines\stem latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\BAND-results-stem.txt" STEM
```
Search results are written to the provided file name in the same folder as the found inverted index and lexicon. They are written in the TREC file format, with each line of results consisting of: Query id, Q0, Docno, Rank, Score, and useridAND, where the userid is my student WatIam login. The ranks increase by 1, and the scores decrease by 1 with each result for a query, with the first being ranked 1, and the last being scored 1.

### BM25.java ###
BM25.java takes 3 to 6 arguments, the first 3 are required and will result in errors if not provided, the 4th through 6th are optional.
1. The folder location with the created inverted index & lexicon
2. The file location with the search terms
3. The file location to write search results to
4. The k1 value to use (any double), will be ignored if not numeric
5. The b value to use (any double from 0 to 1), will be ignored if not numeric or invalid
6. The word "stem" if the search query should be stemmed (case-insensitive)

If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes error such as invalid folder or file paths. The paths used as samples may not work on your computer, so please ensure the directories and file names match what is used on your device.

```bash
java BM25.java "C:\Users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\BM25-default-results.txt"

java BM25.java "C:\Users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\BM25-k1-1.3-b-0.6-results.txt" 1.3 0.6

java BM25.java "C:\Users\carte\Documents\Search Engines\stem latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\BM25-default-stem-results.txt" null null stem
```
Search results are written to the provided file name in the same folder as the found inverted index and lexicon. They are written in the TREC file format, with each line of results consisting of: Query id, Q0, Docno, Rank, Score, and useridAND, where the userid is my student WatIam login. The ranks increase by 1, and the scores decrease by 1 with each result for a query, with the first being ranked 1, and the last being scored 1.

### CosineSearch.java ###
CosineSearch.java takes 3 or 4 arguments, the first 3 are required and will result in errors if not provided, the 4th is optional.
1. The folder location with the created inverted index & lexicon
2. The file location with the search terms
3. The file location to write search results to
4. The word "stem" if the search query should be stemmed (case-insensitive)

If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes error such as invalid folder or file paths. The paths used as samples may not work on your computer, so please ensure the directories and file names match what is used on your device.

```bash
java CosineSearch.java "C:\Users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\cosine-results.txt"

java BooleanAND.java "C:\Users\carte\Documents\Search Engines\stem latimes docs" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\results\cosine-results-stem.txt" STEM
```
Search results are written to the provided file name in the same folder as the found inverted index and lexicon. They are written in the TREC file format, with each line of results consisting of: Query id, Q0, Docno, Rank, Score, and useridAND, where the userid is my student WatIam login. The ranks increase by 1, and the scores decrease by 1 with each result for a query, with the first being ranked 1, and the last being scored 1.

### EvalScore.java ###
EvalScore.java takes 4 or more arguments, the first 3 should always follow the same order, then any past there can be entered in any order.
1. The location of the TREC format results file to read in
2. The location of the QRELS or relevancy file to read in
3. The location to write the results of the accuracy measure to
4. This argument and any more are what accuracy measures should be used. Only the ones included as arguments will be used in the file, always in the order shown below. The options are (case-insensitive):
   1. ap: Average Precision
   2. p_10: Precision at 10
   3. ndcg_10: NDCG at 10
   4. ndcg_1000: NDCG at 1000
   5. all: All of the above measures. This overwrites any other arguments, so for all measures, only provide this argument.
   
If there is user input error, an error message will be presented to the user indicating the problem with their input as well as showing what they inputted that caused the problem. This includes missing files or pre-existing file directories. The paths used may not work on your computer, so please ensure the directories and file names match what is used on your device.
If the TREC file provided is improperly formatted, an error message will indicate the line that was improperly formatted, as well as a message indicating that there was improper format. 
This formatting checks for:
   1. The correct number of columns
   2. The topic is an integer in the 1st column
   3. The rank is an integer in the 4th column
   4. The score is a number in the 5th column
  
```bash
java EvalScore.java "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\results-files\student1.results" "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\qrels\qrels.txt" "C:\Users\carte\Documents\Search Engines\hw3\test1.txt" ap ndcg_1000

java EvalScore.java "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\results-files\msmuckerAND.results" "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\qrels\qrels.txt" "C:\Users\carte\Documents\Search Engines\hw3\test1.txt" p_10

java EvalScore.java "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\results-files\student11.results" "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\qrels\qrels.txt" "C:\Users\carte\Documents\Search Engines\hw3\test1.txt" all
```

### MemoryCalculations.java ###
This program provides numeric values so Memory usage when running program can be calculated. It takes 2 arguments, which are both mandatory.
1. The value(s) to print, must be one of the following options: NumDocs, NumWords, AvgDocs, All (case-insensitive)
2. The folder location with the created inverted index & lexicon

If there are errors in input, such as none of the calculation types being provided or the file location not existing, the program will print an error message and exit.

The options for values to return work as following:
NumDocs: Prints the total number of documents in the collection
NumWords: Prints the total number of unique words/terms/tokens in the collection
AvgDocs: Prints the average number of docs containing each word (avg length of inverted index / 2)
All: Prints all 3 of the above values

These are all printed to the command line on their own line with phrasing the user understands what each value is.

``` bash
java MemoryCalculations.java all "C:\Users\carte\Documents\Search Engines\latimes docs"

java MemoryCalculations.java AvgDocs "C:\Users\carte\Documents\Search Engines\stem latimes docs"
```
### ParameterSweep.java ###
This program performs a sweep of BM25 parameter values for b-values from 0 to 1 on intervals of 0.05, and k1 values from 0 to 10 on intervals of 0.1. It measures the NDCG@10 for each run and averages the accuracy for each run in one output file as well as producing all of the result files and evaluation files. It takes 4 mandatory arguments, and up to 6 optional ones
1. The path for the inverted index to use for the searching
2. The qrels file to judge accuracy with
3. The queries file to perform searches with
4. The base path to construct new files in
5. The k1 value to start at, requires double value, defaults to 0.0 if invalid or missing
6. The b value to start at, requires double value between 0 and 1, defaults to 0.0 if invalid or missing
7. The k1 value to end at, requires double value, defaults to 10.0 if invalid or missing
8. The b value to end at, requires double value between 0 and 1, defaults to 1.0 if invalid or missing
9. The increment to increase k1 by, requires double value, defaults to 0.1 if invalid or missing
10. The increment to increase b by, requires double value, defaults to 0.05 if invalid or missing

If there are errors in input such as insufficient arguments or missing files, the program will print an error and exit.

``` bash
java ParameterSweep.java "C:\users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\qrels\qrels.txt" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\sweep test"

java ParameterSweep.java "C:\users\carte\Documents\Search Engines\latimes docs" "C:\Users\carte\Documents\Search Engines\hw3\hw3-files-2023\qrels\qrels.txt" "C:\Users\carte\Documents\Search Engines\SearchQueriesFull.txt" "C:\Users\carte\Documents\Search Engines\hw4\sweep test" 1 0.3 4 0.8 0.5 0.02
```
This code takes a very long time to run (>60 minutes), so be warned, or change starting/ending b and k1 values before running.

### InvertedMethods.java ###
This program is not meant to be run from the command line and only includes methods used for the construction and use of the inverted index and lexicon.

### ResultScore.java ###
This program is not meant to be run from the command line and only includes a class for tracking document docnos and scores.

### MeasureCalculations.java ###
This program is not meant to be run from the command line and only includes methods used for the calculations of evaluation scores.

### PorterStemmer.java ###
This program is not meant to be run from the command line and only includes methods for stemming tokens with the porter stemmer.

### AverageAccuracy.java ###
This program is not meant to be run from the command line and only includes methods for calculating average accuracy from an evaluation file
