package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException2.exceptions;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.OpenMarkovException;


@SuppressWarnings("ALL")
public class TrialException2 extends OpenMarkovException {
    
    private final String program;
    private final String netName;
    
    public TrialException2(String program, String netName) {
        this.program = program;
        this.netName = netName;
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return this.autoGetExceptionTitle();
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        return this.autoGetExceptionMessage();
    }
}
