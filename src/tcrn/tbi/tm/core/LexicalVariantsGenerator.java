package tcrn.tbi.tm.core;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.nih.nlm.nls.lvg.Api.LvgCmdApi;

import tcrn.tbi.tm.annotator.Annotator;
import tcrn.tbi.tm.documents.DocumentsReader;
import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.SentenceAnnotation;
import tcrn.tbi.tm.model.TokenAnnotation;

/**
 * @author Manish
 * Generates word variants using "norm" and "LVG"
 *
 */
public class LexicalVariantsGenerator implements Annotator{
	private final String lvgPropertiesFile = "D:\\Programs\\lvg2014\\lvg2014\\lvg2014\\data\\config\\lvg.properties";
	static final Logger logger = LogManager.getLogger(DocumentsReader.class.getName());
	@Override
	public Annotation annotate() {
		return null;
	}

	@Override
	public FileAnnotation annotate(FileAnnotation annFile) {
		logger.info("Genrating lexical Variants");
		Hashtable<String, String> properties = new Hashtable<String, String>(2);
		properties.put("DATABASE", "HSQLDB");
		properties.put("DB_DRIVER", "org.hsqldb.jdbcDriver");
		LvgCmdApi lvg = new LvgCmdApi("-f:l:b -C:2",
				lvgPropertiesFile,
				properties);
		logger.info("Lvg properties file found and loaded");

		List<SentenceAnnotation> sentences = annFile.getSentenceAnnList();
		for (SentenceAnnotation sentenceAnnotation : sentences) {
			List<TokenAnnotation> tokens = sentenceAnnotation.getTokenList();
			List<TokenAnnotation> normalizedTokens = new LinkedList<TokenAnnotation>();
			try {
				for (TokenAnnotation token : tokens) {
					String normalizedForm = null;
					String out = lvg.MutateToString(token.getCoveredText());
					String[] output = out.split("\\|");
					//System.out.println(output[1]);
					if ((output != null) && (output.length >= 2)
							&& (!output[1].matches("No Output"))) {
						normalizedForm = output[1];

					}
					TokenAnnotation normToken = new TokenAnnotation();
					normToken.setCoveredText(normalizedForm);
					normToken.setStartOffset(token.getStartOffset());
					normToken.setEndOffset(token.getEndOffset());
					normalizedTokens.add(normToken);
				}
				sentenceAnnotation.setNormalizedTokenList(normalizedTokens);
			} catch (SQLException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		lvg.CleanUp();
		return annFile;
	}
	//	private Vector<LexItem> Mutate(LexItem in, LvgApi lvgApi) throws SQLException {
	//		Vector<LexItem> outs = new Vector<LexItem>();
	//
	//		// first flow component in a flow
	//		Vector<LexItem> out1 = ToLowerCase.Mutate(in, false, true);
	//
	//		// second flow component in a flow
	//		for(int i = 0 ; i < out1.size(); i++)
	//		{
	//			LexItem temp = out1.elementAt(i);
	//			// convert results from above flow component to input for next FC 
	//			LexItem tempIn = LexItem.TargetToSource(temp);
	//			String mimmTermlength = lvgApi.GetConfiguration().GetConfiguration("MIN_TERM_LENGTH");
	//			String lvgDir = lvgApi.GetConfiguration().GetConfiguration("LVG_DIR");
	//			RamTrie trie = new RamTrie(true, Integer.parseInt(mimmTermlength), lvgDir, 0);
	//			Vector<LexItem> out2 = ToUninflectTerm.Mutate(tempIn, lvgApi.GetConnection(), trie, false, false);
	//			
	//			outs.addAll(out2);
	//		}
	//
	//		return outs;
	//	}

	//	private static void PrintResult(Vector<LexItem> result)
	//	{
	//		for(int i = 0; i < result.size(); i++)
	//		{
	//			LexItem temp = result.elementAt(i);
	//			System.out.println(temp.GetOriginalTerm() + "|"
	//					+ temp.GetSourceTerm() + "|"
	//					+ temp.GetSourceCategory().GetValue() + "|"
	//					+ temp.GetSourceInflection().GetValue() + "|"
	//					+ temp.GetTargetTerm() + "|"
	//					+ temp.GetTargetCategory().GetValue() + "|"
	//					+ temp.GetTargetInflection().GetValue() + "|"
	//					+ temp.GetMutateInformation()+"|" 
	//					+ temp.GetDetailInformation());
	//		}
	//	}

}
