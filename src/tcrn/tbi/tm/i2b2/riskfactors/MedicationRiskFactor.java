package tcrn.tbi.tm.i2b2.riskfactors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.exception.SystemException;
import tcrn.tbi.tm.i2b2.RiskFactorModel;
import tcrn.tbi.tm.i2b2.RiskFactorsAnnotation;
import tcrn.tbi.tm.mallet.MalletClassLabelTagger;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SectionAnnotation;
import tcrn.tbi.tm.ner.MedicationAnnotation;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class MedicationRiskFactor extends RiskFactorModel {

	private static final Logger logger = LogManager.getLogger(MedicationRiskFactor.class.getName());
	public String riskFactor = "MEDICATION";
	private String medModel;
	private MedicationAnnotation medAnnotation;

	public List<MedicationRiskFactor> getMedicationRisks(List<MedicationAnnotation> medList,
			FileAnnotation annFile) {
		List<MedicationRiskFactor> medRiskList = new LinkedList<MedicationRiskFactor>();
		int i = 0;
		for (MedicationAnnotation medAnnotation : medList) {
			MedicationRiskFactor medRisk = new MedicationRiskFactor();
			medRisk.setId("DOC"+i);
			medRisk.setTagName(medRisk.riskFactor);
			medRisk.setMedAnnotation(medAnnotation);
			medRiskList.add(medRisk);
			i++;
		}
		return medRiskList;

	}

	/**
	 * @return the medAnnotation
	 */
	public MedicationAnnotation getMedAnnotation() {
		return medAnnotation;
	}

	/**
	 * @param medAnnotation the medAnnotation to set
	 */
	public void setMedAnnotation(MedicationAnnotation medAnnotation) {
		this.medAnnotation = medAnnotation;
	}
	public String getMedTimeAttributeModel(List<RiskFactorsAnnotation> rfAnnotations) throws Exception {
		ArrayList<Attribute> attrList = getAttributeList();
		Instances dataRaw = new Instances("TrainingInstances",attrList,0);
		for (RiskFactorsAnnotation riskFactorsAnnotation : rfAnnotations) {
			List<MedicationRiskFactor> medRiskFactorList = riskFactorsAnnotation.getMedications();
			getDataAsinstance(dataRaw,medRiskFactorList,riskFactorsAnnotation);
		}
		//get model
		//System.out.println(dataRaw);
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		Instances dataFiltered = Filter.useFilter(dataRaw, filter);
		// System.out.println(dataFiltered);
		dataFiltered.setClassIndex(0); 
		String modelPath = getNaiveBayesModel(dataFiltered);
		return modelPath;

	}
	private void getDataAsinstance(Instances dataRaw, List<MedicationRiskFactor> medRiskFactorList, RiskFactorsAnnotation riskFactorsAnnotation) throws SystemException {
		for (MedicationRiskFactor medicationRiskFactor : medRiskFactorList) {
			MedicationAnnotation medAnn = medicationRiskFactor.getMedAnnotation();
			String coveredText = medAnn.getCoveredText();
			Integer startOffset = medAnn.getStartOffset();
			Integer endOffset = medAnn.getEndOffset();
			String timeAttr = medAnn.getScore();
			String secHeader = getSectionHeader(riskFactorsAnnotation,startOffset,endOffset);
			if(secHeader!=null){
				//logger.info(coveredText+":"+secHeader+":"+riskFactorsAnnotation.getFileName());
				//create weka instance 
				double[] instanceValue1 = new double[dataRaw.numAttributes()];
				instanceValue1[0] = dataRaw.attribute(0).addStringValue(coveredText);
				instanceValue1[1] = dataRaw.attribute(1).addStringValue(secHeader);
				Integer classInt = null ;
				if(timeAttr.equals("before DCT")){
					classInt = 0;
				}else if (timeAttr.equals("during DCT")) {
					classInt = 1;
				}else if(timeAttr.equals("after DCT")){
					classInt = 2;
				}else if (timeAttr.equals("CONTINUING")) {
					classInt = 3;
				}else{
					logger.warn("unkonwn time attribute in time attr");
				}
				//add class label
				if (classInt!=null) {
					instanceValue1[2] = classInt;
				}else{
					logger.error("undefined class type");
					throw new SystemException("undefined class type "+timeAttr+" for file "+riskFactorsAnnotation.getFileName()+ " at "+startOffset+" "+ endOffset);
				}
				dataRaw.add(new DenseInstance(1.0, instanceValue1));
			}else{
				logger.warn("No section info found");
			}
		}

	}

	private String getNaiveBayesModel(Instances dataRaw) throws Exception {
		Classifier cModel = (Classifier)new NaiveBayes();
		cModel.buildClassifier(dataRaw);
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(this.medModel));
		oos.writeObject(cModel);
		oos.flush();
		oos.close();
		return this.medModel;
		//		 System.out.println(dataRaw.get(11).classValue());
		//		 double predictedclass = cModel.classifyInstance(dataRaw.get(11));
		//		 System.out.println(predictedclass);

	}

	private ArrayList<Attribute> getAttributeList() {
		//declare attr
		ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("BEFORE_DCT");
		classVal.add("DURING_DCT");
		classVal.add("AFTER_DCT");
		classVal.add("CONTINUING");
		atts.add(new Attribute("content",(ArrayList<String>)null));
		atts.add(new Attribute("content2",(ArrayList<String>)null));
		atts.add(new Attribute("@@class@@",classVal));


		return atts;

	}
	private LinkedList<String> parsedSectionList;
	private String getSectionHeader(RiskFactorsAnnotation riskFactorsAnnotation, Integer startOffset, Integer endOffset){
		List<SectionAnnotation> SectionAnnotations = riskFactorsAnnotation.getSectionAnnotation();
		parsedSectionList = new LinkedList<String>();
		for (SectionAnnotation sectionAnnotation : SectionAnnotations) {
			Integer start = sectionAnnotation.getStartOffset();
			Integer end = sectionAnnotation.getEndOffset();
			String sectionName = sectionAnnotation.getCoveredText();
			parsedSectionList.add(start+"\t"+end+"\t"+sectionName);
		}

		//reverse it
		LinkedList<String> tempList = new LinkedList<String>();
		@SuppressWarnings("rawtypes")
		Iterator x = parsedSectionList.descendingIterator();

		// print list with descending order
		while (x.hasNext()) {
			tempList.add((String) x.next());
		}
		parsedSectionList = tempList;
		int i = 0;
		LinkedList<SectionAnnotation> sectionList = new LinkedList<SectionAnnotation>();
		for (String sectionOffset : parsedSectionList) {
			SectionAnnotation sec = new SectionAnnotation();
			sec.setCoveredText(sectionOffset.split("\t")[2]);
			//String currentSectionEnd = sectionOffset.split("\t")[1];
			String currentSectionStart = sectionOffset.split("\t")[0];
			String nextSectionStart = null;
			if(i<parsedSectionList.size()-1){
				String nextSection = parsedSectionList.get(i+1);
				nextSectionStart = nextSection.split("\t")[0];
			}
			else{
				Integer nextSectionStartInt = riskFactorsAnnotation.getCoveredText().length();
				nextSectionStart = nextSectionStartInt.toString();
			}
			sec.setStartOffset(Integer.parseInt(currentSectionStart));
			sec.setEndOffset(Integer.parseInt(nextSectionStart));
			sectionList.add(sec);
			i++;
		}
		for (SectionAnnotation sec  : sectionList) {
			Integer secStart = sec.getStartOffset();
			Integer secEnd = sec.getEndOffset();
			if(secStart <= startOffset && secEnd >= endOffset){
				//System.out.println(secStart+" "+ann.getStartOffset()+" "+" "+ann.getEndOffset()+" "+secEnd);
				return sec.getCoveredText();
			}
		}
		logger.error("cannot find section for file "+riskFactorsAnnotation.getFileName()+ startOffset + " "+ endOffset + riskFactorsAnnotation.getCoveredText().substring(startOffset,endOffset));
		return null;
	}

	/**
	 * @return the medModel
	 */
	public String getMedModel() {
		return medModel;
	}

	/**
	 * @param medModel the medModel to set
	 */
	public void setMedModel(String medModel) {
		this.medModel = medModel;
	}

	public void getMedTimeAttribute(List<RiskFactorsAnnotation> rfAnnotations) throws Exception {
		//generate classifier from model
		//get redicted time  attr for each instance
		InputStream is = MedicationRiskFactor.class.getResourceAsStream("/med_naive_bayes.model");
		Classifier classifier = (Classifier) weka.core.SerializationHelper.read(is);
		ArrayList<Attribute> attrList = getAttributeList();
		Instances dataRaw = new Instances("TestingInstances",attrList,0);
		double[] instanceValue1 = new double[dataRaw.numAttributes()];
		for (RiskFactorsAnnotation riskFactorsAnnotation : rfAnnotations) {
			List<MedicationRiskFactor> medRiskFactorList = riskFactorsAnnotation.getMedications();
			for (MedicationRiskFactor medicationRiskFactor : medRiskFactorList) {
				MedicationAnnotation medAnn = medicationRiskFactor.getMedAnnotation();
				String coveredText = medAnn.getCoveredText();
				Integer startOffset = medAnn.getStartOffset();
				Integer endOffset = medAnn.getEndOffset();
				String secHeader = getSectionHeader(riskFactorsAnnotation,startOffset,endOffset);
				instanceValue1[0] = dataRaw.attribute(0).addStringValue(coveredText);
				//System.out.println(secHeader+ coveredText+ startOffset+" "+endOffset+ riskFactorsAnnotation.getFileName());
				instanceValue1[1] = dataRaw.attribute(1).addStringValue(secHeader);
				dataRaw.add(new DenseInstance(1.0, instanceValue1));
				dataRaw.setClassIndex(dataRaw.numAttributes()-1);
				StringToWordVector filter = new StringToWordVector();
				filter.setInputFormat(dataRaw);
				Instances dataFiltered = Filter.useFilter(dataRaw, filter);
				dataFiltered.setClassIndex(0); 
				double predictedClass = classifier.classifyInstance(dataRaw.get(0));
				//System.out.println(predictedClass + " -> " + dataRaw.classAttribute().value((int) predictedClass));
				String time = assignTimeAttr(predictedClass);
				medicationRiskFactor.setTime(time);
			}
			//		 System.out.println(predictedclass);

		}
	}

	private String assignTimeAttr(double predictedClass) {
		String timeString = null;
		if(predictedClass==0.0){
			timeString = "before DCT";
		}else if(predictedClass==1.0){
			timeString = "during DCT";
		}else if (predictedClass==2.0) {
			timeString = "after DCT";
		} else {
			timeString = "Continuing";
		}
		return timeString;
	}

	public List<MedicationRiskFactor> filterDrugAndAssignType(
			List<MedicationRiskFactor> medRiskFactorList) throws Exception {
		InputStream input = MedicationRiskFactor.class.getResourceAsStream("/Drug_class.csv");
		HashMap<String, String[]> drugClassMap = getDrugClass(input);
		for (MedicationRiskFactor medicationRiskFactor : medRiskFactorList) {
			MedicationAnnotation medAnn = medicationRiskFactor.getMedAnnotation();
			String drugText = medAnn.getCoveredText();
			String[] types = drugClassMap.get(drugText.toLowerCase());
			if(types!=null){
				if(types.length==1  ){
					medicationRiskFactor.setType1(types[0]);
				}else if(types.length==2 && types!=null){
					medicationRiskFactor.setType1(types[0]);
					medicationRiskFactor.setType1(types[1]);
				}
			}else{
				logger.debug("no type found for drug: "+drugText+" ..will be filtered out");
			}
		}
		List<MedicationRiskFactor> medRiskFactorListCopy = new LinkedList<MedicationRiskFactor>();
		for (MedicationRiskFactor medicationRiskFactor : medRiskFactorList) {
			String type1 = medicationRiskFactor.getType1();
			if(type1!=null){
				medRiskFactorListCopy.add(medicationRiskFactor);
			}
		}
		return medRiskFactorListCopy;
	}

	private static HashMap<String, String[]> getDrugClass(InputStream input) throws IOException {
		HashMap<String, String[]> drugClassMap = new HashMap<String, String[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		String line;
		while((line=br.readLine())!=null){
			//System.out.println(line);
			if(line.split(",").length>1){
				String [] split = line.split(",");

				if(split.length==3){
					String [] types = {split[1],split[2]};
					drugClassMap.put(split[0],types);
				}else{
					String [] types = {split[1]};
					drugClassMap.put(split[0],types);
				}


			}

		}
		br.close();
		return drugClassMap;
	}

	public void filterByTimeAndType(List<RiskFactorsAnnotation> rfAnnotations) {
		for (RiskFactorsAnnotation riskFactorsAnnotation : rfAnnotations) {
			List<MedicationRiskFactor> medRiskFactorList = riskFactorsAnnotation.getMedications();
			List<MedicationRiskFactor> filteredList = new LinkedList<>();
			for (MedicationRiskFactor medicationRiskFactor : medRiskFactorList) {
				//find duplicate
				boolean isFound = findInList(medicationRiskFactor,filteredList);
				if(!isFound){
					filteredList.add(medicationRiskFactor);
				}
			}
			riskFactorsAnnotation.setMedications(filteredList);
		}
		
	}

	private boolean findInList(MedicationRiskFactor medicationRiskFactor,
			List<MedicationRiskFactor> filteredList) {
		for (MedicationRiskFactor medicationRiskFactor2 : filteredList) {
			String timethis = medicationRiskFactor2.getTime();
			String type1this = medicationRiskFactor2.getType1();
			String timeTomatch = medicationRiskFactor.getTime();
			String type1toMatch = medicationRiskFactor.getType1();
			if(timethis.equals(timeTomatch)&& type1this.equals(type1toMatch)){
				return true;
			}
		}
		return false;
	}
	


}
