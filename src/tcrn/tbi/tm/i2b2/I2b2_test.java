package tcrn.tbi.tm.i2b2;

import java.io.File;

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

public class I2b2_test {
	
	static String allOutPath = "D:\\TBI_Datasets\\mergedResults";
	static String smokingOutPath = "D:\\TBI_Datasets\\smokingresults\\system";
	
	public static void main(String[] args) {
		try {
			process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void process() throws Exception {
		File[] allOutFiles = new File(allOutPath).listFiles();
		File [] smokeOutFiles = new File(smokingOutPath).listFiles();
		for (File allFile : allOutFiles) {
			//find file in smoke_Out
			File foundFile = I2B2Utils.findGoldFile(allFile.getName(),smokeOutFiles );
			System.out.println(foundFile.getName());
			//get smoking value 
			XMLpreprocessor xpr = new XMLpreprocessor(foundFile);
			NodeList smokList = xpr.getRiskfactorNodes("SMOKER");
			Element elem = (Element) smokList.item(0);
			String status = elem.getAttribute("status");
			//System.out.println(status);
			//get smoking val
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(allFile);
			
			Node statusNode = doc.getElementsByTagName("SMOKER").item(0).getAttributes().getNamedItem("status");
			System.out.println(statusNode.getNodeValue());
			statusNode.setTextContent(status);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("D:\\TBI_Datasets\\mergedResults_new\\"+allFile.getName()));
			transformer.transform(source, result);
			/*XMLpreprocessor xpr1 = new XMLpreprocessor(allFile);
			xpr1.getRiskfactorNodes("SMOKER").item(0).getAttributes().getNamedItem("status").setNodeValue(status);;
			NodeList textList = xpr1.getRiskfactorNodes("TEXT");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			addNodeListTodoc(rootElement,textList,doc);
			Element tags = doc.createElement("TAGS");
			rootElement.appendChild(tags);
			addNodeListTodoc(tags, medList,doc);*/
			
			
			
		}
	}

}
