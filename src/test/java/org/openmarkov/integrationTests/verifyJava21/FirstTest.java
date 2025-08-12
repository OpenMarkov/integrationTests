package org.openmarkov.integrationTests.verifyJava21;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;

import static org.junit.jupiter.api.Assertions.fail;

public class FirstTest {
    
    @Test
    public void test() {
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
