package tcrn.tbi.tm.metamap;

import java.util.ArrayList;
import java.util.List;
import tcrn.tbi.tm.ner.MedicationAnnotation;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

public class MetaMapRunner {
	

	public static void main(String[] args) {
		String testText = "morphine is  a killer drug";
		try {
			List<String> theOptions = new ArrayList<>();
			theOptions.add("-R");
			theOptions.add("RXNORM");
			MetaMapRunner mmrunner = new MetaMapRunner();
			mmrunner.runMetaMap(testText,theOptions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public MedicationAnnotation runMetaMap(String lookupText,List<String> theOptions) throws Exception {
		MetaMapApi api = new MetaMapApiImpl();
		api.resetOptions();
		
		if (theOptions.size() > 0) {
			api.setOptions(theOptions);
		}
		//System.out.println("api instanciated");
		MedicationAnnotation meds = null;
		List<Result> resultList = api.processCitationsFromString(lookupText);
		Result result = resultList.get(0);
		for (Utterance utterance: result.getUtteranceList()) {
			//System.out.println(" Utterance text: " + utterance.getString());
			for (PCM pcm: utterance.getPCMList()) {
				for (Mapping map: pcm.getMappingList()) {
					//System.out.println("\n");
					for (Ev mapEv: map.getEvList()) {
						meds = new MedicationAnnotation();
						meds.setConceptId(mapEv.getConceptId());
						meds.setConceptName(mapEv.getConceptName());
						meds.setCoveredText(lookupText);
//						drug.setStartOffset(mapEv.getPositionalInfo().get(0).getX());
//						drug.setEndOffset(mapEv.getPositionalInfo().get(0).getY());						
					}
				}
			}
		}
		api.disconnect();
		return meds;
	}

}

