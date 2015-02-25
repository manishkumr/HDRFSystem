package tcrn.tbi.tm.model;

import java.util.List;

public class SentenceAnnotation extends Annotation{
	private List<TokenAnnotation> tokenList;
	private List<TokenAnnotation> normalizedTokenList;
	private List<TokenAnnotation> NPtokens;
	private List<TokenAnnotation> normalizedNPtokens;
	private List<TokenAnnotation> mergedTokensNPppNP;
	private List<TokenAnnotation> mergedTokensNPNP;
	private List<TokenAnnotation> mergedNormalizedTokensNPNP;
	private List<TokenAnnotation> mergedNormalizedTonenNPppNP;

	/**
	 * @return the tokenList
	 */
	public List<TokenAnnotation> getTokenList() {
		return tokenList;
	}

	/**
	 * @param tokenList the tokenList to set
	 */
	public void setTokenList(List<TokenAnnotation> tokenList) {
		this.tokenList = tokenList;
	}

	/**
	 * @return the mergedTokensNPppNP
	 */
	public List<TokenAnnotation> getMergedTokensNPppNP() {
		return mergedTokensNPppNP;
	}

	/**
	 * @param mergedTokensNPppNP the mergedTokensNPppNP to set
	 */
	public void setMergedTokensNPppNP(List<TokenAnnotation> mergedTokensNPppNP) {
		this.mergedTokensNPppNP = mergedTokensNPppNP;
	}

	/**
	 * @return the mergedTokensNPNP
	 */
	public List<TokenAnnotation> getMergedTokensNPNP() {
		return mergedTokensNPNP;
	}

	/**
	 * @param mergedTokensNPNP the mergedTokensNPNP to set
	 */
	public void setMergedTokensNPNP(List<TokenAnnotation> mergedTokensNPNP) {
		this.mergedTokensNPNP = mergedTokensNPNP;
	}

	/**
	 * @return the normalizedTokenList
	 */
	public List<TokenAnnotation> getNormalizedTokenList() {
		return normalizedTokenList;
	}

	/**
	 * @param normalizedTokenList the normalizedTokenList to set
	 */
	public void setNormalizedTokenList(List<TokenAnnotation> normalizedTokenList) {
		this.normalizedTokenList = normalizedTokenList;
	}

	/**
	 * @return the mergedNormalizedTokensNPNP
	 */
	public List<TokenAnnotation> getMergedNormalizedTokensNPNP() {
		return mergedNormalizedTokensNPNP;
	}

	/**
	 * @param mergedNormalizedTokensNPNP the mergedNormalizedTokensNPNP to set
	 */
	public void setMergedNormalizedTokensNPNP(
			List<TokenAnnotation> mergedNormalizedTokensNPNP) {
		this.mergedNormalizedTokensNPNP = mergedNormalizedTokensNPNP;
	}

	/**
	 * @return the mergedNormalizedTonenNPppNP
	 */
	public List<TokenAnnotation> getMergedNormalizedTonenNPppNP() {
		return mergedNormalizedTonenNPppNP;
	}

	/**
	 * @param mergedNormalizedTonenNPppNP the mergedNormalizedTonenNPppNP to set
	 */
	public void setMergedNormalizedTonenNPppNP(
			List<TokenAnnotation> mergedNormalizedTonenNPppNP) {
		this.mergedNormalizedTonenNPppNP = mergedNormalizedTonenNPppNP;
	}

	/**
	 * @return the nPtokens
	 */
	public List<TokenAnnotation> getNPtokens() {
		return NPtokens;
	}

	/**
	 * @param nPtokens the nPtokens to set
	 */
	public void setNPtokens(List<TokenAnnotation> nPtokens) {
		NPtokens = nPtokens;
	}

	/**
	 * @return the normalizedNPtoens
	 */
	public List<TokenAnnotation> getNormalizedNPtoens() {
		return normalizedNPtokens;
	}

	/**
	 * @param normalizedNPtoens the normalizedNPtoens to set
	 */
	public void setNormalizedNPtoens(List<TokenAnnotation> normalizedNPtoens) {
		this.normalizedNPtokens = normalizedNPtoens;
	}
	public boolean equals(SentenceAnnotation sentAnn) {
		if(sentAnn.getStartOffset().equals(this.getStartOffset())&&
		   sentAnn.getEndOffset().equals(this.getEndOffset())){
			return true;
		}
		return false;
	}
}
