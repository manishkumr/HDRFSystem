package tcrn.tbi.tm.i2b2;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tcrn.tbi.tm.documents.XMLpreprocessor;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.ner.MedicationAnnotation;
import tcrn.tbi.tm.util.I2B2Utils;

public class Evaluator {
	private String completeGold;
	private String completeSys ;
	private String sysOut;
	private String gold;
	
	/**
	 * @return the gold
	 */
	public String getGold() {
		return gold;
	}
	/**
	 * @param gold the gold to set
	 */
	public void setGold(String gold) {
		this.gold = gold;
	}
	/**
	 * @return the completeGold
	 */
	public String getCompleteGold() {
		return completeGold;
	}
	/**
	 * @param completeGold the completeGold to set
	 */
	public void setCompleteGold(String completeGold) {
		this.completeGold = completeGold;
	}
	/**
	 * @return the completeSys
	 */
	public String getCompleteSys() {
		return completeSys;
	}
	/**
	 * @param completeSys the completeSys to set
	 */
	public void setCompleteSys(String completeSys) {
		this.completeSys = completeSys;
	}
	
	public static void main(String [] args) {
		Evaluator evaluator = new Evaluator();
		evaluator.setCompleteGold("D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014-  Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\test\\testing-RiskFactors-Complete\\xmlFiles");
		evaluator.setCompleteSys("complete_out");
		evaluator.setSysOut("sysOut");
		evaluator.setGold("D:\\DropBox\\Dropbox\\TCRN DTM Projects\\Data,Text Mining, IE, IR, NLP\\ST - i2b2 2014\\ST- i2b2 -2014-  Task 2 - CVD Risk Factors\\Datasets\\2014\\Track2\\test\\testing-RiskFactors-Gold\\xmlFiles");
		try {
			evaluator.evaluateComplete();
			evaluator.SysAnnCount();
			evaluator.evaluateSys();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void evaluateSys() throws Exception {
		//get Gold Annotation
		File [] goldSys = new File(this.gold).listFiles();
		File [] sysOutFolder = new File(this.sysOut).listFiles();
		for (File gold : goldSys) {
			File sysFile = I2B2Utils.findGoldFile(gold.getName(), sysOutFolder);
			XMLpreprocessor goldProcess = new XMLpreprocessor(gold);
			NodeList goldmedNodeList = goldProcess.getRiskfactorNodes("MEDICATION");
			XMLpreprocessor sysProcess = new XMLpreprocessor(sysFile);
			NodeList sysmedNodeList = sysProcess.getRiskfactorNodes("MEDICATION");
			System.out.print(sysFile.getName()+ "\t");
			compareMedAnnotation(goldmedNodeList,sysmedNodeList);
			
		}
		//compare time attr and type 
		
		
	}
	int sysTPcount = 0;
	int sysFNcount = 0;
	private void compareMedAnnotation(NodeList goldmedNodeList,
			NodeList sysmedNodeList) {
		for (int i = 0; i < goldmedNodeList.getLength(); i++) {
			Node node = goldmedNodeList.item(i);
			String goldtime = (node.getAttributes()
					.getNamedItem("time").getNodeValue());
			String goldtype1 = node.getAttributes()
					.getNamedItem("type1").getNodeValue();
			boolean flag = false;
			for (int j = 0; j < sysmedNodeList.getLength(); j++) {
				String systime = (node.getAttributes()
						.getNamedItem("time").getNodeValue());
				String systype1 = node.getAttributes()
						.getNamedItem("type1").getNodeValue();
				if(systime.equals(goldtime)&& systype1.equals(goldtype1)){
					sysTPcount++;
					flag = true;
					break;
				}
			}
			if(!flag){
				sysFNcount++;
			}
			
		}
		System.out.println(sysTPcount+" "+ sysFNcount);
		
	}
	private void SysAnnCount() throws Exception {
		File [] sysOutFiles = new File(this.sysOut).listFiles();
		int sysAnnCount = 0;
		for (File file : sysOutFiles) {
			
			XMLpreprocessor goldProcess = new XMLpreprocessor(file);
			NodeList medNodeList = goldProcess.getRiskfactorNodes("MEDICATION");
			sysAnnCount+=medNodeList.getLength();
		}
		System.out.println("SysAnnCount : "+sysAnnCount);
		
	}
	private void evaluateComplete() throws Exception {
		File [] goldFiles = new File(this.completeGold).listFiles();
		File [] sysFiles = new File(this.completeSys).listFiles();
		FileWriter fw = new FileWriter("Med_Eval.csv");
		int goldAnnCount = 0;
		int sysAnnCount = 0;
		for (File goldFile : goldFiles) {
			//get gold File ann
			FileAnnotation annFile = new FileAnnotation();
			annFile.setFileName(goldFile.getName());
			RiskFactorGoldAnnotation  goldAnn = new RiskFactorGoldAnnotation();
			goldAnn.setRiskFactor("MEDICATION");
			goldAnn.setGoldFolderPath(this.completeGold);
			List<MedicationAnnotation> goldMedList = goldAnn.getMedicationAnnotation(annFile);
			goldAnnCount+=goldMedList.size();
			//get sysoutAnn
			File sysFile = I2B2Utils.findGoldFile(goldFile.getName(), sysFiles);
			goldAnn.setGoldFolderPath(this.completeSys);
			annFile.setFileName(sysFile.getName());
			List<MedicationAnnotation> sysMedList = goldAnn.getMedicationAnnotation(annFile);
			sysAnnCount+=sysMedList.size();
			//for TP and Fn
			
			compareAnnotations(goldMedList,sysMedList,fw,goldFile.getName());
			//for FP
			compareAnnotationFP(goldMedList,sysMedList,fw,goldFile.getName());
			
			
		}
		System.out.println(goldAnnCount+" "+ sysAnnCount);
		//get fP
				fw.close();
		
	}
	private void compareAnnotationFP(List<MedicationAnnotation> goldMedList,
			List<MedicationAnnotation> sysMedList, FileWriter fw, String thisFileNAme) throws Exception {
		for (MedicationAnnotation medicationAnnotation : sysMedList) {
			Integer sysStart = medicationAnnotation.getStartOffset();
			Integer sysEnd = medicationAnnotation.getEndOffset();
			boolean ifFound = findSysInGold(sysStart,sysEnd,goldMedList);
			if(!ifFound){
				fw.write(thisFileNAme+",,,,\""+medicationAnnotation.getCoveredText()+"\","+sysStart+","+sysEnd+",FP\n");
			}
		}
		
	}
	private boolean findSysInGold(Integer sysStart, Integer sysEnd,
			List<MedicationAnnotation> goldMedList) {
		for (MedicationAnnotation goldMedAnn : goldMedList) {
			Integer goldStart = goldMedAnn.getStartOffset();
			Integer goldEnd = goldMedAnn.getEndOffset();
			if(goldStart-10<sysStart && sysEnd<goldEnd+10){
				return true;
			}
		}
		return false;
		
	}
	private void compareAnnotations(List<MedicationAnnotation> goldMedList,
			List<MedicationAnnotation> sysMedList, FileWriter fw, String thisFileName) throws Exception {
		int fnCount = 0;
		int tpCount = 0;
		for (MedicationAnnotation goldmedicationAnnotation : goldMedList) {
			String goldText = (goldmedicationAnnotation.getCoveredText());
			Integer goldstart = goldmedicationAnnotation.getStartOffset();
			Integer goldend = goldmedicationAnnotation.getEndOffset();
			
			MedicationAnnotation annFound = findgoldInSys(goldstart,goldend,sysMedList);
			if(annFound!=null){
				fw.write(thisFileName+",\""+goldText+"\","+ goldstart+ ","+ goldend+",");
				fw.write(annFound.getCoveredText()+","+ annFound.getStartOffset()+ ","+ annFound.getEndOffset()+",TP"+"\n");
				tpCount++;
			}else{
				fnCount++;
				fw.write(thisFileName+",\""+goldText+"\","+ goldstart+ ","+ goldend+","+",,,FN\n");
			}
		}
		System.out.println(tpCount+" "+ fnCount);
		
	}
	private MedicationAnnotation findgoldInSys(Integer goldstart, Integer goldend,
			List<MedicationAnnotation> sysMedList) {
		for (MedicationAnnotation medicationAnnotation : sysMedList) {
			Integer sysStart = medicationAnnotation.getStartOffset();
			Integer sysEnd = medicationAnnotation.getEndOffset();
			if(goldstart-10<sysStart && sysEnd<goldend+10){
				return medicationAnnotation;
			}
			
		}
		return null;
	}
	/**
	 * @return the sysOut
	 */
	public String getSysOut() {
		return sysOut;
	}
	/**
	 * @param sysOut the sysOut to set
	 */
	public void setSysOut(String sysOut) {
		this.sysOut = sysOut;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
