package tcrn.tbi.tm.runner;

import java.io.File;
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

import tcrn.tbi.tm.i2b2.RiskFactorsAnnotation;
import tcrn.tbi.tm.i2b2.riskfactors.FamilyHistoryRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HyperlipidemiaRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.HypertensionRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.MedicationRiskFactor;
import tcrn.tbi.tm.i2b2.riskfactors.SmokingHistoryRiskFactor;
import tcrn.tbi.tm.ner.HyperlipidemiaAnnotation;
import tcrn.tbi.tm.ner.HypertensionAnnotation;
import tcrn.tbi.tm.ner.MedicationAnnotation;

public class I2B2SystemOutputWriter {
	private String outFolderPath;
	static final Logger logger = LogManager.getLogger(I2B2SystemOutputWriter.class.getName());

	/**
	 * @return the outFolderPath
	 */
	public String getOutFolderPath() {
		return outFolderPath;
	}
	/**
	 * @param outFolderPath the outFolderPath to set
	 */
	public void setOutFolderPath(String outFolderPath) {
		File outFolder = new File(outFolderPath);
		if (!outFolder.exists()) {
			outFolder.mkdir();
		}
		this.outFolderPath = outFolderPath;
	}

	public void writeOutput(List<RiskFactorsAnnotation> riskFactorsAnnotations,String type){
		for (RiskFactorsAnnotation riskFactorsAnnotation : riskFactorsAnnotations) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			File outFolder = new File(this.outFolderPath);
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.newDocument();
				Element rootElement = doc.createElement("root");
				doc.appendChild(rootElement);
				Element text = doc.createElement("TEXT");
				CDATASection cdata = doc.createCDATASection("mycdata");
				cdata.setTextContent(riskFactorsAnnotation.getCoveredText());
				text.appendChild(cdata);
				rootElement.appendChild(text);
				//create tags
				Element tags = doc.createElement("TAGS");
				rootElement.appendChild(tags);

				if (type.equals("complete")) {
					//getAnnotation
					logger.info("writing complete file for "+riskFactorsAnnotation.getFileName());
					writeMedAnnotationComplete(tags, doc, riskFactorsAnnotation);
					writeSHAnnotationComplete(tags,doc,riskFactorsAnnotation);
					writeFHAnnotationComplete(tags,doc,riskFactorsAnnotation);
					writeHypertensionAnnotationComplete(tags,doc,riskFactorsAnnotation);
					writeHyperlipidemiaAnnotationComplete(tags,doc,riskFactorsAnnotation);
				}
				if (type.equals("sys")) {
					logger.info("writing system out file for "+riskFactorsAnnotation.getFileName());
					writeMedAnnotationSystem(tags, doc, riskFactorsAnnotation);
					writeSHAnnotationSystem(tags, doc,riskFactorsAnnotation);
					writeFHAnnotationSystem(tags, doc,riskFactorsAnnotation);
					writeHypertensionAnnotationSystem(tags, doc,riskFactorsAnnotation);
					writeHyperlipidemiaAnnotationSystem(tags, doc,riskFactorsAnnotation);
				}
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				//for pretty print
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(doc);

				//write to console or file

				if(outFolder==null||!outFolder.exists()){
					outFolder = new File(outFolderPath);
					outFolder.mkdir();
				}
				//System.out.println("writing file: "+riskFactorsAnnotation.getFileName());
				StreamResult file = new StreamResult(new File(outFolder,riskFactorsAnnotation.getFileName()));
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

	}
	private void writeHyperlipidemiaAnnotationSystem(Element tags,
			Document doc, RiskFactorsAnnotation riskFactorsAnnotation) {
		for (HyperlipidemiaRiskFactor hrf : riskFactorsAnnotation.getHyperlipidemia()) {
			String indicator = hrf.getIndicator();
			String id = hrf.getId();
			String time = hrf.getTime();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element hyperElem = doc.createElement("HYPERLIPIDEMIA");
					hyperElem.setAttribute("id", id);
					if (i==0) {
						hyperElem.setAttribute("time", "before DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}else if(i==1){
						hyperElem.setAttribute("time", "during DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}else{
						hyperElem.setAttribute("time", "after DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}

				}
			}else{
				Element hyperElem = doc.createElement("HYPERLIPIDEMIA");
				hyperElem.setAttribute("id", id);
				hyperElem.setAttribute("time", time);
				hyperElem.setAttribute("indicator", indicator);
				tags.appendChild(hyperElem);
			}
		}
		
	}
	private void writeHyperlipidemiaAnnotationComplete(Element tags,
			Document doc, RiskFactorsAnnotation riskFactorsAnnotation) {
		for (HyperlipidemiaRiskFactor hrf : riskFactorsAnnotation.getHyperlipidemia()) {
			String indicator = hrf.getIndicator();
			String id = hrf.getId();
			String time = hrf.getTime();
			HyperlipidemiaAnnotation hyperAnn = hrf.getHlAnn();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element hyperElem = doc.createElement("HYPERLIPIDEMIA");
					hyperElem.setAttribute("id", id);
					if (i==0) {
						hyperElem.setAttribute("time", "before DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERLIPIDEMIA");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "before DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}else if(i==1){
						hyperElem.setAttribute("time", "during DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERLIPIDEMIA");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "during DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}else{
						hyperElem.setAttribute("time", "after DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERLIPIDEMIA");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "after DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}

				}
			}else{
				Element hyperElem = doc.createElement("HYPERLIPIDEMIA");
				hyperElem.setAttribute("id", id);
				hyperElem.setAttribute("time", time);
				hyperElem.setAttribute("indicator", indicator);
				Element hyperChildElem = doc.createElement("HYPERLIPIDEMIA");
				hyperChildElem.setAttribute("id", id);
				hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
				hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
				hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
				hyperChildElem.setAttribute("time", time);
				hyperChildElem.setAttribute("indicator", indicator);
				hyperChildElem.setAttribute("comment", "");
				hyperElem.appendChild(hyperChildElem);
				tags.appendChild(hyperElem);
			}
		}

		
	}
	private void writeHypertensionAnnotationSystem(Element tags, Document doc,
			RiskFactorsAnnotation riskFactorsAnnotation) {
		for (HypertensionRiskFactor hrf : riskFactorsAnnotation.getHypertension()) {
			String indicator = hrf.getIndicator();
			String id = hrf.getId();
			String time = hrf.getTime();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element hyperElem = doc.createElement("HYPERTENSION");
					hyperElem.setAttribute("id", id);
					if (i==0) {
						hyperElem.setAttribute("time", "before DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}else if(i==1){
						hyperElem.setAttribute("time", "during DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}else{
						hyperElem.setAttribute("time", "after DCT");
						hyperElem.setAttribute("indicator", indicator);
						tags.appendChild(hyperElem);
					}

				}
			}else{
				Element hyperElem = doc.createElement("HYPERTENSION");
				hyperElem.setAttribute("id", id);
				hyperElem.setAttribute("time", time);
				hyperElem.setAttribute("indicator", indicator);
				tags.appendChild(hyperElem);
			}
		}
	}
	private void writeHypertensionAnnotationComplete(Element tags,
			Document doc, RiskFactorsAnnotation riskFactorsAnnotation) {
		for (HypertensionRiskFactor hrf : riskFactorsAnnotation.getHypertension()) {
			String indicator = hrf.getIndicator();
			String id = hrf.getId();
			String time = hrf.getTime();
			HypertensionAnnotation hyperAnn = hrf.getHypertensionAnnotation();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element hyperElem = doc.createElement("HYPERTENSION");
					hyperElem.setAttribute("id", id);
					if (i==0) {
						hyperElem.setAttribute("time", "before DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERTENSION");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "before DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}else if(i==1){
						hyperElem.setAttribute("time", "during DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERTENSION");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "during DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}else{
						hyperElem.setAttribute("time", "after DCT");
						hyperElem.setAttribute("indicator", indicator);
						Element hyperChildElem = doc.createElement("HYPERTENSION");
						hyperChildElem.setAttribute("id", id);
						hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
						hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
						hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
						hyperChildElem.setAttribute("time", "after DCT");
						hyperChildElem.setAttribute("indicator", indicator);
						hyperChildElem.setAttribute("comment", "");
						hyperElem.appendChild(hyperChildElem);
						tags.appendChild(hyperElem);
					}

				}
			}else{
				Element hyperElem = doc.createElement("HYPERTENSION");
				hyperElem.setAttribute("id", id);
				hyperElem.setAttribute("time", time);
				hyperElem.setAttribute("indicator", indicator);
				Element hyperChildElem = doc.createElement("HYPERTENSION");
				hyperChildElem.setAttribute("id", id);
				hyperChildElem.setAttribute("start",hyperAnn.getStartOffset().toString() );
				hyperChildElem.setAttribute("end", hyperAnn.getEndOffset().toString());
				hyperChildElem.setAttribute("text", hyperAnn.getCoveredText());
				hyperChildElem.setAttribute("time", time);
				hyperChildElem.setAttribute("indicator", indicator);
				hyperChildElem.setAttribute("comment", "");
				hyperElem.appendChild(hyperChildElem);
				tags.appendChild(hyperElem);
			}
		}

	}
	private void writeFHAnnotationSystem(Element tags, Document doc,
			RiskFactorsAnnotation riskFactorsAnnotation) {
		FamilyHistoryRiskFactor fmHist = riskFactorsAnnotation.getFamilyHistory();
		String fhIndicator = fmHist.getIndicator();
		Element fhElem = doc.createElement("FAMILY_HIST");
		String id = fmHist.getId();
		fhElem.setAttribute("id", id);
		fhElem.setAttribute("indicator", fhIndicator);
		tags.appendChild(fhElem);

	}
	private void writeFHAnnotationComplete(Element tags, Document doc,
			RiskFactorsAnnotation riskFactorsAnnotation) {
		FamilyHistoryRiskFactor fmHist = riskFactorsAnnotation.getFamilyHistory();
		String fhIndicator = fmHist.getIndicator();
		Element fhElem = doc.createElement("FAMILY_HIST");
		String id = fmHist.getId();
		fhElem.setAttribute("id", id);
		fhElem.setAttribute("indicator", fhIndicator);
		tags.appendChild(fhElem);

	}
	private void writeSHAnnotationSystem(Element tags, Document doc,
			RiskFactorsAnnotation riskFactorsAnnotation) {
		SmokingHistoryRiskFactor smHist = riskFactorsAnnotation.getSmokingHistory();
		String smokingStatus = smHist.getStatus();
		Element smokingElem = doc.createElement("SMOKER");
		String id = smHist.getId();
		smokingElem.setAttribute("id", id);
		smokingElem.setAttribute("status", smokingStatus);
		tags.appendChild(smokingElem);

	}
	private void writeSHAnnotationComplete(Element tags, Document doc,
			RiskFactorsAnnotation riskFactorsAnnotation) {
		SmokingHistoryRiskFactor smHist = riskFactorsAnnotation.getSmokingHistory();
		String smokingStatus = smHist.getStatus();
		Element smokingElem = doc.createElement("SMOKER");
		String id = smHist.getId();
		smokingElem.setAttribute("id", id);
		smokingElem.setAttribute("status", smokingStatus);
		Element smokingChildElem = doc.createElement("SMOKER");
		smokingChildElem.setAttribute("id", id);
		smokingChildElem.setAttribute("status", smokingStatus);
		smokingElem.appendChild(smokingChildElem);
		tags.appendChild(smokingElem);

	}
	private void writeMedAnnotationSystem(Element tags, Document doc,
			RiskFactorsAnnotation riskFactor) {
		List<MedicationRiskFactor> meds = riskFactor.getMedications();
		for (MedicationRiskFactor medicationRiskFactor : meds) {
			String id = medicationRiskFactor.getId();
			String time = medicationRiskFactor.getTime();
			String type1 = medicationRiskFactor.getType1();
			String type2 = medicationRiskFactor.getType2();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element medicationElem = doc.createElement("MEDICATION");
					medicationElem.setAttribute("id", id);
					if (i==0) {
						medicationElem.setAttribute("time", "before DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						tags.appendChild(medicationElem);
					}else if(i==1){
						medicationElem.setAttribute("time", "during DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						tags.appendChild(medicationElem);
					}else{
						medicationElem.setAttribute("time", "after DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						tags.appendChild(medicationElem);
					}
				}
			}else{
				Element medicationElem = doc.createElement("MEDICATION");
				medicationElem.setAttribute("id", id);
				medicationElem.setAttribute("time", time);
				medicationElem.setAttribute("type1",type1);
				medicationElem.setAttribute("type2", type2);
				tags.appendChild(medicationElem);
			}
		}
	}
	private void writeMedAnnotationComplete(Element tags, Document doc,
			RiskFactorsAnnotation riskFactor) {
		List<MedicationRiskFactor> meds = riskFactor.getMedications();
		for (MedicationRiskFactor medicationRiskFactor : meds) {
			MedicationAnnotation medAnn = medicationRiskFactor.getMedAnnotation();
			String coveredText = medAnn.getCoveredText();
			Integer startOffset = medAnn.getStartOffset();
			Integer endOffset = medAnn.getEndOffset();
			String id = medicationRiskFactor.getId();
			String time = medicationRiskFactor.getTime();
			String type1 = medicationRiskFactor.getType1();
			String type2 = medicationRiskFactor.getType2();
			if(time.equalsIgnoreCase("continuing")){
				for (int i = 0; i < 3; i++) {
					Element medicationElem = doc.createElement("MEDICATION");
					medicationElem.setAttribute("id", id);
					if (i==0) {
						medicationElem.setAttribute("time", "before DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						Element medicationChildElem = doc.createElement("MEDICATION");
						medicationChildElem.setAttribute("id", "M0");
						medicationChildElem.setAttribute("start", startOffset.toString());
						medicationChildElem.setAttribute("end", endOffset.toString());
						medicationChildElem.setAttribute("text", coveredText);
						medicationChildElem.setAttribute("time", "before DCT");
						medicationChildElem.setAttribute("type1", type1);
						medicationChildElem.setAttribute("type2", type2);
						medicationElem.appendChild(medicationChildElem);
						tags.appendChild(medicationElem);
					}else if(i==1){
						medicationElem.setAttribute("time", "during DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						Element medicationChildElem = doc.createElement("MEDICATION");
						medicationChildElem.setAttribute("id", "M0");
						medicationChildElem.setAttribute("start", startOffset.toString());
						medicationChildElem.setAttribute("end", endOffset.toString());
						medicationChildElem.setAttribute("text", coveredText);
						medicationChildElem.setAttribute("time", "during DCT");
						medicationChildElem.setAttribute("type1", type1);
						medicationChildElem.setAttribute("type2", type2);
						medicationElem.appendChild(medicationChildElem);
						tags.appendChild(medicationElem);
					}else{
						medicationElem.setAttribute("time", "after DCT");
						medicationElem.setAttribute("type1",type1);
						medicationElem.setAttribute("type2", type2);
						tags.appendChild(medicationElem);
						Element medicationChildElem = doc.createElement("MEDICATION");
						medicationChildElem.setAttribute("id", "M0");
						medicationChildElem.setAttribute("start", startOffset.toString());
						medicationChildElem.setAttribute("end", endOffset.toString());
						medicationChildElem.setAttribute("text", coveredText);
						medicationChildElem.setAttribute("time", "after DCT");
						medicationChildElem.setAttribute("type1", type1);
						medicationChildElem.setAttribute("type2", type2);
						medicationElem.appendChild(medicationChildElem);
					}
				}
			}
			else{
				Element medicationElem = doc.createElement("MEDICATION");
				medicationElem.setAttribute("id", id);
				medicationElem.setAttribute("time", time);
				medicationElem.setAttribute("type1",type1);
				medicationElem.setAttribute("type2", type2);
				tags.appendChild(medicationElem);
				Element medicationChildElem = doc.createElement("MEDICATION");
				medicationChildElem.setAttribute("id", "M0");
				medicationChildElem.setAttribute("start", startOffset.toString());
				medicationChildElem.setAttribute("end", endOffset.toString());
				medicationChildElem.setAttribute("text", coveredText);
				medicationChildElem.setAttribute("time", time);
				medicationChildElem.setAttribute("type1", type1);
				medicationChildElem.setAttribute("type2", type2);
				medicationElem.appendChild(medicationChildElem);
			}
		}
	}
}