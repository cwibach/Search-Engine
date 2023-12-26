public class ResultScore{
	/*
	 * Class for tracking Results by Docno and accuracy score for measuring accuracy 
	 */
	private double score;
	private String docno;
	
	public ResultScore(double score, String docno) {
		this.score = score;
		this.docno = docno;
	}
	
	public double getScore() {
		/*
		 * Retrieve the score
		 */
		return this.score;
	}
	
	public String getDocno() {
		/*
		 * Retrieve the docno
		 */
		return this.docno;
	}
	
	public boolean compare(ResultScore other) {
		/*
		 * Determine if this is greater than another ResultScore
		 * Return true if this is greater
		 * Return false if the other is greater
		 */
		
		// Get the other score and docno
		double otherScore = other.getScore();
		String otherDocno = other.getDocno();
		
		if (otherScore > this.score) { // if the other has a greater score
			return false;
			
		} else if (otherScore < this.score) { // if this has a greater score
			return true;
			
		} else { // otherScore == this.score
			
			// compare docnos instead
			if (otherDocno.compareTo(this.docno) > 0) {
				return false;
			} else { // otherDocno <= this.docno
				return true;
			}
		}
	}
}