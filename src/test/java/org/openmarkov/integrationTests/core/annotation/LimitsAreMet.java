package org.openmarkov.integrationTests.core.annotation;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.annotation.Limits;
import org.openmarkov.core.inference.annotation.InferenceAnnotation;
import org.openmarkov.core.test.TestSpeed;
import org.openmarkov.plugin.PluginSearch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LimitsAreMet {
    
    @Tag(TestSpeed.FAST)
    @Test
    public void limitsAreMet() {
        var currentDate = Date.from(Instant.now());
        var maxDate = new Date(2025-1900, 9-1, 1, 0, 0, 0);
        if(currentDate.before(maxDate)) {
            return;
        }
        var errorsString = PluginSearch.init()
                                       .annotatedWith(Limits.class)
                                       .stream()
                                       .flatMap(LimitsAreMet::findAllErrors)
                                       .sorted()
                                       .map(error -> "\t- " + error)
                                       .collect(Collectors.joining(System.lineSeparator()));
        if (!errorsString.isBlank()) {
            fail("Some limits aren't met:" + System.lineSeparator() + errorsString);
        }
    }
    
    private static @NotNull Stream<String> findAllErrors(Class<Object> limitedClass) {
        ArrayList<String> errors = new ArrayList<>();
        var limits = limitedClass.getAnnotation(Limits.class);
        if (limits.classesThatCanBeAnnotated().length > 0) {
            errors.addAll(LimitsAreMet.findInheritanceErrors(limitedClass, limits).toList());
        }
        if (limits.requiredConstructors().length > 0) {
            errors.addAll(LimitsAreMet.findConstructorsErrors(limitedClass, limits).toList());
        }
        return errors.stream();
    }
    
    private static @NotNull Stream<String> findConstructorsErrors(Class<Object> limitedClass, Limits limits) {
        var requiredConstructorsArgs = Arrays
                .stream(limits.requiredConstructors())
                .map(requiredConstructor -> requiredConstructor.value())
                .toList();
        var constructorsStrings = requiredConstructorsArgs
                .stream()
                .map(requiredConstructors ->
                             Arrays.stream(requiredConstructors)
                                   .map(Class::getName)
                                   .collect(Collectors.joining(", ")))
                .toList();
        
        
        Stream<String> constructorErrors = PluginSearch.init()
                                                       .annotatedWith(limitedClass)
                                                       .filter(annotatedClass ->
                                                                       requiredConstructorsArgs.stream().noneMatch(constructorArgs->{
                                                               try {
                                                                   annotatedClass.getDeclaredConstructor(constructorArgs);
                                                                   return true;
                                                               } catch (NoSuchMethodException e) {
                                                                   return false;
                                                               }
                                                           })
                                                       )
                                                       .stream()
                                                       .map(wrongClass -> "Class " + wrongClass.getName() + " is annotated with @" + limitedClass.getSimpleName() + " and so it should a constructor such as: "+
                                                               constructorsStrings.stream().map(constructorString -> wrongClass.getSimpleName() + "(" + constructorString + ")")
                                                                                  .collect(Collectors.joining(", ")));
        return constructorErrors;
    }
    
    private static @NotNull Stream<String> findInheritanceErrors(Class<Object> limitedClass, Limits limits) {
        List<Class<?>> annotableClasses = List.of(limits.classesThatCanBeAnnotated());
        return PluginSearch.init()
                           .annotatedWith(limitedClass)
                           .filter(annotatedClass -> annotableClasses
                                   .stream()
                                   .noneMatch(classThatCanBeAnnotated -> classThatCanBeAnnotated.isAssignableFrom(annotatedClass)))
                           .stream()
                           .map(wrongClass -> "Class " + wrongClass.getName() + " is annotated with @" + limitedClass.getSimpleName() + " and so it should extend one of the following classes: " + annotableClasses);
    }
    
    
}
