package tcrn.tbi.tm.ner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class DiseaseDisorderNER {

	public List<MedicationAnnotation> diseaseLookup(FileAnnotation annFile) throws Exception {
		List<String> theOptions = new ArrayList<>();
		theOptions.add("-R");
		theOptions.add("SNOMECT");
		List<SentenceAnnotation> sentences = annFile.getSentenceAnnList();
		List<MedicationAnnotation> medList = new LinkedList<MedicationAnnotation>();
		for (SentenceAnnotation sentenceAnnotation : sentences) {
			List<TokenAnnotation> npTokenList = sentenceAnnotation.getNPtokens();
			for (TokenAnnotation tokenAnnotation : npTokenList) {
				String text = tokenAnnotation.getCoveredText();
				String tag = tokenAnnotation.getTags();
				//System.out.println(text+":"+tag);
			}
		}
		return null;
	}

}
