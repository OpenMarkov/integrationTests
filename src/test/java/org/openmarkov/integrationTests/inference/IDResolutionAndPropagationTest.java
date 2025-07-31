package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.test.TestSpeed;
import org.openmarkov.inference.InferenceTestsTools;
import org.openmarkov.integrationTests.inference.heuristics.Tools;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class IDResolutionAndPropagationTest {

	private ProbNet loadID(String nameSuffix) throws ParserException, URISyntaxException, FileNotFoundException {
		Tools t = new Tools();
		return t.loadID(nameSuffix);
	}
	
	private void testBasicInferenceNoCEA(String nameSuffix, boolean checkStrategyTree) throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, CannotNormalizeNullVectorException, FileNotFoundException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
			testResolutionAndPropagation(loadID(nameSuffix),null,checkStrategyTree);
	}
	
	@Test
	public void testIDOnlyDecisionNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, CannotNormalizeNullVectorException, FileNotFoundException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
		testBasicInferenceNoCEA("only-decision-no-utility", false);
	}
	
	@Test
	public void testIDThreeDecTwoUtil() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, CannotNormalizeNullVectorException, FileNotFoundException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
		testBasicInferenceNoCEA("three-dec-two-util", true);
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testIDOnlyImposedUniformDecisionNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, CannotNormalizeNullVectorException, FileNotFoundException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
		testBasicInferenceNoCEA("only-imposed-uniform-dec-no-util", false);
	}
	
	@Test
	public void testIDOnlyImposedDecisionAndChanceNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, CannotNormalizeNullVectorException, FileNotFoundException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
		testBasicInferenceNoCEA("only-imposed-decision-and-chance-no-utility", false);
	}
	
	private static void testResolutionAndPropagation(ProbNet probNet, EvidenceCase preResolutionEvidence, boolean checkStrategyTree)
			throws CannotNormalizeNullVectorException, NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
		InferenceTestsTools.testResolveNetwork(probNet, preResolutionEvidence, checkStrategyTree);

		// TODO - Check propagate errors
		InferenceTestsTools.testPropagateNetwork(probNet, probNet.getVariables(), preResolutionEvidence);
	}
	
}
