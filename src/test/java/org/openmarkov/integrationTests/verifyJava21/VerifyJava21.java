package org.openmarkov.integrationTests.verifyJava21;

import org.openmarkov.core.exception.IncompatibleEvidenceException;

import static org.junit.jupiter.api.Assertions.fail;

public class VerifyJava21 {
    
    public static void main(String[] args) {
        IncompatibleEvidenceException ex = new IncompatibleEvidenceException.SamplesWeigthIsZero(new double[][]{});
        switch (ex){
            case IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther evidenceIsIncompatibleWithOther ->
                    fail();
            case IncompatibleEvidenceException.FindingVariableIsMissingAState findingVariableIsMissingAState ->
                    fail();
            case IncompatibleEvidenceException.SamplesWeigthIsZero samplesWeigthIsZero ->
                    System.out.println("This is the one!");
        }
    }
}
