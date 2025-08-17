package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException.exceptions;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.OpenMarkovException;

@SuppressWarnings("ALL")
public class UnbundledTrialException2 extends OpenMarkovException {
    public final String program;
    public final String concept;
    
    public UnbundledTrialException2(String program, String concept) {
        this.program = program;
        this.concept = concept;
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return this.autoGetExceptionTitle();
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        return this.autoGetExceptionMessage();
    }
}
