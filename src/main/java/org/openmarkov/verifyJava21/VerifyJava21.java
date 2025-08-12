package org.openmarkov.verifyJava21;

import org.openmarkov.core.exception.IncompatibleEvidenceException;

public class VerifyJava21 {
    
    public static void main(String[] args) {
        IncompatibleEvidenceException ex = new IncompatibleEvidenceException.SamplesWeigthIsZero(new double[][]{});
        switch (ex){
            case IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther evidenceIsIncompatibleWithOther ->
                    System.out.println("How did I get here?");
            case IncompatibleEvidenceException.FindingVariableIsMissingAState findingVariableIsMissingAState ->
                    System.out.println("How did I get here?");
            case IncompatibleEvidenceException.SamplesWeigthIsZero samplesWeigthIsZero ->
                    System.out.println("This is the one!");
        }
    }
}
