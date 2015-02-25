package tcrn.tbi.tm.i2b2.riskfactors;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tcrn.tbi.tm.i2b2.RiskFactorModel;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.ner.HypertensionAnnotation;
import tcrn.tbi.tm.ruta.RutaAnnotator;

public class HypertensionRiskFactor extends RiskFactorModel{
	
	private HypertensionAnnotation hypertensionAnnotation;
	
	public List<HypertensionRiskFactor> getHypertension(FileAnnotation annFile) throws Exception{
		RutaAnnotator rutaAnnotator = new RutaAnnotator();
		List<Annotation> BpvalueList = rutaAnnotator.annotate(annFile, "i2b2.riskfactor.diseasedisorder.LabValues");
		//System.out.println(BpvalueList.size());
		List<HypertensionRiskFactor> hrfList = new LinkedList<HypertensionRiskFactor>();
		for (Annotation annotation : BpvalueList) {
			//System.out.println("lab "+annotation.getCoveredText());
			String systolicBpVal = getBpVal(annotation.getCoveredText());
			int systolicBpIntVal = Integer.parseInt(systolicBpVal);
			if(systolicBpIntVal>140){
				//create hypertension annotation
				HypertensionAnnotation hyperAnn = new HypertensionAnnotation();
				hyperAnn.setCoveredText(annotation.getCoveredText());
				hyperAnn.setStartOffset(annotation.getStartOffset());
				hyperAnn.setEndOffset(annotation.getEndOffset());
				HypertensionRiskFactor hrf = new HypertensionRiskFactor();
				hrf.setTagName("HYPERTENSION");
				hrf.setIndicator("high bp");
				hrf.setTime("during DCT");
				hrf.setHypertensionAnnotation(hyperAnn);
				hrfList.add(hrf);
			}
		}
		List<Annotation> hypertensionList = rutaAnnotator.annotate(annFile, "i2b2.riskfactor.diseasedisorder.hypertension");
		//System.out.println(hypertensionList.size());
		for (Annotation annotation : hypertensionList) {
			HypertensionAnnotation hyperAnn = new HypertensionAnnotation();
			hyperAnn.setCoveredText(annotation.getCoveredText());
			hyperAnn.setStartOffset(annotation.getStartOffset());
			hyperAnn.setEndOffset(annotation.getEndOffset());
			HypertensionRiskFactor hrf = new HypertensionRiskFactor();
			hrf.setTagName("HYPERTENSION");
			hrf.setIndicator("mention");
			hrf.setTime("continuing");
			hrf.setHypertensionAnnotation(hyperAnn);
			hrfList.add(hrf);
		}
		return hrfList;
		
	}
	
	private static String getBpVal(String bpVal) {
		String systolic = bpVal.split("/")[0];
		
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(systolic);
		String bpReturnVal = "";
		while (m.find()) {
			bpReturnVal = (m.group());
		}
		return bpReturnVal;
	}

	/**
	 * @return the hypertensionAnnotation
	 */
	public HypertensionAnnotation getHypertensionAnnotation() {
		return hypertensionAnnotation;
	}

	/**
	 * @param hypertensionAnnotation the hypertensionAnnotation to set
	 */
	public void setHypertensionAnnotation(HypertensionAnnotation hypertensionAnnotation) {
		this.hypertensionAnnotation = hypertensionAnnotation;
	}

}
