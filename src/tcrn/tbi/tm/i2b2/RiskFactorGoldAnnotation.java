package tcrn.tbi.tm.i2b2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tcrn.tbi.tm.documents.XMLpreprocessor;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.ner.MedicationAnnotation;
import tcrn.tbi.tm.util.I2B2Utils;

public class RiskFactorGoldAnnotation {
	private static final Logger logger = LogManager.getLogger(RiskFactorGoldAnnotation.class.getName());
	private String riskFactor;
	private String goldFolderPath;


	/**
	 * @return the goldFolderPath
	 */
	public String getGoldFolderPath() {
		return goldFolderPath;
	}

	/**
	 * @param goldFolderPath the goldFolderPath to set
	 */
	public void setGoldFolderPath(String goldFolderPath) {
		this.goldFolderPath = goldFolderPath;
	}

	/**
	 * @return the riskFactor
	 */
	public String getRiskFactor() {
		return riskFactor;
	}

	/**
	 * @param riskFactor the riskFactor to set
	 */
	public void setRiskFactor(String riskFactor) {
		this.riskFactor = riskFactor;
	}

	public List<MedicationAnnotation> getMedicationAnnotation(
			FileAnnotation annFile) throws Exception {
		List<MedicationAnnotation> medAnnList = null;
		//parse complete set
		File [] goldFileList = new File(this.goldFolderPath).listFiles();
		File goldFile = I2B2Utils.findGoldFile(annFile.getFileName(),goldFileList);
		if(goldFile!=null){
			XMLpreprocessor goldProcess = new XMLpreprocessor(goldFile);
			NodeList medNodeList = goldProcess.getRiskfactorNodes(this.riskFactor);
			medAnnList = gedMedList(medNodeList);
			//System.out.println(medNodeList.getLength());
		}else{
			logger.warn("GoldFile not found for "+annFile.getFileName());
		}
		return medAnnList;
	}

	private List<MedicationAnnotation> gedMedList(NodeList medNodeList) throws IOException {
		List<MedicationAnnotation> medAnnList = new LinkedList<MedicationAnnotation>();
		for (int i = 0; i < medNodeList.getLength(); i++) {
			Node node = medNodeList.item(i);
			if(node.hasChildNodes()){
				NodeList childNodeList = node.getChildNodes();
				for (int j = 0; j < childNodeList.getLength(); j++) {
					Node childNode = childNodeList.item(j);
					if (childNode.getNodeName().equals(this.riskFactor)){ 
						String annotatedText = (childNode.getAttributes()
								.getNamedItem("text").getNodeValue());
						String startString = childNode.getAttributes()
								.getNamedItem("start").getNodeValue();
						Integer startOff = Integer.parseInt(startString);
						String endString = childNode.getAttributes()
								.getNamedItem("end").getNodeValue();
						Integer endOff = Integer.parseInt(endString);
						MedicationAnnotation medAnn = new MedicationAnnotation();
						medAnn.setCoveredText(annotatedText);
						medAnn.setStartOffset(startOff);
						medAnn.setEndOffset(endOff);
						//what about time attr??
						String timeAttr = (childNode.getAttributes().getNamedItem("time").getNodeValue());
						medAnn.setScore(timeAttr);//currently setting as score, since goldAnn do not have score
						medAnnList.add(medAnn);
						//System.out.println(annotatedText);
						break;//to capture first elem only
					}
				}
			}
		}
		medAnnList = postProcessMedication(medAnnList);
		return medAnnList;
	}
	private static List<MedicationAnnotation> postProcessMedication(
			List<MedicationAnnotation> medAnnList ) throws IOException {
		List<MedicationAnnotation> tempAnnList = new LinkedList<MedicationAnnotation>();
		for (MedicationAnnotation annotation : medAnnList) {
			boolean during=false,after= false,before = false;
			Integer startOffSet = annotation.getStartOffset();
			Integer endOffset = annotation.getEndOffset();
			String timeAttr = annotation.getScore();
			for (MedicationAnnotation annotation2 : medAnnList) {
				Integer startOffSet2 = annotation2.getStartOffset();
				Integer endOffset2 = annotation2.getEndOffset();
				String timeAttr2 = annotation2.getScore();
				if(startOffSet2.equals(startOffSet)&&endOffset2.equals(endOffset)){
					if(timeAttr2.equals("before DCT")){
						before=true;
					}else if(timeAttr2.equals("after DCT")){
						after=true;
					}else if(timeAttr2.equals("during DCT")){
						during = true;
					}
				}
				if(during&&after&&before){
					MedicationAnnotation ann = new MedicationAnnotation();
					ann.setStartOffset(startOffSet);ann.setEndOffset(endOffset);
					ann.setCoveredText(annotation.getCoveredText());
					ann.setScore("CONTINUING");
					tempAnnList.add(ann);
					break;
				}
				
			}
			if(!(during&&after&&before)){
				MedicationAnnotation ann = new MedicationAnnotation();
				ann.setStartOffset(startOffSet);ann.setEndOffset(endOffset);
				ann.setScore(timeAttr);
				ann.setCoveredText(annotation.getCoveredText());
				tempAnnList.add(ann);
			}
		}
		return tempAnnList;
	}
}
