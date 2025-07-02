package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException2.exceptions;

public class SubTrialException2 extends TrialException2 {
    private final int errorCode;
    
    public SubTrialException2(String program, String netName, int errorCode) {
        super(program, netName);
        this.errorCode = errorCode;
    }
}
