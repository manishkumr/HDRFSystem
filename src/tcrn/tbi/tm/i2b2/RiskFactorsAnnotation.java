package tcrn.tbi.tm.i2b2;

import java.util.List;

import tcrn.tbi.tm.i2b2.riskfactors.FamilyHistoryRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HyperlipidemiaRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HypertensionRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.MedicationRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.SmokingHistoryRiskFactor;
import tcrn.tbi.tm.model.FileAnnotation;

public class RiskFactorsAnnotation extends FileAnnotation{
	
	private List<MedicationRiskFactor> medications;
	private SmokingHistoryRiskFactor smokingHistory;
	private FamilyHistoryRiskFactor familyHistory;
	private List<HypertensionRiskFactor> hypertension;
	private List<HyperlipidemiaRiskFactor> hyperlipidemia;

	/**
	 * @return the medications
	 */
	public List<MedicationRiskFactor> getMedications() {
		return medications;
	}

	/**
	 * @param medications the medications to set
	 */
	public void setMedications(List<MedicationRiskFactor> medications) {
		this.medications = medications;
	}

	/**
	 * @return the smokingHistory
	 */
	public SmokingHistoryRiskFactor getSmokingHistory() {
		return smokingHistory;
	}

	/**
	 * @param smokingHistory the smokingHistory to set
	 */
	public void setSmokingHistory(SmokingHistoryRiskFactor smokingHistory) {
		this.smokingHistory = smokingHistory;
	}

	/**
	 * @return the familyHistory
	 */
	public FamilyHistoryRiskFactor getFamilyHistory() {
		return familyHistory;
	}

	/**
	 * @param familyHistory the familyHistory to set
	 */
	public void setFamilyHistory(FamilyHistoryRiskFactor familyHistory) {
		this.familyHistory = familyHistory;
	}

	/**
	 * @return the hypertension
	 */
	public List<HypertensionRiskFactor> getHypertension() {
		return hypertension;
	}

	/**
	 * @param hypertension the hypertension to set
	 */
	public void setHypertension(List<HypertensionRiskFactor> hypertension) {
		this.hypertension = hypertension;
	}

	/**
	 * @return the hyperlipidemia
	 */
	public List<HyperlipidemiaRiskFactor> getHyperlipidemia() {
		return hyperlipidemia;
	}

	/**
	 * @param hyperlipidemia the hyperlipidemia to set
	 */
	public void setHyperlipidemia(List<HyperlipidemiaRiskFactor> hyperlipidemia) {
		this.hyperlipidemia = hyperlipidemia;
	}

}
