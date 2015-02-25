package tcrn.tbi.tm.ner;


import tcrn.tbi.tm.model.Annotation;

public class MedicationAnnotation extends Annotation {
	
	private String conceptId;
	private String conceptName;
	private String score;
	/**
	 * @return the conceptId
	 */
	public String getConceptId() {
		return conceptId;
	}
	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	/**
	 * @return the conceptName
	 */
	public String getConceptName() {
		return conceptName;
	}
	/**
	 * @param conceptName the conceptName to set
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	/**
	 * @return the score
	 */
	public String getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

}
