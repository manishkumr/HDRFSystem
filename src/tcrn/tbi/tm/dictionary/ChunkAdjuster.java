package tcrn.tbi.tm.dictionary;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

public class ChunkAdjuster implements Annotator{
	private static final Logger logger = LogManager.getLogger(ChunkAdjuster.class.getName());
	@Override
	public Annotation annotate() {
		return null;
	}

	@Override
	public FileAnnotation annotate(FileAnnotation annFile) {
		List<SentenceAnnotation> sentences = annFile.getSentenceAnnList();

		for (int j = 0; j < sentences.size(); j++) {
			SentenceAnnotation sentence = sentences.get(j);
//			if(sentence.getCoveredText().contains("isosorbide")){
//				System.out.println(sentence.getTokenList());
//			}
			//logger.info("Sentence : "+sentence.getCoveredText());
			LinkedList<int[]> mergedNPs = getmergedNPs(sentence,"tokens");
			LinkedList<int[]> mergedNormalisedNPs = getmergedNPs(sentence, "normalisedTokens");
			
			//add NPppNP 
			String[] tokens = getTokens(sentence.getTokenList());
			String[] tags = getTokensTags(sentence.getTokenList());
			List<TokenAnnotation> mergedTokenList = getMergedNPppNPTokens(tokens, mergedNPs,tags,sentence);
			sentence.setMergedTokensNPppNP(mergedTokenList);
			
			String[] normalisedTokens = getTokens(sentence.getNormalizedTokenList());
			String[] normalisedTags = getTokensTags(sentence.getNormalizedTokenList());
			List<TokenAnnotation> mergedNormalisedTokenList = getMergedNPppNPTokens(normalisedTokens, mergedNormalisedNPs,normalisedTags,sentence);
			sentence.setMergedNormalizedTonenNPppNP(mergedNormalisedTokenList);
			
//			for (TokenAnnotation tokenAnnotation : sentence.getTokenList()) {
//				logger.info(" Token "+ tokenAnnotation.getTags()+":"+tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getNPtokens()) {
//				logger.info(" Token NP:"+ tokenAnnotation.getTags()+":"+tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getMergedTokensNPNP()) {
//				logger.info(" Token NPNP:"+ tokenAnnotation.getTags()+":"+tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getMergedTokensNPppNP()) {
//				logger.info(" token NPppNP: "+ tokenAnnotation.getTags()+":"+ tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getNormalizedTokenList()) {
//				logger.info(" Token Normalised"+ tokenAnnotation.getTags()+":"+tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getMergedNormalizedTonenNPppNP()) {
//				logger.info("  normalised Token NPPPNP:"+ tokenAnnotation.getTags()+":"+ tokenAnnotation.getCoveredText());
//			}
//			for (TokenAnnotation tokenAnnotation : sentence.getMergedNormalizedTokensNPNP()) {
//				logger.info(" normalised token NPNP: "+ tokenAnnotation.getTags()+":"+ tokenAnnotation.getCoveredText());
//			}
		}

		return annFile;
	}
	private List<TokenAnnotation> getMergedNPppNPTokens(String[] tokens, LinkedList<int[]> mergedNPs, String[] tags, SentenceAnnotation sentence) {
		
		LinkedList<String> npMergedTokens = getNPmergedTokens(tokens,mergedNPs);
		LinkedList<String> npMergedTokensTag = getNPmergedTokens(tokens,mergedNPs,tags);
		LinkedList<String> mergedChunks = getMergedChunks(npMergedTokens,npMergedTokensTag);
		LinkedList<int []> mergedChunksPost = getMergedChunksPositions(mergedChunks,sentence.getCoveredText());

		List<TokenAnnotation> mergedTokenList = new LinkedList<>();
		for (int i = 0; i < mergedChunks.size(); i++) {
			TokenAnnotation token = new TokenAnnotation();
			token.setCoveredText(mergedChunks.get(i));
			token.setStartOffset(mergedChunksPost.get(i)[0]);
			token.setEndOffset(mergedChunksPost.get(i)[1]);
			mergedTokenList.add(token);
		}
		return mergedTokenList;
	}

	private LinkedList<int[]> getmergedNPs(SentenceAnnotation sentenceAnnotation, String tokenType) {
		
		SentenceAnnotation sentence = sentenceAnnotation;
		
		String[] tokens = null;
		String[] tags = null;
		if(tokenType.equals("tokens")){
			tokens = getTokens(sentence.getTokenList());
			tags = getTokensTags(sentence.getTokenList());
		}else{
			tokens = getTokens(sentence.getNormalizedTokenList());
			tags = getTokensTags(sentence.getNormalizedTokenList());
		}
		
		// get NP chunks 
		LinkedList<int [] > mergedNPs = new LinkedList<int []>();
		for (int i = 0; i < tokens.length; i++) {
			int []  mergedNPphraseTokensPosition = nounPhraseMerger(tags, i);
			if(mergedNPphraseTokensPosition!=null){
				mergedNPs.add(mergedNPphraseTokensPosition);
			}
		}
		//set NP chunks
		List<TokenAnnotation> npChunksToken = new LinkedList<>();
		for (int[] is : mergedNPs) {
			int startIndex = sentence.getTokenList().get(is[0]).getStartOffset();
			int endIndex = sentence.getTokenList().get(is[1]).getEndOffset();
			String coveredText = sentence.getCoveredText().substring(startIndex,endIndex);
			TokenAnnotation npChunks = new TokenAnnotation();
			npChunks.setCoveredText(coveredText);
			npChunks.setStartOffset(startIndex);
			npChunks.setEndOffset(endIndex);
			npChunksToken.add(npChunks);
		}
		if (tokenType.equals("tokens")) {
			sentence.setNPtokens(npChunksToken);
		}else{
			sentence.setNormalizedNPtoens(npChunksToken);;
		}
		
		//add adjacent NP chunks
		LinkedList<int [] > mergedAdjacentNPs = new LinkedList<int []>();
		for (int i = 0; i < mergedNPs.size(); i++) {
			int startPost = (mergedNPs.get(i)[0]);
			int endPost   = (mergedNPs.get(i)[1]);;
			if(i< mergedNPs.size()-1 && endPost+1 == mergedNPs.get(i+1)[0]){
				endPost= mergedNPs.get(i+1)[1];
				i++;
			}
			int [] mergedAdjacentNpsPos = {startPost,endPost};
			mergedAdjacentNPs.add(mergedAdjacentNpsPos);
		}
		//add NPNP chunks
		List<TokenAnnotation> mergedNPNP = new LinkedList<>();
		for (int[] is : mergedAdjacentNPs) {
			int startIndex = sentence.getTokenList().get(is[0]).getStartOffset();
			int endIndex = sentence.getTokenList().get(is[1]).getEndOffset();
			String coveredText = sentence.getCoveredText().substring(startIndex,endIndex);
			TokenAnnotation npnpChunks = new TokenAnnotation();
			npnpChunks.setCoveredText(coveredText);
			int startOffset = sentence.getCoveredText().indexOf(coveredText);
			npnpChunks.setStartOffset(startOffset);
			int endOffset = startOffset+coveredText.length()-1;
			npnpChunks.setEndOffset(endOffset);
			mergedNPNP.add(npnpChunks);
		}
		if (tokenType.equals("tokens")) {
			sentence.setMergedTokensNPNP(mergedNPNP);
		}else{
			sentence.setMergedNormalizedTokensNPNP(mergedNPNP);
		}
		
		return mergedAdjacentNPs;
	}

	private LinkedList<int[]> getMergedChunksPositions(
			LinkedList<String> mergedChunks, String sentence) {
		LinkedList<int []> mergedChunksPositions = new LinkedList<>();
		for (String chunk : mergedChunks) {
			int [] offsets = new int[2];
			int startoffset = sentence.indexOf(chunk);
			int endOffset = startoffset + chunk.length()-1;
			offsets[0] = startoffset;
			offsets[1] = endOffset;
			mergedChunksPositions.add(offsets);
		}
		return mergedChunksPositions;
	}

	private LinkedList<String> getMergedChunks(LinkedList<String> npMergedTokens,
			LinkedList<String> npMergedTokensTag) {
		LinkedList<String> mergedChunksList = new LinkedList<>();
		//merge NP PP NP
		for (int i = 0; i < npMergedTokensTag.size()-2; i++) {
			String npppnpmergedChunks = "";
			String npMergedTag = npMergedTokensTag.get(i);
			if(!npMergedTag.equals("O")&&npMergedTag.split("-")[1].equals("NP")){
				//find next PP
				String nextTag = npMergedTokensTag.get(i+1);
				String nextNextTag = npMergedTokensTag.get(i+2);
				if(!nextTag.equals("O")&& !nextNextTag.equals("O")&&
						npMergedTokensTag.get(i+1).split("-")[1].equals("PP")&& npMergedTokensTag.get(i+2).split("-")[1].equals("NP")){
					npppnpmergedChunks = npMergedTokens.get(i)+" "+npMergedTokens.get(i+1)+" "+npMergedTokens.get(i+2);
					mergedChunksList.add(npppnpmergedChunks);
					i= i+1;
				}else{
					continue;
				}
			}else{
				continue;
			}
		}
		return mergedChunksList;
	}

	private LinkedList<String> getNPmergedTokens(String[] tokens,
			LinkedList<int[]> mergedNPs, String[] tags) {
		LinkedList<String> npMergedTokens = new LinkedList<>();
		LinkedList<String> npMergedTokensTags = new LinkedList<>();
		for (int i = 0; i < tokens.length; i++) {
			String mergedToken = tokens[i];
			String mergedTokenTag = tags[i];
			for (int[] is : mergedNPs) {
				int startOfMergedNp = is[0];
				if(i==startOfMergedNp){
					int endOfMergedNp = is[1];
					for (int j = i+1; j <= endOfMergedNp; j++) {
						mergedToken = mergedToken + " " + tokens[j];
					}
					i= endOfMergedNp;
				}else{
					continue;
				}
			}
			npMergedTokens.add(mergedToken);
			npMergedTokensTags.add(mergedTokenTag);
		}
		return npMergedTokensTags;

	}

	private LinkedList<String> getNPmergedTokens(String[] tokens, LinkedList<int[]> mergedNPs) {
		LinkedList<String> npMergedTokens = new LinkedList<>();
		for (int i = 0; i < tokens.length; i++) {
			String mergedToken = tokens[i];
			for (int[] is : mergedNPs) {
				int startOfMergedNp = is[0];
				if(i==startOfMergedNp){
					int endOfMergedNp = is[1];
					for (int j = i+1; j <= endOfMergedNp; j++) {
						mergedToken = mergedToken + " " + tokens[j];
					}
					i= endOfMergedNp;
				}else{
					continue;
				}
			}
			npMergedTokens.add(mergedToken);
		}
		return npMergedTokens;
	}
	private static int[] nounPhraseMerger(String[] chunks, int i) {
		//if chunk[i] == B

		int [] mergedNPTokensPosition = new int[2];
		if(chunks[i].split("-")[0].equals("B") && chunks[i].split("-")[1].equals("NP")){
			//find next B
			mergedNPTokensPosition[0] = i;
			for (int j = i+1; j < chunks.length; j++) {
				if(chunks[j].split("-")[0].equals("I")&& chunks[j].split("-")[1].equals("NP")){
					mergedNPTokensPosition[1] = j;
				}else{
					break;
				}
			}
		}else{
			return null;
		}
		if (mergedNPTokensPosition[1] == 0) {
			return null;
		}
		//logger.debug(mergedNPTokensPosition[0]+" "+mergedNPTokensPosition[1]);
		return mergedNPTokensPosition;

	}
	/**
	 * @param tokens
	 * @return tokensArray String Array of tokens
	 */
	protected String[] getTokens(List<TokenAnnotation> tokens) {
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
	protected String[] getTokensTags(List<TokenAnnotation> tokens) {
		String [] tokensPosArray = new String[tokens.size()] ;
		int i = 0;
		for (TokenAnnotation tokenAnnotation : tokens) {
			tokensPosArray[i]= tokenAnnotation.getTags();
			i++;
		}
		return tokensPosArray;
	}

}
