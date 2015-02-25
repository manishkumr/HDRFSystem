package tcrn.tbi.tm.i2b2.riskfactors;

import tcrn.tbi.tm.i2b2.RiskFactorModel;
import tcrn.tbi.tm.model.FileAnnotation;

public class FamilyHistoryRiskFactor extends RiskFactorModel{
	
	public FamilyHistoryRiskFactor getFamilyHistory(FileAnnotation annFile){
		FamilyHistoryRiskFactor fhRiskFactor = new FamilyHistoryRiskFactor();
		fhRiskFactor.setIndicator("not present");
		return fhRiskFactor;
	}

}
