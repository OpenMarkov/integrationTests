package org.openmarkov.core.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.openmarkov.gui.localize.LocalizedException;


public class OpenMarkovExceptionTest {

    @Test
    public void testMessages() {
        OpenMarkovException openMarkovException = new OpenMarkovException("ConfigurationException", "Test");
        assertNotNull(openMarkovException);
        LocalizedException configurationException = new LocalizedException(openMarkovException, null);
        assertNotNull(configurationException);
    }
}
