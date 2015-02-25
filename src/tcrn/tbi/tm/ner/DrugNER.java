package tcrn.tbi.tm.ner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tcrn.tbi.tm.metamap.MetaMapRunner;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class DrugNER {
	
	private static final Logger logger = LogManager.getLogger(DrugNER.class.getName());
	
	public List<MedicationAnnotation> drugLookup(FileAnnotation annFile) throws Exception {
		List<String> theOptions = new ArrayList<>();
		theOptions.add("-R");
		theOptions.add("RXNORM");
		List<SentenceAnnotation> sentences = annFile.getSentenceAnnList();
		List<MedicationAnnotation> medList = new LinkedList<MedicationAnnotation>();
		for (SentenceAnnotation sentenceAnnotation : sentences) {
			//logger.debug("Sentence: " + sentenceAnnotation.getCoveredText());
			for (TokenAnnotation tokenAnnotation : sentenceAnnotation.getTokenList()) {
				if(tokenAnnotation.getTags().equals("B-NP")){
					//System.out.print(tokenAnnotation.getCoveredText()+" : "+tokenAnnotation.getTags());
					MetaMapRunner mmrunner = new MetaMapRunner();
					MedicationAnnotation meds = mmrunner.runMetaMap(tokenAnnotation.getCoveredText(), theOptions);
					if(meds!=null){
						Integer drugStartOffset = sentenceAnnotation.getStartOffset()+tokenAnnotation.getStartOffset();
						Integer drugEndOffset = sentenceAnnotation.getStartOffset()+tokenAnnotation.getEndOffset();
						meds.setStartOffset(drugStartOffset);
						meds.setEndOffset(drugEndOffset);
						//logger.debug("Token: "+tokenAnnotation.getCoveredText()+drugStartOffset+":"+drugEndOffset+" Dict: "+meds.getCoveredText());
						medList.add(meds);
					}else{
						//logger.debug("Token: "+tokenAnnotation.getCoveredText()+" Dict: "+null);
					}
				}
			}
			//System.out.println();
//			for (TokenAnnotation tokenAnnotation : npTokens) {
//				System.out.println("npTokens : "+tokenAnnotation.getCoveredText());
//			}
		}
		return medList;
	}

}
