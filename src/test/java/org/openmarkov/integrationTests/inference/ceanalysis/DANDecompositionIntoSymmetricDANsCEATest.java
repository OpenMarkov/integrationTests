package org.openmarkov.integrationTests.inference.ceanalysis;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecompositionIntoSymmetricDANsCEA;

public class DANDecompositionIntoSymmetricDANsCEATest extends DANCEATest {
    
    @Override
    protected CEAnalysis buildCEAnalysis(ProbNet network) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedContraints {
        return new DANDecompositionIntoSymmetricDANsCEA(network);
	}

}
