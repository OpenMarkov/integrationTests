package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException2.exceptions;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class NoExceptionsInheritsFromOpenMarkovException {
    
    @Test
    public void testAllExceptionsAreFromOpenMarkov() {
        var openMarkovExceptions = PluginSearch
                .init()
                .extending(Exception.class)
                .stream()
                .filter(exceptionClass -> exceptionClass.getSuperclass().equals(OpenMarkovException.class))
                .sorted(Comparator.comparing(Class::getName))
                .map(exceptionClass -> "\t- " + exceptionClass.getName())
                .collect(Collectors.joining(System.lineSeparator()));
        if (openMarkovExceptions.isBlank()) {
            return;
        }
        fail("There are some exceptions extending from OpenMarkovException:" + System.lineSeparator() + openMarkovExceptions);
    }
    
    @Test
    public void printAllExceptionsFromOpenMarkov() {
        var openMarkovExceptions = PluginSearch
                .init()
                .extending(Exception.class)
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .map(exceptionClass -> "\t- " + exceptionClass.getName())
                .collect(Collectors.joining(System.lineSeparator()));
        System.out.println("This are the exceptions defined in OpenMarkov:" + System.lineSeparator() + openMarkovExceptions);
    }
    
    
}
