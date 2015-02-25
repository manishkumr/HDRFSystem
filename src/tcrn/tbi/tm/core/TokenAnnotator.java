package tcrn.tbi.tm.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class TokenAnnotator implements Annotator{
	static final Logger logger = LogManager.getLogger(TokenAnnotator.class.getName());
	final InputStream tokenModelInput = TokenAnnotator.class.getResourceAsStream("/en-token.bin");
	@Override
	public Annotation annotate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileAnnotation annotate(FileAnnotation annFile) {
		//logger.info("Getting token annotation");
		TokenizerModel tokenModel = null;
		Tokenizer tokenizer = null;
		try {
			tokenModel = new TokenizerModel(tokenModelInput);
			tokenizer = new TokenizerME(tokenModel);
		} catch (IOException e) {
			logger.error(e.getMessage());
			
		}
		if(tokenizer!=null){
			List<SentenceAnnotation> sentList = annFile.getSentenceAnnList();
			for (SentenceAnnotation sentenceAnnotation : sentList) {
				String sentence = sentenceAnnotation.getCoveredText();
				//get token
				String [] tokens = tokenizer.tokenize(sentence);
				Span[] tokenPosition = tokenizer.tokenizePos(sentence);
				List<TokenAnnotation> tokenList = new LinkedList<TokenAnnotation>();
				for (int i = 0; i < tokenPosition.length; i++) {
					String coveredText = tokens[i];
					Integer startOffset = tokenPosition[i].getStart();
					Integer endOffset = tokenPosition[i].getEnd();
					TokenAnnotation tokenAnnotation = new TokenAnnotation();
					tokenAnnotation.setCoveredText(coveredText);
					tokenAnnotation.setStartOffset(startOffset);
					tokenAnnotation.setEndOffset(endOffset);
					//logger.debug(tokenAnnotation.getCoveredText());
					tokenList.add(tokenAnnotation);
				}
				sentenceAnnotation.setTokenList(tokenList);
			}
		}
		return annFile;
	}

}
