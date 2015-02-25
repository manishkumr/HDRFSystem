package tcrn.tbi.tm.runner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.core.LexicalVariantsGenerator;
import tcrn.tbi.tm.core.PosAnnotator;
import tcrn.tbi.tm.core.SectionAnnotator;
import tcrn.tbi.tm.core.SentencerAnnotator;
import tcrn.tbi.tm.core.ShallowParser;
import tcrn.tbi.tm.core.TokenAnnotator;
import tcrn.tbi.tm.dictionary.ChunkAdjuster;
import tcrn.tbi.tm.documents.DocumentsReader;
import tcrn.tbi.tm.documents.TextPreprocessor;
import tcrn.tbi.tm.documents.XMLpreprocessor;
import tcrn.tbi.tm.i2b2.RiskFactorGoldAnnotation;
import tcrn.tbi.tm.i2b2.RiskFactorsAnnotation;
import tcrn.tbi.tm.i2b2.riskfactors.FamilyHistoryRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HyperlipidemiaRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HypertensionRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.MedicationRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.SmokingHistoryRiskFactor;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.ner.DiseaseDisorderNER;
import tcrn.tbi.tm.ner.DrugNER;
import tcrn.tbi.tm.ner.MedicationAnnotation;
import tcrn.tbi.tm.ruta.RutaAnnotator;

/**
 * @author Manish
 *
 */
public class I2B2systemRunner {
	
	static final String trainingGoldsetPath = "D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014 Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\Test\\testing-RiskFactors-Gold\\xmlFiles";
	static final String sectionPath = "D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\TCRN Sectionizer\\DaiSectionizer Gold Set by NLP students\\Training Set1\\ann";
	static final Logger logger = LogManager.getLogger(I2B2systemRunner.class.getName());
	static final String trainingCompleteSetPath = "testDocs_complete";
	public static void main(String[] args) {
		logger.info("processing i2b2");
		try {
			process();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}
	/**
	 * processes each file in the folder containg xml files
	 * @throws Exception 
	 */
	private static void process() throws Exception {
		long startTime = System.currentTimeMillis();
		String mode = "testing";
		SystemConfiguration sysConfiguration = new SystemConfiguration();
		sysConfiguration = sysConfiguration.getPropertyValues();
		List<RiskFactorsAnnotation> rfAnnotations = new LinkedList<RiskFactorsAnnotation>();
		//get documents
		DocumentsReader docReader = null;
		docReader = new DocumentsReader();
		docReader.setFilePath(sysConfiguration.getInputDirectoryPath());
		File[] inputFiles = docReader.getFiles();
		for (File file : inputFiles) {
			//get annotations
			logger.info("processing "+file.getName());
			FileAnnotation annFile = new FileAnnotation();
			String fileName = file.getName();
			annFile.setFileName(fileName);
			String fileExt = fileName.split("\\.")[1];
			//get fileText from xml file
			if (fileExt.equals("xml")) {
				XMLpreprocessor preprocessor = new XMLpreprocessor(file);
				String fileText = preprocessor.getFileTextAsString("TEXT");
				annFile.setCoveredText(fileText);
				annFile.setStartOffset(0);
				annFile.setEndOffset(fileText.length());
			//get fileText fron text
			}
			/*else if (fileExt.equals("text")){
				TextPreprocessor preprocessor = new TextPreprocessor();
				String fileText = preprocessor.processTextFile(file);
//				String testText = "" +
//					      "Medications:\n" +
//					      "Hibernol, jamitol, triopenin, sproingo\n\n" +
//					      "Physical exam:\n" +
//					      "Patient is doing fine but probably taking too many fictional drugs. Cholesterol is acceptable. Heartrate is elevated. \n" +
//					      "Instructions:\n" +
//					      "Patient should quit smoking and taunting sharks.";
				//annFile.setCoveredText("Patient recorded as having no known allergies to drugs");
				//Induction of NF-KB during monocyte differentiation by HIV type 1 infection.
				//HISTORY OF PRESENT ILLNESS:  The patient is a 40-year-old female with complaints of headache and dizziness.
				annFile.setCoveredText(fileText);
				//annFile.setCoveredText(testText);
				annFile.setStartOffset(0);
				annFile.setEndOffset(fileText.length());
			}*/
			//get sentence annotaion
			logger.info("Processing Core NLP");
			SentencerAnnotator sentAnnotator = new SentencerAnnotator();
			sentAnnotator.annotate(annFile);
			//get tokens
			TokenAnnotator tokenAnnotator = new TokenAnnotator();
			tokenAnnotator.annotate(annFile);
			//LVG
			LexicalVariantsGenerator lvgGenerator = new LexicalVariantsGenerator();
			lvgGenerator.annotate(annFile);
			//get pos
			PosAnnotator posAnnotator = new PosAnnotator();
			posAnnotator.annotate(annFile);
			//System.out.println(annFile.getClass());
			//get chunks
			ShallowParser sp = new ShallowParser();
			sp.annotate(annFile);
			//Chunke merger
			ChunkAdjuster chunkadjuster = new ChunkAdjuster();
			chunkadjuster.annotate(annFile);
			//section
			logger.info("Processing Sectionizer");
			SectionAnnotator sectionAnnotator = new SectionAnnotator();
			sectionAnnotator.setSectionGoldPath("sectionDataset");
			sectionAnnotator.annotate(annFile);
			
			
			//get Medication
			logger.info("Processing Medications");
			List<MedicationRiskFactor> medRiskFactorList = getMedicationRiskFacrorList("testing",annFile);
			//get smoking history
			logger.info("Processing Smoking History");
			SmokingHistoryRiskFactor shriskFactor = new SmokingHistoryRiskFactor();
			shriskFactor.setSmokerModel("SmokingClassifier.model");;
			shriskFactor = shriskFactor.getSmokingRisks(annFile);
			//get family history
			logger.info("Processing Family History");
			FamilyHistoryRiskFactor fhRiskFactor = new FamilyHistoryRiskFactor();
			fhRiskFactor = fhRiskFactor.getFamilyHistory(annFile);
			//get hypertension
			logger.info("Processing Hypertension");
			HypertensionRiskFactor hrf = new HypertensionRiskFactor();
			List<HypertensionRiskFactor> hrfList = hrf.getHypertension(annFile);
			//get hyperlipedimai
			logger.info("Processing Hyperlipidemia");
			HyperlipidemiaRiskFactor hlrf = new HyperlipidemiaRiskFactor();
			List<HyperlipidemiaRiskFactor> hlrfList = hlrf.getHyperlipidemia(annFile);
			//get CAD
			logger.info("Processing CAD");
			//get Obesity
			logger.info("Processing Obesity");
			//get Diabetes
			logger.info("Processing Diabetes");
			
			//medRiskFactor.getMedTimeAttribute(medRiskFactorList,annFile);
			RiskFactorsAnnotation riskFactor  = new RiskFactorsAnnotation();
			riskFactor.setCoveredText(annFile.getCoveredText());
			riskFactor.setStartOffset(annFile.getStartOffset());
			riskFactor.setEndOffset(annFile.getEndOffset());
			riskFactor.setFileName(annFile.getFileName());
			riskFactor.setSentenceAnnList(annFile.getSentenceAnnList());
			riskFactor.setSectionAnnotation(annFile.getSectionAnnotation() );
			riskFactor.setSectionAnnotation(annFile.getSectionAnnotation());
			riskFactor.setMedications(medRiskFactorList);
			//add other risk factor
			riskFactor.setSmokingHistory(shriskFactor);
			riskFactor.setFamilyHistory(fhRiskFactor);
			riskFactor.setHypertension(hrfList);
			riskFactor.setHyperlipidemia(hlrfList);
			//add to this risk factor to list
			rfAnnotations.add(riskFactor);
			
			//create smoking risk factor
			
			//break;
		}//end of loop
		
		MedicationRiskFactor medRiskFactor = new MedicationRiskFactor();
		medRiskFactor.setMedModel("models/med_naive_bayes.model");
		if (mode.equals("training")) {
			//get Medication time attribute
			String generatedmodelPath = medRiskFactor.getMedTimeAttributeModel(rfAnnotations);
			System.out.println(generatedmodelPath);
		}else if (mode.equals("testing")) {
			// find generated model
			medRiskFactor.getMedTimeAttribute(rfAnnotations);
			medRiskFactor.filterByTimeAndType(rfAnnotations);
			// get time attribute
			// write to file
		}
		//write to file
		I2B2SystemOutputWriter outWriter = new I2B2SystemOutputWriter();
		logger.info("Writing "+rfAnnotations.size()+" number of files to output directory");
		
		//type "complete" or "sys" 
		String outType = sysConfiguration.getOutputType();
		if(outType.equalsIgnoreCase("system")){
			outWriter.setOutFolderPath(sysConfiguration.getOutputDirectoryPath()+"/sys_out");
			outWriter.writeOutput(rfAnnotations,"sys");
		}else if(outType.equalsIgnoreCase("complete")){
			outWriter.setOutFolderPath(sysConfiguration.getOutputDirectoryPath()+"/complete_out");
			outWriter.writeOutput(rfAnnotations, "complete");
		}else{
			outWriter.setOutFolderPath(sysConfiguration.getOutputDirectoryPath()+"/sys_out");
			outWriter.writeOutput(rfAnnotations,"sys");
			outWriter.setOutFolderPath(sysConfiguration.getOutputDirectoryPath()+"/complete_out");
			outWriter.writeOutput(rfAnnotations, "complete");
		}
		long endTime = System.currentTimeMillis();
		long totalTime = (endTime - startTime)/1000;
		logger.info("processing Complete in "+totalTime+" seconds");
		
	}
	private static List<MedicationRiskFactor> getMedicationRiskFacrorList(String mode,FileAnnotation annFile) throws Exception {
		DrugNER drugNer =  new DrugNER();
		List<MedicationAnnotation> medList = null;
		MedicationRiskFactor medRiskFactor = null;
		List<MedicationRiskFactor> medRiskFactorList = null;
		if(mode.equals("training")){
			//add drug info  from complete set
			RiskFactorGoldAnnotation  goldAnn = new RiskFactorGoldAnnotation();
			goldAnn.setRiskFactor("MEDICATION");
			goldAnn.setGoldFolderPath(trainingCompleteSetPath);
			medList = goldAnn.getMedicationAnnotation(annFile);
			//get time attr from gold set
			medRiskFactor = new MedicationRiskFactor();
			medRiskFactorList = medRiskFactor.getMedicationRisks(medList,annFile);
			
		}else if(mode.equals("testing")){
			// add drug info by using DrugNer
			medList = drugNer.drugLookup(annFile);
			medRiskFactor = new MedicationRiskFactor();
			medRiskFactorList = medRiskFactor.getMedicationRisks(medList,annFile);
			// filter drug and assign type
			medRiskFactorList = medRiskFactor.filterDrugAndAssignType(medRiskFactorList);
			//diseaseNer.diseaseLookup(annFile);
		}
		return medRiskFactorList;
	}
}
