package tcrn.tbi.tm.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SectionAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.util.I2B2Utils;

public class SectionAnnotator implements Annotator{
	static final Logger logger = LogManager.getLogger(SectionAnnotator.class.getName());
	private String sectionGoldPath ;
	
	/**
	 * @return the sectionGoldPath
	 */
	public String getSectionGoldPath() {
		return sectionGoldPath;
	}

	/**
	 * @param sectionGoldPath the sectionGoldPath to set
	 */
	public void setSectionGoldPath(String sectionGoldPath) {
		this.sectionGoldPath = sectionGoldPath;
	}

	@Override
	public Annotation annotate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileAnnotation annotate(FileAnnotation annFile) {
		try {
			File goldSectionFolder = new File(this.sectionGoldPath);
			File [] goldSecFiles = goldSectionFolder.listFiles();
			File matchedGoldFile = I2B2Utils.findGoldFile(annFile.getFileName(), goldSecFiles);
			if(matchedGoldFile!=null){
				getGoldAnnotation(matchedGoldFile, annFile);
			}else{
				logger.warn("Section info not found for file:"+ annFile.getFileName());	
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return annFile;
	}
	private void getGoldAnnotation(File goldFile, FileAnnotation annotatedFile) throws Exception {
		FileReader fr = new FileReader(goldFile);
		BufferedReader br = new BufferedReader(fr);
		List<SectionAnnotation> sectionAnnotations = new LinkedList<SectionAnnotation>();
		String line;
		while((line=br.readLine())!=null){
			if(line.contains("Section")){
				String [] lineSplit = line.split("\t");
				Integer startOffset = Integer.parseInt(lineSplit[1].split(" ")[1]);
				Integer endOffset = Integer.parseInt(lineSplit[1].split(" ")[2]);
				//System.out.println(startOffset+" "+ endOffset);
				SectionAnnotation secAnn = new SectionAnnotation();
				secAnn.setCoveredText(lineSplit[2]);
				secAnn.setStartOffset(startOffset);
				secAnn.setEndOffset(endOffset);
				getSectionLineIndex(secAnn,annotatedFile);

				//annotatedFile.getSentenceAnnList().get(54);
				sectionAnnotations.add(secAnn);
			}
		}
		annotatedFile.setSectionAnnotation(sectionAnnotations);
		br.close();
		fr.close();
	}
	private void getSectionLineIndex(SectionAnnotation secAnn, FileAnnotation annotatedFile) {
		List<SentenceAnnotation> sentenceAnnList = annotatedFile.getSentenceAnnList();
		int sentenceIndex = 0;
		for (SentenceAnnotation sentenceAnnotation : sentenceAnnList) {
			Integer sentenceStartOffset = sentenceAnnotation.getStartOffset();
			Integer sentenceEndOffset = sentenceAnnotation.getEndOffset();
			Integer sectionStartOffset = secAnn.getStartOffset();
			Integer sectionEndOffset = secAnn.getEndOffset();
			if(sentenceStartOffset<=sectionStartOffset && sentenceEndOffset>=sectionEndOffset){
				//				//System.out.println("Start "+sentenceStartOffset+" "+ sectionStartOffset);
				//				//System.out.println("End "+sentenceEndOffset+" "+ sectionEndOffset);
				secAnn.setSentenceIndex(sentenceIndex);
			}
			sentenceIndex++;
		}

	}
	

}
