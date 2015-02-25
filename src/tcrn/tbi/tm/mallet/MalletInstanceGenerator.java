package tcrn.tbi.tm.mallet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class MalletInstanceGenerator {
	
	public InstanceList generateInstanceList(String filePath,boolean targetProcessing) throws Exception {
 
		 LineGroupIterator lgitr = new LineGroupIterator(new FileReader(filePath), Pattern.compile("^\\s*$"), true);
		 ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		 pipes.add(new SimpleTaggerSentence2TokenSequence());
		 pipes.add(new TokenSequence2FeatureVectorSequence (true, false)); 
	     pipes.add(new PrintInputAndTarget());
	    
	     
		Pipe pipe = new SerialPipes(pipes);
		InstanceList instances = new InstanceList(pipe);
		pipe.setTargetProcessing(targetProcessing);
		instances.addThruPipe(lgitr);
		return instances;
	}

	public Instance generateInstance(String string) {
		Instance instance = new Instance(string, null, "instacne-1", null);
//		 ArrayList<Pipe> pipes = new ArrayList<Pipe>();
//		 pipes.add(new SimpleTaggerSentence2TokenSequence());
//		 pipes.add(new TokenSequence2FeatureVectorSequence (true, false)); 
//	     pipes.add(new PrintInputAndTarget());
	     SimpleTaggerSentence2TokenSequence ob3=new SimpleTaggerSentence2TokenSequence();
	     Instance i=ob3.pipe(instance);
	     TokenSequence2FeatureVectorSequence ob4 = new TokenSequence2FeatureVectorSequence();
	     Instance i2 = ob4.pipe(i);
//	     PrintInputAndTarget print = new PrintInputAndTarget();
//	     print.pipe(i2);
	     return i2;
	     
	}

}
