package org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.UnreacheableException;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class testUnreacheableExceptionStacktrace {
    
    /**
     * Tests {@link UnreacheableException} always contains just the target exception, meaning an
     * {@link UnreacheableException} will never contain another {@link UnreacheableException}.
     * <p>
     * It also tests the operation {@link ThrowableUtils#flatten(Throwable)}.
     */
    @Test
    public void testFlattening() {
        UnreacheableException exception;
        try {
            callerA();
            return;
        } catch (UnreacheableException e) {
            exception = e;
        }
        Throwable flat = ThrowableUtils.flatten(exception);
        assertEquals(flat.getClass(), EmptyDatabaseException.class);
        var stackTraceInOrder = List.of(
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.thrower",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.callerD",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.callerB",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.callerA",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreacheableExceptionStacktrace.testFlattening"
        );
        for (int i = 0; i < stackTraceInOrder.size(); i++) {
            var expectedMethodInStackTrace = stackTraceInOrder.get(i);
            var stackTraceElement = flat.getStackTrace()[i];
            assertEquals(expectedMethodInStackTrace, stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
        }
    }
    
    static void callerA() {
        callerB();
    }
    
    static void callerB() {
        callerC();
    }
    
    static void callerC() {
        try {
            callerD();
        } catch (EmptyDatabaseException e) {
            throw new UnreacheableException(e);
        }
        
    }
    
    static void callerD() throws EmptyDatabaseException {
        thrower();
    }
    
    static void thrower() throws EmptyDatabaseException {
        throw new EmptyDatabaseException("DB.file");
    }
    
}
