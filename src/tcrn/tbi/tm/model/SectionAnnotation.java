package tcrn.tbi.tm.model;


public class SectionAnnotation extends Annotation{
	private Integer sentenceIndex;

	/**
	 * @return the sentenceIndex
	 */
	public Integer getSentenceIndex() {
		return sentenceIndex;
	}

	/**
	 * @param sentenceIndex the sentenceIndex to set
	 */
	public void setSentenceIndex(Integer sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

}
