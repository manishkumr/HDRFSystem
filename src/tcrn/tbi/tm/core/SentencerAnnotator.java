package tcrn.tbi.tm.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.SentenceAnnotation;

/**
 * @author Manish
 *
 */
public class SentencerAnnotator implements Annotator{
	static final Logger logger = LogManager.getLogger(SentencerAnnotator.class.getName());
	private String separatorPattern ;
	final InputStream sentenceModelInput = SentencerAnnotator.class.getResourceAsStream("/en-sent.bin");
	/* (non-Javadoc)
	 * @see tcrn.tbi.tm.annotator.Annotator#annotate()
	 */
	@Override
	public Annotation annotate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see tcrn.tbi.tm.annotator.Annotator#annotate(java.lang.String)
	 */
	@Override
	public FileAnnotation annotate(FileAnnotation annotatedFile) {
		//logger.info("Detecting sentence from model");
		SentenceDetector sentDetect;	
		SentenceModel sentenceModel;
		LinkedList<SentenceAnnotation> sentenceAnnList= null;
		try {
			sentenceModel = new SentenceModel(sentenceModelInput);
			sentDetect = new SentenceDetectorME(sentenceModel);
			String fileText = annotatedFile.getCoveredText();
			String[] sentences = sentDetect.sentDetect(fileText);
			Span sentenceSpan[]= sentDetect.sentPosDetect(fileText);

			sentenceAnnList = new LinkedList<SentenceAnnotation>();
			for (int i = 0; i < sentences.length; i++) {
				//logger.debug(sentenceSpan[i].getStart()+sentences[i]+sentenceSpan[i].getEnd());
				Integer startOffset = sentenceSpan[i].getStart();
				String coveredText = sentences[i];
				//sub sentence annotation
				List<SentenceAnnotation> subSentenceAnnList = getSubsentence(coveredText,startOffset);
				sentenceAnnList.addAll(subSentenceAnnList);
			}
			annotatedFile.setSentenceAnnList(sentenceAnnList);
//			for (SentenceAnnotation sentence : sentenceAnnList) {
//				logger.debug(sentence.getStartOffset()+sentence.getCoveredText()+sentence.getEndOffset());
//			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		
		return annotatedFile;
	}

	private List<SentenceAnnotation> getSubsentence(String coveredText, Integer startOffset) {
		//check if contains white sapce only
		getSeperatorPattern(coveredText);
		//logger.info("separator is: "+separatorPattern);
		List<SentenceAnnotation> sentenceAnnotations = new LinkedList<SentenceAnnotation>();
		String trimmedText = coveredText.trim();
		int trimmedLen = trimmedText.length();
		if (trimmedLen == 0) {
			return sentenceAnnotations;
		}
		int positionOfNonWhiteSpace = 0;
		String spans[] = coveredText.split("\n");
		int position = startOffset;
		for (String s : spans) {
			String t = s.trim();
			if (t.length()>0) {
			    positionOfNonWhiteSpace = s.indexOf(t.charAt(0));
			} else {
			    positionOfNonWhiteSpace = 0;
			}
			// Might have trimmed off some at the beginning of the sentences other than the 1st (#0)
			position += positionOfNonWhiteSpace; // sf Bugs artifact 3083903: For _each_ sentence, advance past any spaces at beginning of line
			SentenceAnnotation sentence = new SentenceAnnotation();
			sentence.setCoveredText(t);
			sentence.setStartOffset(position);
			sentence.setEndOffset(position+t.length());
			sentenceAnnotations.add(sentence);
			position += (s.length()-positionOfNonWhiteSpace + 1);
		}
		
		return sentenceAnnotations;
	}

	private void getSeperatorPattern(String coveredText) {
		
		if(coveredText.contains("\n")){
			//linux and window
			if(coveredText.substring(coveredText.indexOf("\n")-1, coveredText.indexOf("\n")).equals("\r\n")){
				separatorPattern = "\r\n";
			}else{
				separatorPattern = "\n";
			}
		}else if (coveredText.contains("\r")&& !coveredText.substring(coveredText.indexOf("\r"),coveredText.indexOf("\r")+1).equals("\r\n")){
			separatorPattern = "\r";
		}
	}

}
