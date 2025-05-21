package org.openmarkov.inference.ceanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.test.TestSpeed;
import org.openmarkov.inference.heuristics.Tools;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;

import java.net.URISyntaxException;


public abstract class DANCEATest {

	public void testCEADANEvaluation(String danName, int globalNumberOfCEPIntervals, double... expectedThreshods) throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		Tools t = new Tools();
		ProbNet network = t.loadDAN(danName);
		System.out.println("*** CEA with DAN " + danName + " ***");
		System.out.println();
		CEAnalysis eval = buildCEAnalysis(network);
		testCEADANEvaluation(globalNumberOfCEPIntervals, eval, expectedThreshods);
	}

	protected void testCEADANEvaluation(int globalNumberOfCEPIntervals, CEAnalysis eval, double... expectedThreshods) throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
        CEP cep = eval.getCEP();
		Assertions.assertNotNull(cep);
		Assertions.assertEquals(globalNumberOfCEPIntervals, cep.getNumIntervals());
		int numThresholds = globalNumberOfCEPIntervals - 1;
		Assertions.assertEquals(numThresholds, expectedThreshods.length);
		double[] obtainedThresholds = cep.getThresholds();
		for (int i = 0; i < numThresholds; i++) {
			Assertions.assertEquals(expectedThreshods[i], obtainedThresholds[i], 0.1);
		}
	}

	protected abstract CEAnalysis buildCEAnalysis(ProbNet network) throws NotEvaluableNetworkException;
	
	@Test
	public void testDANOnlyNonZeroUtility() throws
            NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		testCEADANEvaluation("only-non-zero-utility-ce", 1);
	}
	
	@Test
	public void testDANOnlyZeroyUtility() throws
            NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		testCEADANEvaluation("only-zero-utility-ce", 1);
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testDANOneDecisionCE() throws
            NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		testCEADANEvaluation("one-decision-CE", 2, 1.333333333);
	}
	
	@Tag(TestSpeed.SLOW)
	@Test
	public void testDANOneChanceCE() throws
            NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		testCEADANEvaluation("one-chance-ce", 1);
	}
	
	@Tag(TestSpeed.SLOW)
	@Test
	public void testDANDecideTest() throws
            NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException, ParserException, URISyntaxException {
		testCEADANEvaluation("decide-test-ce", 3, 11171.347828594418, 33383.5);
	}
		
	

}
