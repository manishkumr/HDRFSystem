package tcrn.tbi.tm.mallet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFCacheStaleIndicator;
import cc.mallet.fst.CRFOptimizableByBatchLabelLikelihood;
import cc.mallet.fst.CRFTrainerByThreadedLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.ThreadedOptimizable;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.ViterbiWriter;
import cc.mallet.optimize.Optimizable;
import cc.mallet.types.InstanceList;

public class MalletModelGenerator {
	public  void getTrainingModel(InstanceList trainingInstances, InstanceList testingInstances) {
		CRF crf = new CRF(trainingInstances.getDataAlphabet(),
				trainingInstances.getTargetAlphabet());
		/*crf.addStatesForLabelsConnectedAsIn(trainingInstances);
        crf.addStartState();
        CRFTrainerByThreadedLabelLikelihood trainer = new CRFTrainerByThreadedLabelLikelihood(crf, 32);
        */
        //new config
        crf.addFullyConnectedStatesForLabels();
        crf.addStartState();
        crf.setWeightsDimensionAsIn(trainingInstances, false);
        //crf.setWeightsDimensionDensely();
        int numThreads = 32;
        CRFOptimizableByBatchLabelLikelihood batchOptLabel =
            new CRFOptimizableByBatchLabelLikelihood(crf, trainingInstances, numThreads);
        ThreadedOptimizable optLabel = new ThreadedOptimizable(
                batchOptLabel, trainingInstances, crf.getParameters().getNumFactors(),
                new CRFCacheStaleIndicator(crf));
        // CRF trainer
//        Optimizable.ByGradientValue[] opts =
//                new Optimizable.ByGradientValue[]{optLabel};
        // by default, use L-BFGS as the optimizer
//        CRFTrainerByValueGradients trainer =
//                new CRFTrainerByValueGradients(crf, opts);
        
        CRFTrainerByThreadedLabelLikelihood trainer = new CRFTrainerByThreadedLabelLikelihood(crf, 32);
        trainer.setGaussianPriorVariance(10);
        PerClassAccuracyEvaluator perCalssEval = new PerClassAccuracyEvaluator(trainingInstances, "training");
        trainer.addEvaluator(perCalssEval);
        if (testingInstances != null) {
        	PerClassAccuracyEvaluator perClassEval = new PerClassAccuracyEvaluator(testingInstances, "testing");
            trainer.addEvaluator(perClassEval);
            trainer.addEvaluator(new TokenAccuracyEvaluator(testingInstances, "testing"));
           // String[] labels = new String[]{"SENT_SECT", "SENT_ONLY"};
//            TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
//                new InstanceList[]{trainingInstances, testingInstances},
//                new String[]{"train", "test"}, labels, labels) {
//              @Override
//              public boolean precondition(TransducerTrainer tt) {
//                // evaluate model every 5 training iterations
//                return tt.getIteration() % 5 == 0;
//              }
//            };
//            trainer.addEvaluator(evaluator);
           ViterbiWriter viterbiWriter = new ViterbiWriter(
                    "dis_con_crf", // output file prefix
                    new InstanceList[]{trainingInstances, testingInstances},
                    new String[]{"train", "test"}) {

                @Override
                public boolean precondition(TransducerTrainer tt) {
                    return tt.getIteration() % 5 == 0;
                }
            };
            trainer.addEvaluator(viterbiWriter);
        }

        // all setup done, train until convergence
        //trainer.setMaxResets(0);
        trainer.train(trainingInstances,Integer.MAX_VALUE);
        perCalssEval.evaluateInstanceList(trainer, testingInstances, "testing");
        //test
        optLabel.shutdown(); // clean exit for all the threads
        trainer.shutdown();
        try {
            PrintWriter writer = new PrintWriter("crf_raw.txt");
            trainer.getCRF().print(writer);
            trainer.getCRF().write(new File("SmokingClassifier.model"));
            writer.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
	}

}
