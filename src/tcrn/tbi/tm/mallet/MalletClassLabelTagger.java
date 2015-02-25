package tcrn.tbi.tm.mallet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import cc.mallet.fst.CRF;
import cc.mallet.fst.MaxLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

public class MalletClassLabelTagger {
	public void getTags(String modelFilePath, InstanceList devinstances) throws Exception {
		ObjectInputStream s =
				new ObjectInputStream(new FileInputStream(modelFilePath));
		CRF crf = (CRF) s.readObject();
		s.close();

		boolean includeInput = true;
		for (int i = 0; i < devinstances.size(); i++)
		{
			Sequence input = (Sequence)devinstances.get(i).getData();
			Sequence[] outputs = apply(crf, input, 1);
			int k = outputs.length;
			boolean error = false;
			for (int a = 0; a < k; a++) {
				if (outputs[a].size() != input.size()) {
					//logger.info("Failed to decode input sequence " + i + ", answer " + a);
					error = true;
				}
			}
			if (!error) {
				for (int j = 0; j < input.size(); j++)
				{
					StringBuffer buf = new StringBuffer();
					for (int a = 0; a < k; a++)
						buf.append(outputs[a].get(j).toString()).append(" ");
					if (includeInput) {
						FeatureVector fv = (FeatureVector)input.get(j);
						buf.append(fv.toString(true));                
					}
					System.out.println(buf.toString());
				}
				System.out.println();
			}
		}
	}
	private Sequence[] apply(Transducer model, Sequence input, int k) {
		Sequence[] answers;
		if (k == 1) {
			answers = new Sequence[1];
			answers[0] = model.transduce (input);
		}
		else {
			MaxLatticeDefault lattice =
					new MaxLatticeDefault (model, input, null, 100000);

			answers = lattice.bestOutputSequences(k).toArray(new Sequence[0]);
		}
		return answers;
	}
	public String getTag(Instance instance, String modelFilePath) throws Exception {
		InputStream is = MalletClassLabelTagger.class.getResourceAsStream("/SmokingClassifier.model");
		ObjectInputStream s = new ObjectInputStream(is);
		CRF crf = (CRF) s.readObject();
		s.close();
		boolean includeInput = true;
		Sequence input = (Sequence)instance.getData();
		Sequence[] outputs = apply(crf, input, 1);
		int k = outputs.length;
		boolean error = false;
		for (int a = 0; a < k; a++) {
			if (outputs[a].size() != input.size()) {
				//logger.info("Failed to decode input sequence " + i + ", answer " + a);
				error = true;
			}
		}
		String predictedClass = null;
		if (!error) {
			for (int j = 0; j < input.size(); j++)
			{
				StringBuffer buf = new StringBuffer();
				for (int a = 0; a < k; a++)
					buf.append(outputs[a].get(j).toString()).append(" ");
				if (includeInput) {
					FeatureVector fv = (FeatureVector)input.get(j);
					buf.append(fv.toString(true));                
				}
				predictedClass = (buf.toString());
			}
			//System.out.println();
		}
		return predictedClass;
	}

}
