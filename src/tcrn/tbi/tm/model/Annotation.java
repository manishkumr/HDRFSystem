package tcrn.tbi.tm.model;

public class Annotation {
	private Integer startOffset;
	private Integer endOffset;
	private String coveredText;
	/**
	 * @return the coveredText
	 */
	public String getCoveredText() {
		return coveredText;
	}
	/**
	 * @param coveredText the coveredText to set
	 */
	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}
	/**
	 * @return the startOffset
	 */
	public Integer getStartOffset() {
		return startOffset;
	}
	/**
	 * @param startOffset the startOffset to set
	 */
	public void setStartOffset(Integer startOffset) {
		this.startOffset = startOffset;
	}
	/**
	 * @return the endOffset
	 */
	public Integer getEndOffset() {
		return endOffset;
	}
	/**
	 * @param endOffset the endOffset to set
	 */
	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
	}
	
	

}
