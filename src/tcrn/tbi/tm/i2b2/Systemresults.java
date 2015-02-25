package tcrn.tbi.tm.i2b2;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tcrn.tbi.tm.documents.XMLpreprocessor;
import tcrn.tbi.tm.util.I2B2Utils;

public class Systemresults {

	/*static String oldResultsPath = "D:\\TBI_Datasets\\i2b2results\\TMUNSW_task2_run3b\\TMUNSW_task2_run3b";
	static String htnResultsPath = "D:\\TBI_Datasets\\htn";
	static String newResultsPath = "D:\\TBI_Datasets\\sys2";*/
	static String medication = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\Medication";
	static String obesity = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\obesity";
	static String diabetes = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\diabetes";
	static String hypertension = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\hypertension";
	static String hyperlipidemia = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\hyperlipidemia";
	static String FH = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\FH";
	static String SH = "D:\\TBI_Datasets\\i2b2Training\\trainingSysOutRiskFactors\\SH";
	
	
	static File [] medicationFolder = new File(medication).listFiles();
	static File [] obesityFolder = new File(obesity).listFiles();
	static File [] diabetesFolder = new File(diabetes).listFiles();
	static File [] hypertensionFolder = new File(hypertension).listFiles();
	static File [] hyperlipedimaiFolder = new File(hyperlipidemia).listFiles();
	static File [] fhFolder  = new File(FH).listFiles();
	static File [] shFolder  = new File(SH).listFiles();
	public static void main(String[] args) {
		
		
		try {
			processresults();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void processresults() throws Exception {
		File outFolder = new File("D:\\TBI_Datasets\\i2b2Training\\trainingResultsMerged");
		for (File medRes : medicationFolder) {
			File oberes = I2B2Utils.findGoldFile(medRes.getName(), obesityFolder);
			File diaRes = I2B2Utils.findGoldFile(medRes.getName(), diabetesFolder);
			File htres = I2B2Utils.findGoldFile(medRes.getName(), hypertensionFolder);
			File hlres = I2B2Utils.findGoldFile(medRes.getName(), hyperlipedimaiFolder);
			File fhRes = I2B2Utils.findGoldFile(medRes.getName(), fhFolder);
			File shRes = I2B2Utils.findGoldFile(medRes.getName(), shFolder);
			
			//pars xml and get Tags
			//get text and medication from medRes
			XMLpreprocessor xpr = new XMLpreprocessor(medRes);
			NodeList textList = xpr.getRiskfactorNodes("TEXT");
			NodeList medList = xpr.getRiskfactorNodes("MEDICATION");
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			addNodeListTodoc(rootElement,textList,doc);
			Element tags = doc.createElement("TAGS");
			rootElement.appendChild(tags);
			addNodeListTodoc(tags, medList,doc);
			//get other from old one'
			if(hlres!=null){
				xpr = new XMLpreprocessor(hlres);
				NodeList lipList = xpr.getRiskfactorNodes("HYPERLIPIDEMIA");
				addNodeListTodoc(tags, lipList,doc);
			}
			if(htres!=null){
				xpr = new XMLpreprocessor(htres);
				NodeList tensList = xpr.getRiskfactorNodes("HYPERTENSION");
				addNodeListTodoc(tags, tensList,doc);
			}
			if(shRes!=null){
				xpr = new XMLpreprocessor(shRes);
				NodeList smokeList = xpr.getRiskfactorNodes("SMOKER");
				addNodeListTodoc(tags, smokeList,doc);
			}
			
			if(fhRes!=null){
				xpr = new XMLpreprocessor(fhRes);
				NodeList famList = xpr.getRiskfactorNodes("FAMILY_HIST");
				addNodeListTodoc(tags, famList,doc);
			}
			
			if(diaRes!=null){
				xpr = new XMLpreprocessor(diaRes);
				NodeList diaList = xpr.getRiskfactorNodes("DIABETES");
				addNodeListTodoc(tags, diaList,doc);
			}
			
			if(oberes!=null){
				xpr = new XMLpreprocessor(oberes);
				NodeList obeList = xpr.getRiskfactorNodes("OBESE");
				addNodeListTodoc(tags, obeList,doc);
			}
			
			
			//NodeList cadList = xpr.getMedicationNodes("CAD");
			//get htn for htnres

			//write to file

			//write to file
			writeDocToFile(medRes.getName(),outFolder,doc);
			//break;
		}

	}
	private static void writeDocToFile(String name, File outFolder, Document doc) throws Exception {
		// TODO Auto-generated method stub
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		//for pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);

		//write to console or file

		if(outFolder==null||!outFolder.exists()){
			outFolder = new File(outFolder.getPath());
			outFolder.mkdir();
		}
		System.out.println("writing file: "+name);
		StreamResult file = new StreamResult(new File(outFolder,name));
		//write data
		transformer.transform(source, file);
	}
	private static void addNodeListTodoc(Element rootElement, NodeList nodeList, Document doc) {
		int size = nodeList.getLength();
		List<Node> listNode = new LinkedList<Node>();
		if (size>0) {
			for (int i = 0; i < size; i++) {
				listNode.add(nodeList.item(i));
			}
		}
		for (int i = 0; i < listNode.size(); i++) {
			Node node = doc.importNode(listNode.get(i),true);
			//if list contains same node discard
			Element elem = (Element) node;
			if(elem.getNodeName().equals("HYPERTENSION")){
				String indicator = elem.getAttribute("indicator");
				//System.out.println(time+ indicator);
				if(indicator.equals("high bp")){
					elem.setAttribute("time", "during DCT");
					
				}
				}
			rootElement.appendChild(node);
			

		}
		

	}
	private static Node findhyperTimeAttr(Node node, List<Node> listNode) {
		for (Node node2 : listNode) {

			if (node2.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) node2;
				if(elem.getNodeName().equals("HYPERTENSION")){
					String time = elem.getAttribute("time");
					String indicator = elem.getAttribute("indicator");
					//System.out.println(time+ indicator);
					if(indicator.equals("high bp")){
						elem.setAttribute("time", "during DCT");
						return node2;
					}
					}
				}
			}
		
		return null;
	}

}
