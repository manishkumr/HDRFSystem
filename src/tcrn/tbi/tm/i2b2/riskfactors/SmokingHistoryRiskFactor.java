package tcrn.tbi.tm.i2b2.riskfactors;

import java.util.LinkedList;
import java.util.List;

import cc.mallet.types.Instance;
import tcrn.tbi.tm.i2b2.RiskFactorModel;
import tcrn.tbi.tm.mallet.MalletClassLabelTagger;
import tcrn.tbi.tm.mallet.MalletInstanceGenerator;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;
import tcrn.tbi.tm.ner.SmokerAnnotation;

public class SmokingHistoryRiskFactor extends RiskFactorModel{

	static final String [] smokingTermsDic = {"smoking","smoker","smokes","smoke","smoked","nonsmoker",
		"tobacco","-tobacco","+tobacco","tobacco:","tob","tob:","cigarettes","cigs","packs","pack","ppd"};
	static final String [] pastTermsDic    = {"past","quit","quitting","former","history","remote","smoked","ex","stopped","stop"}; 
	static final String [] currentTermsDic = {"active","continues","current"};
	static final String [] neverTermsDic   = {"none","denies","not","no","never","non","neither","never","neg","-","negative"};




	public String riskFactor = "SMOKER";
	private String smokerModel ;




	/**
	 * @return the smokerModel
	 */
	public String getSmokerModel() {
		return smokerModel;
	}
	/**
	 * @param smokerModel the smokerModel to set
	 */
	public void setSmokerModel(String smokerModel) {
		this.smokerModel = smokerModel;
	}
	public SmokingHistoryRiskFactor getSmokingRisks(FileAnnotation annFile) throws Exception{

		List<SentenceAnnotation> smokingSentList = findSmokingSentence(annFile);
		//generate features
		LinkedList<String> instances = generateFeatures(smokingSentList);
		String smokingClass = "";
		if(instances!=null && instances.size()>0){
			LinkedList<String> predictedClasses = getPredictedClass(instances);
			//summarize
			smokingClass = predictedClasses.get(0);
			
		}else{
			smokingClass = "unknown";
		}
		SmokingHistoryRiskFactor shRiskFactor = new SmokingHistoryRiskFactor();
		shRiskFactor.setStatus(smokingClass);
		return shRiskFactor;

	}
	private LinkedList<String> getPredictedClass(LinkedList<String> instances) throws Exception {
		MalletInstanceGenerator instancegen = new MalletInstanceGenerator();
		MalletClassLabelTagger tagger = new MalletClassLabelTagger();
		LinkedList<String> predictedClassList = new LinkedList<String>();
		for (int i = 0; i < instances.size(); i++) {
			Instance instance = instancegen.generateInstance(instances.get(i));
			String predictedClass = tagger.getTag(instance,this.getSmokerModel());
			predictedClassList.add(predictedClass.split(" ")[0]);
		}
		return predictedClassList;
	}
	private  List<SentenceAnnotation> findSmokingSentence(FileAnnotation annFile) {
		List<SentenceAnnotation> sentList = annFile.getSentenceAnnList();
		List<SentenceAnnotation> smokingSentList = new LinkedList<SentenceAnnotation>();
		for (SentenceAnnotation sentenceAnnotation : sentList) {
			List<TokenAnnotation> tokenList = sentenceAnnotation.getTokenList();
			for (TokenAnnotation tokenAnnotation : tokenList) {
				boolean found = findIndic(tokenAnnotation.getCoveredText(),smokingTermsDic);
				if(found){
					smokingSentList.add(sentenceAnnotation);
				}

			}
		}
		//filter
		List<SentenceAnnotation> filteredSentList = new LinkedList<SentenceAnnotation>();
		if(smokingSentList.size()>1){
			filteredSentList = filterSentList(smokingSentList);

		}else{
			filteredSentList = smokingSentList;
		}

		return filteredSentList;
	}
	private boolean findIndic(String coveredText, String[] dicterms) {
		for (String terms : dicterms) {
			if(terms.toLowerCase().equals(coveredText)){
				return true;
			}
		}
		return false;
	}
	private List<SentenceAnnotation> filterSentList(
			List<SentenceAnnotation> smokingSentList) {
		LinkedList<SentenceAnnotation> filteredSentenceList = new LinkedList<SentenceAnnotation>();
		filteredSentenceList.add(smokingSentList.get(0));
		for (SentenceAnnotation smokingSentAnn : smokingSentList) {
			boolean flagFound = false;
			for (SentenceAnnotation filteredSentAnn : filteredSentenceList) {
				if(filteredSentAnn.equals(smokingSentAnn)){
					flagFound = true;
					break;
				}
			}
			if (!flagFound) {
				filteredSentenceList.add(smokingSentAnn);
			}
		}

		return filteredSentenceList;
	}
	private LinkedList<String> generateFeatures(
			List<SentenceAnnotation> smokingSentList) {
		//get smoking term and pos //get dictterm and Pos //get smokingPrev word and Pos
		LinkedList<String> returnList = new LinkedList<String>();
		for (SentenceAnnotation sentenceAnnotation : smokingSentList) {
			//System.out.println(">>>"+sentenceAnnotation.getCoveredText());
			LinkedList<String> foundTerm = getSmokingTermPos(sentenceAnnotation);
			String smokingTermtext = "";
			if(foundTerm!=null){
				if(foundTerm.size()>2){
					smokingTermtext = foundTerm.get(0)+" "+foundTerm.get(1)+" "+foundTerm.get(2)+" "+foundTerm.get(3)+" "+foundTerm.get(0)+"_"+foundTerm.get(2)+" ";
				}else{
					smokingTermtext = foundTerm.get(0)+" "+foundTerm.get(1)+" ";
				}

			}else{

			}
			String dicttext = getDictTermPos(sentenceAnnotation);
			//System.out.println("instance: "+instanceId+" : "+foundTerm[0]+ " "+foundTerm[1]+ " "+dicttext);
			String instanceText = smokingTermtext+" "+dicttext;
			returnList.add(instanceText);
			//			getPrevTermPos(sentenceAnnotation);
			//			getNextrermPos(sentenceAnnotation);
		}
		return returnList;
	}
	private LinkedList<String> getSmokingTermPos(SentenceAnnotation sentenceAnnotation) {
		LinkedList<String> returnArray = new LinkedList<String>();
		int i=0;
		for (TokenAnnotation token : sentenceAnnotation.getTokenList()) {
			boolean found = findIndic(token.getCoveredText(), smokingTermsDic);
			if(found){
				returnArray.add(token.getCoveredText());
				returnArray.add(token.getPos());
				if(i>0){
					TokenAnnotation prevToken = sentenceAnnotation.getTokenList().get(i-1);
					returnArray.add(prevToken.getCoveredText());
					returnArray.add(prevToken.getPos());
				}
				return returnArray;
			}
			i++;
		}
		return null;
	}
	private String getDictTermPos(SentenceAnnotation sentenceAnnotation) {
		//find in past dic
		String returntext = "";
		LinkedList<TokenAnnotation> pastTermsTokens = findDicTermInSentence(sentenceAnnotation, pastTermsDic);
		if(pastTermsTokens!=null){
			for (TokenAnnotation tokenAnnotation : pastTermsTokens) {
				returntext+=tokenAnnotation.getCoveredText()+" "+tokenAnnotation.getPos()+" "+"pastTerm ";
			}

		}
		//find in current dic
		LinkedList<TokenAnnotation> currentTermsTokens = findDicTermInSentence(sentenceAnnotation, currentTermsDic);
		if(currentTermsTokens!=null){
			for (TokenAnnotation tokenAnnotation : currentTermsTokens) {
				returntext+=tokenAnnotation.getCoveredText()+" "+tokenAnnotation.getPos()+" "+"currentTerm ";
			}
		}
		//find in never dic
		LinkedList<TokenAnnotation> neverTermsTokens = findDicTermInSentence(sentenceAnnotation, neverTermsDic);
		if(neverTermsTokens!=null){
			for (TokenAnnotation tokenAnnotation : neverTermsTokens) {
				returntext+=tokenAnnotation.getCoveredText()+" "+tokenAnnotation.getPos()+" "+"neverTerm ";
			}
		}
		return returntext.toLowerCase();
	}
	private LinkedList<TokenAnnotation> findDicTermInSentence(
			SentenceAnnotation sentenceAnnotation, String[] pasttermsdic) {
		LinkedList<TokenAnnotation> pastTermTokens = new LinkedList<TokenAnnotation>();
		for (String dicTerm : pasttermsdic) {
			for (TokenAnnotation tokenTerm : sentenceAnnotation.getTokenList()) {
				if(tokenTerm.getCoveredText().toLowerCase().equals(dicTerm)){
					pastTermTokens.add(tokenTerm);
				}
			}
		}
		return pastTermTokens;
	}

}
