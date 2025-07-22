package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException2.exceptions;

import org.junit.jupiter.api.Test;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllExceptionsAreFromOpenMarkov {
    
    @Test
    public void testAllExceptionsAreFromOpenMarkov() {
        var openMarkovExceptions = PluginSearch
                .init()
                .extending(Exception.class)
                .stream()
                .collect(Collectors.toSet());
        
        var thrownExceptionsAndOrigins = PluginSearch
                .init()
                .stream()
                .flatMap(aClass -> {
                    var exceptionsFromMethods = Arrays.stream(aClass.getDeclaredMethods())
                                                      .flatMap(method -> Arrays.stream(method.getExceptionTypes())
                                                                               .map(exceptionClass -> new ExceptionAndOrigin(exceptionClass, aClass, method))
                                                      
                                                      );
                    var exceptionsFromConstructors = Arrays.stream(aClass.getDeclaredConstructors())
                                                           .flatMap(constructor -> Arrays.stream(constructor.getExceptionTypes())
                                                                                         .map(exceptionClass -> new ExceptionAndOrigin(exceptionClass, aClass, constructor)));
                    return Stream.concat(exceptionsFromMethods, exceptionsFromConstructors);
                })
                .filter(exceptionAndOrigin -> !openMarkovExceptions.contains(exceptionAndOrigin.exceptionClass))
                .toList();
        thrownExceptionsAndOrigins = new ArrayList<>(thrownExceptionsAndOrigins);
        Comparator<ExceptionAndOrigin> classNameComparator = Comparator.comparing(exceptionAndOrigin -> exceptionAndOrigin.originClass.getName());
        Comparator<ExceptionAndOrigin> constructorsFirstComparator = (exceptionAndOrigin1, exceptionAndOrigin2) -> {
            if (exceptionAndOrigin1.throwingExecutable instanceof Constructor && exceptionAndOrigin2.throwingExecutable instanceof Method) {
                return 1;
            }
            if (exceptionAndOrigin1.throwingExecutable instanceof Method && exceptionAndOrigin2.throwingExecutable instanceof Constructor) {
                return -1;
            }
            return 0;
        };
        Comparator<ExceptionAndOrigin> methodNameComparator = Comparator.comparing(exceptionAndOrigin -> exceptionAndOrigin.throwingExecutable.getName());
        Comparator<ExceptionAndOrigin> exceptionNameComparator = Comparator.comparing(exceptionAndOrigin -> exceptionAndOrigin.exceptionClass.getName());
        thrownExceptionsAndOrigins.sort(
                classNameComparator.thenComparing(constructorsFirstComparator)
                                   .thenComparing(methodNameComparator)
                                   .thenComparing(exceptionNameComparator)
        );
        
        thrownExceptionsAndOrigins.stream()
                                  .map(ExceptionAndOrigin::exceptionClass)
                                  .map(Class::getName)
                                  .distinct()
                                  .forEach(System.out::println);
        
        thrownExceptionsAndOrigins.forEach(System.out::println);
        
    }
    
    record ExceptionAndOrigin(Class<?> exceptionClass, Class<?> originClass, Executable throwingExecutable) {
    }
    
    ;
    
}
