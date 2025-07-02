package org.openmarkov.integrationTests.core.exception;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.test.TestSpeed;
import org.openmarkov.core.localize.LocalizedException;


public class OpenMarkovExceptionTest {
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testMessages() {
        OpenMarkovException openMarkovException = new OpenMarkovException("ConfigurationException", "Test");
        assertNotNull(openMarkovException);
        LocalizedException configurationException = new LocalizedException(openMarkovException, null);
        assertNotNull(configurationException);
    }
}
