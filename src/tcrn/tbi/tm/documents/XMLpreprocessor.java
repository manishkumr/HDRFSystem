package tcrn.tbi.tm.documents;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Manish
 *
 */
public class XMLpreprocessor {
	
	private String fileText;
	private File xmlFile;

	/**
	 * @param xmlFile xml File to process
	 */
	public XMLpreprocessor(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	/**
	 * @return the fileText
	 */
	public String getFileText() {
		return fileText;
	}

	/**
	 * @param fileText the fileText to set
	 */
	public void setFileText(String fileText) {
		this.fileText = fileText;
	}
	
	
	/**
	 * @param textTag XML tag containing text in the document
	 * @return fileText String containg text of the file
	 * @throws IOException Cannot read xml File
	 * @throws SAXException Unable to parse XMl file
	 * @throws ParserConfigurationException cannot parse xml file
	 */
	public String getFileTextAsString(String textTag) throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("TEXT");
		Node nNode = nList.item(0);
		Element eElement = (Element) nNode;
		fileText = eElement.getTextContent();
		return fileText;
		
	}

	/**
	 * @return the xmlFile
	 */
	public File getXmlFile() {
		return xmlFile;
	}

	/**
	 * @param xmlFile the xmlFile to set
	 */
	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	public NodeList getRiskfactorNodes(String riskFactor) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList =  doc.getElementsByTagName(riskFactor);
		return nList;
		
	}

}
