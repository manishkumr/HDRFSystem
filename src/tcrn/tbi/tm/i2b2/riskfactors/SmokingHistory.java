package tcrn.tbi.tm.i2b2.riskfactors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import tcrn.tbi.tm.core.LexicalVariantsGenerator;
import tcrn.tbi.tm.core.PosAnnotator;
import tcrn.tbi.tm.core.SentencerAnnotator;
import tcrn.tbi.tm.core.TokenAnnotator;
import tcrn.tbi.tm.documents.DocumentsReader;
import tcrn.tbi.tm.documents.XMLpreprocessor;
import tcrn.tbi.tm.exception.SystemException;
import tcrn.tbi.tm.mallet.MalletClassLabelTagger;
import tcrn.tbi.tm.mallet.MalletInstanceGenerator;
import tcrn.tbi.tm.mallet.MalletModelGenerator;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class SmokingHistory {
	//extract from dataset sentence for smoking history mention

	static final String trainingGoldCompleteSetPath = "D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014 Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\Training\\training-RiskFactors-Complete-Set1"; 
	//static final String trainingGoldCompleteSetPath = "D:\\TBI_Datasets\\i2b2_dataset\\Train_1_train_2_test_complete"; 
	static final String devCompleteSetPath = "D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014 Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\Training\\training-RiskFactors-Complete-Set2";
	static final String testingCompleteSetPath = "D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014 Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\Test\\testing-RiskFactors-Complete\\xmlFiles";
	
	static final Logger logger = LogManager.getLogger(SmokingHistory.class.getName());

	//dicts
	static final String [] smokingTermsDic = {"smoking","smoker","smokes","smoke","smoked","nonsmoker",
		"tobacco","-tobacco","+tobacco","tobacco:","tob","tob:","cigarettes","cigs","packs","pack","ppd"};
	static final String [] pastTermsDic    = {"past","quit","quitting","former","history","remote","smoked","ex","stopped","stop"}; 
	static final String [] currentTermsDic = {"active","continues","current"};
	static final String [] neverTermsDic   = {"none","denies","not","no","never","non","neither","never","neg","-","negative"};


	public static void main(String [] args) {
		//generate csv
		try {
			//generateSmokingHistoryAnnotation(testingGoldSetPath);
			findSmokingSentence(trainingGoldCompleteSetPath);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void findSmokingSentence(String goldcompletesetpath2) throws Exception{
		DocumentsReader docReader = null;
		docReader = new DocumentsReader();
		docReader.setFilePath(trainingGoldCompleteSetPath);
		File[] trainingInputFiles = docReader.getFiles();
		//training
		writeSmokingInstances(trainingInputFiles,"smokingTrainningFile.txt");
		//generate training instances 
		MalletInstanceGenerator malletInstances = new MalletInstanceGenerator();
		InstanceList trainingInstances = malletInstances.generateInstanceList("smokingTrainningFile.txt",true);
		//write testing instances 
		docReader.setFilePath(devCompleteSetPath);
		File [] devInputFiles = docReader.getFiles();
		writeSmokingInstances(devInputFiles,"smokingTestingFile.txt");
		InstanceList devInstances = malletInstances.generateInstanceList("smokingTestingFile.txt",true);

		//generate  model and test on dev dataset
		MalletModelGenerator malletModel = new MalletModelGenerator();
		malletModel.getTrainingModel(trainingInstances, devInstances);
		//predict from model
		//get testing mallet instances
		docReader.setFilePath(testingCompleteSetPath);
		File [] testingInputFiles = docReader.getFiles();
		
		getSmokingInstance(testingInputFiles);
//		MalletInstanceGenerator malletInstances = new MalletInstanceGenerator();
//		InstanceList testInstances = malletInstances.generateInstanceList("smokingTestingFile.txt",true);
//		MalletClassLabelTagger tagger = new MalletClassLabelTagger();
//		tagger.getTags("SmokingClassifier.model", testInstances);
	}
	private static void getSmokingInstance(File[] testingInputFiles) throws Exception{
		for (File file : testingInputFiles) {
			//get annotations
			logger.info("processing "+file.getName());
			FileAnnotation annFile = new FileAnnotation();
			String fileName = file.getName();
			annFile.setFileName(fileName);
			//String fileExt = fileName.split("\\.")[1];
			XMLpreprocessor preprocessor = new XMLpreprocessor(file);
			String fileText = preprocessor.getFileTextAsString("TEXT");
			annFile.setCoveredText(fileText);
			annFile.setStartOffset(0);
			annFile.setEndOffset(fileText.length());
			SentencerAnnotator sentAnnotator = new SentencerAnnotator();
			sentAnnotator.annotate(annFile);
			//get tokens
			TokenAnnotator tokenAnnotator = new TokenAnnotator();
			tokenAnnotator.annotate(annFile);
			LexicalVariantsGenerator lvgGenerator = new LexicalVariantsGenerator();
			lvgGenerator.annotate(annFile);
			//get pos 
			PosAnnotator posAnnotator = new PosAnnotator();
			posAnnotator.annotate(annFile);
			//find smoking tokens and return sentence
			System.out.println("fileName"+annFile.getFileName());
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
			//write to file
			writeToFile(annFile,smokingClass);
		}

	}

	private static void writeToFile(FileAnnotation annFile, String smokingClass) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		File outFolder = new File("smokingOut");
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			Element text = doc.createElement("TEXT");
			CDATASection cdata = doc.createCDATASection("mycdata");
			cdata.setTextContent(annFile.getCoveredText());
			text.appendChild(cdata);
			rootElement.appendChild(text);
			//create tags
			Element tags = doc.createElement("TAGS");
			rootElement.appendChild(tags);
			logger.info("writing system out file for "+annFile.getFileName());
			writeMedAnnotationSystem(tags, doc, smokingClass);
		
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			//for pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			//write to console or file
			
			if(outFolder==null||!outFolder.exists()){
				outFolder = new File("smokingOut");
				outFolder.mkdir();
			}
			//System.out.println("writing file: "+riskFactorsAnnotation.getFileName());
			StreamResult file = new StreamResult(new File(outFolder,annFile.getFileName()));
			//write data
			transformer.transform(source, file);
			//System.out.println("done");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	private static void writeMedAnnotationSystem(Element tags, Document doc,
			String smokingClass) {
		Element medicationElem = doc.createElement("SMOKER");
		medicationElem.setAttribute("id", "DOC2");
		medicationElem.setAttribute("status", smokingClass);
		tags.appendChild(medicationElem);
		
	}
	private static LinkedList<String> getPredictedClass(LinkedList<String> instances) throws Exception {
		MalletInstanceGenerator instancegen = new MalletInstanceGenerator();
		MalletClassLabelTagger tagger = new MalletClassLabelTagger();
		LinkedList<String> predictedClassList = new LinkedList<String>();
		for (int i = 0; i < instances.size(); i++) {
			Instance instance = instancegen.generateInstance(instances.get(i));
			String predictedClass = tagger.getTag(instance,"SmokingClassifier.model");
			predictedClassList.add(predictedClass.split(" ")[0]);
		}
		return predictedClassList;
	}
	private static void writeSmokingInstances(File[] inputFiles,String filePath) throws Exception {
		FileWriter smokingFeatWriter = new FileWriter(filePath);
		for (File file : inputFiles) {
			//get annotations
			logger.info("processing "+file.getName());
			FileAnnotation annFile = new FileAnnotation();
			String fileName = file.getName();
			annFile.setFileName(fileName);
			//String fileExt = fileName.split("\\.")[1];
			XMLpreprocessor preprocessor = new XMLpreprocessor(file);
			String fileText = preprocessor.getFileTextAsString("TEXT");
			annFile.setCoveredText(fileText);
			annFile.setStartOffset(0);
			annFile.setEndOffset(fileText.length());
			SentencerAnnotator sentAnnotator = new SentencerAnnotator();
			sentAnnotator.annotate(annFile);
			//get tokens
			TokenAnnotator tokenAnnotator = new TokenAnnotator();
			tokenAnnotator.annotate(annFile);
			LexicalVariantsGenerator lvgGenerator = new LexicalVariantsGenerator();
			lvgGenerator.annotate(annFile);
			//get pos 
			PosAnnotator posAnnotator = new PosAnnotator();
			posAnnotator.annotate(annFile);
			//find smoking tokens and return sentence
			System.out.println("fileName"+annFile.getFileName());
			List<SentenceAnnotation> smokingSentList = findSmokingSentence(annFile);
			//generate features
			LinkedList<String> instances = generateFeatures(smokingSentList);

			//get class label
			String status = getClassLabelForSmoking(file);
			if(status!=null && !status.equalsIgnoreCase("unknown") && !status.equalsIgnoreCase("ever") ){
				for (String instance : instances) {
					smokingFeatWriter.write(instance.toLowerCase()+" "+status+"\n\n");
				}
			}else if(status!=null){
				System.out.println("class Label: "+status);
			}else{
				System.out.println("class Label: NULL");
			}
		}
		smokingFeatWriter.close();
	}
	private static String getClassLabelForSmoking(File file) throws Exception {
		XMLpreprocessor preprocessor = new XMLpreprocessor(file);
		//get smoking tags
		String status = null;
		NodeList smokingNodeList = preprocessor.getRiskfactorNodes("SMOKER");
		for (int i = 0; i < smokingNodeList.getLength(); i++) {
			Node node = smokingNodeList.item(i);
			if(node.hasChildNodes()){
				NodeList childNodeList = node.getChildNodes();
				for (int j = 0; j < childNodeList.getLength(); j++) {
					Node childNode = childNodeList.item(j);
					if (childNode.getNodeName().equals("SMOKER")){ 
						Node statusNode = childNode.getAttributes()
								.getNamedItem("status");
						if (statusNode!=null) {
							status = statusNode.getNodeValue();
						}
						break;//to capture first elem only
					}
				}
			}
		}
		return status;
	}
	private static LinkedList<String> generateFeatures(
			List<SentenceAnnotation> smokingSentList) {
		//get smoking term and pos //get dictterm and Pos //get smokingPrev word and Pos
		int instanceId = 1;
		LinkedList<String> returnList = new LinkedList<String>();
		for (SentenceAnnotation sentenceAnnotation : smokingSentList) {
			System.out.println(">>>"+sentenceAnnotation.getCoveredText());
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
			instanceId++;
			//			getPrevTermPos(sentenceAnnotation);
			//			getNextrermPos(sentenceAnnotation);
		}
		return returnList;
	}
	private static String getDictTermPos(SentenceAnnotation sentenceAnnotation) {
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
	private static LinkedList<TokenAnnotation> findDicTermInSentence(
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
	private static LinkedList<String> getSmokingTermPos(SentenceAnnotation sentenceAnnotation) {
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
	private static List<SentenceAnnotation> findSmokingSentence(FileAnnotation annFile) {
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
	private static List<SentenceAnnotation> filterSentList(
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
	private static boolean findIndic(String coveredText, String[] dicterms) {
		for (String terms : dicterms) {
			if(terms.toLowerCase().equals(coveredText)){
				return true;
			}
		}
		return false;
	}
	/*private static void generateSmokingHistoryAnnotation(
			String goldcompletesetpath2) throws SystemException, Exception {
		DocumentsReader docReader = null;
		docReader = new DocumentsReader();
		docReader.setFilePath(goldcompletesetpath2);
		FileWriter fw = new FileWriter("TestsmokingHistory.csv");

		File[] inputFiles = docReader.getFiles();
		for (File file : inputFiles) {
			//get annotations
			logger.info("processing "+file.getName());
			FileAnnotation annFile = new FileAnnotation();
			String fileName = file.getName();
			annFile.setFileName(fileName);
			//String fileExt = fileName.split("\\.")[1];
			XMLpreprocessor preprocessor = new XMLpreprocessor(file);
			String fileText = preprocessor.getFileTextAsString("TEXT");
			annFile.setCoveredText(fileText);
			annFile.setStartOffset(0);
			annFile.setEndOffset(fileText.length());
			//get smoking tags
			NodeList smokingNodeList = preprocessor.getRiskfactorNodes("SMOKER");
			//write to file
			writeToCsv(smokingNodeList,fw,annFile.getFileName());

		}
		fw.close();
	}*/
	/*private static void writeToCsv(NodeList smokingNodeList, FileWriter fw, String fileName) throws Exception {
		for (int i = 0; i < smokingNodeList.getLength(); i++) {
			Node node = smokingNodeList.item(i);
			if(node.hasChildNodes()){
				NodeList childNodeList = node.getChildNodes();
				for (int j = 0; j < childNodeList.getLength(); j++) {
					Node childNode = childNodeList.item(j);
					if (childNode.getNodeName().equals("SMOKER")){ 
						Node annotatedTextNode = childNode.getAttributes()
								.getNamedItem("text");
						String annotatedText = "",start = "", end ="",status= "";
						if(annotatedTextNode!=null){
							annotatedText = annotatedTextNode.getNodeValue();
						}
						Node startStringNode = childNode.getAttributes()
								.getNamedItem("start");
						if (startStringNode!=null) {
							start = startStringNode.getNodeValue();
						}

						Node endStringNode = childNode.getAttributes()
								.getNamedItem("end");
						if (endStringNode!=null) {
							end = endStringNode.getNodeValue();
						}
						Node statusNode = childNode.getAttributes()
								.getNamedItem("status");
						if (statusNode!=null) {
							status = statusNode.getNodeValue();
						}
						fw.write(fileName+",\""+annotatedText+"\","+start+","+end+","+status+"\n");
						break;//to capture first elem only
					}
				}
			}
		}
	}*/
}
