package tcrn.tbi.tm.model;

import java.util.List;


public class FileAnnotation extends Annotation{
	
	private String fileName;
	private List<SentenceAnnotation> sentenceAnnList;
	private List<SectionAnnotation> sectionAnnotation;

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the sentenceAnnList
	 */
	public List<SentenceAnnotation> getSentenceAnnList() {
		return sentenceAnnList;
	}

	/**
	 * @param sentenceAnnList the sentenceAnnList to set
	 */
	public void setSentenceAnnList(List<SentenceAnnotation> sentenceAnnList) {
		this.sentenceAnnList = sentenceAnnList;
	}

	/**
	 * @return the sectionAnnotation
	 */
	public List<SectionAnnotation> getSectionAnnotation() {
		return sectionAnnotation;
	}

	/**
	 * @param sectionAnnotation the sectionAnnotation to set
	 */
	public void setSectionAnnotation(List<SectionAnnotation> sectionAnnotation) {
		this.sectionAnnotation = sectionAnnotation;
	}
	

}
