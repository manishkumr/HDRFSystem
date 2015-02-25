package tcrn.tbi.tm.i2b2.riskfactors;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tcrn.tbi.tm.i2b2.RiskFactorModel;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.ner.HyperlipidemiaAnnotation;
import tcrn.tbi.tm.ner.HypertensionAnnotation;
import tcrn.tbi.tm.ruta.RutaAnnotator;

public class HyperlipidemiaRiskFactor extends RiskFactorModel{
	private HyperlipidemiaAnnotation hlAnn;
	

	public List<HyperlipidemiaRiskFactor> getHyperlipidemia(
			FileAnnotation annFile) throws Exception {
		List<HyperlipidemiaRiskFactor> hrfList = new LinkedList<HyperlipidemiaRiskFactor>();
		RutaAnnotator rutaAnnotator = new RutaAnnotator();
		List<Annotation> hlList = rutaAnnotator.annotate(annFile, "i2b2.riskfactor.diseasedisorder.hyperlipedimia");
		for (Annotation annotation : hlList) {
			String annotatedText = annotation.getCoveredText();
			if(annotatedText.matches(".*\\d.*")){
				//get LDL val
				Integer ldlVal = getLDLval(annotatedText);
				if(ldlVal>100){
					HyperlipidemiaAnnotation hlAnn = new HyperlipidemiaAnnotation();
					hlAnn.setCoveredText(annotation.getCoveredText());
					hlAnn.setStartOffset(annotation.getStartOffset());
					hlAnn.setEndOffset(annotation.getEndOffset());
					HyperlipidemiaRiskFactor hlrf = new HyperlipidemiaRiskFactor();
					hlrf.setTagName("HYPERLIPIDEMIA");
					hlrf.setIndicator("high LDL");
					hlrf.setTime("during DCT");
					hlrf.setHlAnn(hlAnn);
					hrfList.add(hlrf);
				}
			}else{
				HyperlipidemiaAnnotation hlAnn = new HyperlipidemiaAnnotation();
				hlAnn.setCoveredText(annotation.getCoveredText());
				hlAnn.setStartOffset(annotation.getStartOffset());
				hlAnn.setEndOffset(annotation.getEndOffset());
				HyperlipidemiaRiskFactor hlrf = new HyperlipidemiaRiskFactor();
				hlrf.setTagName("HYPERLIPIDEMIA");
				hlrf.setIndicator("mention");
				hlrf.setTime("continuing");
				hlrf.setHlAnn(hlAnn);
				hrfList.add(hlrf);
			}
		}
		return hrfList;
	}

	private Integer getLDLval(String annotatedText) {
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(annotatedText);
		Integer hdlVal = null;
		while (m.find()) {
			hdlVal = Integer.parseInt((m.group()));
			
		}
		return hdlVal;
		
	}

	/**
	 * @return the hlAnn
	 */
	public HyperlipidemiaAnnotation getHlAnn() {
		return hlAnn;
	}

	/**
	 * @param hlAnn the hlAnn to set
	 */
	public void setHlAnn(HyperlipidemiaAnnotation hlAnn) {
		this.hlAnn = hlAnn;
	}
	

}
