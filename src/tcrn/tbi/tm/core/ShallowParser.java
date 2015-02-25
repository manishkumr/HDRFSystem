package tcrn.tbi.tm.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class ShallowParser implements Annotator{
	private static final Logger logger = LogManager.getLogger(ShallowParser.class.getName());
	private final InputStream chunkerModelInput = PosAnnotator.class.getResourceAsStream("/en-chunker.bin");
	private ChunkerME chunker = null;
	@Override
	public Annotation annotate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileAnnotation annotate(FileAnnotation annFile) {
		logger.info("getting NP chunks");
		ChunkerModel chunkerModel = null;
		
		try {
			chunkerModel = new ChunkerModel(chunkerModelInput);
			chunker = new ChunkerME(chunkerModel);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		if(chunker!=null){
			List<SentenceAnnotation> sentenceList = annFile.getSentenceAnnList();
			for (SentenceAnnotation sentenceAnnotation : sentenceList) {
				List<TokenAnnotation> tokens = sentenceAnnotation.getTokenList();
				getChunks(tokens);
				List<TokenAnnotation> normalizedtokens = sentenceAnnotation.getNormalizedTokenList();
				getChunks(normalizedtokens);
			}
		}
		return annFile;
	}
	private void getChunks(List<TokenAnnotation> tokens) {
		String [] tokenArray = getTokens(tokens);
		String [] tokenPosArray = getTokensPos(tokens);
		String[] chunks = chunker.chunk(tokenArray, tokenPosArray);
		for (int i = 0; i < tokens.size(); i++) {
			tokens.get(i).setTags(chunks[i]);
			//logger.debug(tokens.get(i).getCoveredText()+"/"+tokens.get(i).getPos()+"/"+chunks[i]);
		}
		
	}

	/**
	 * @param tokens
	 * @return tokensArray String Array of tokens
	 */
	private String[] getTokens(List<TokenAnnotation> tokens) {
		String [] tokensArray = new String[tokens.size()] ;
		int i = 0;
		for (TokenAnnotation tokenAnnotation : tokens) {
			tokensArray[i]= tokenAnnotation.getCoveredText();
			i++;
		}
		return tokensArray;
	}
	/**
	 * @param tokens
	 * @return tokensPosArray String Array containing POS for tokens
	 */
	private String[] getTokensPos(List<TokenAnnotation> tokens) {
		String [] tokensPosArray = new String[tokens.size()] ;
		int i = 0;
		for (TokenAnnotation tokenAnnotation : tokens) {
			tokensPosArray[i]= tokenAnnotation.getPos();
			i++;
		}
		return tokensPosArray;
	}
}
