package org.openmarkov.staticAnalysis.verifyImplementationRequirements;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.annotation.ImplementationRequirements;
import org.openmarkov.core.annotation.RequiredConstructor;
import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.staticAnalysis.utils.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record RequirementsVerifier(Class<Object> constrainedClass, ImplementationRequirements requirements) {
    
    public @NotNull Stream<String> findAllErrors() {
        return Stream.of(this.findInheritanceErrors(), this.findConstructorsErrors())
                     .flatMap(self -> self);
    }
    
    private @NotNull Stream<String> findConstructorsErrors() {
        if (this.requirements.requiresOneOfTheseConstructors().length == 0) {
            return Stream.empty();
        }
        var requiredConstructorsArgs = Arrays
                .stream(this.requirements.requiresOneOfTheseConstructors())
                .map(RequiredConstructor::value)
                .toList();
        var constructorsStrings = requiredConstructorsArgs
                .stream()
                .map(requiredConstructors ->
                             Arrays.stream(requiredConstructors)
                                   .map(Class::getName)
                                   .collect(Collectors.joining(", ")))
                .toList();
        
        return this
                .getSourceClasses()
                .filter(ClassUtils::isConcrete)
                .filter(annotatedClass -> requiredConstructorsArgs
                        .stream()
                        .noneMatch(constructorArgs -> {
                            try {
                                annotatedClass.getDeclaredConstructor(constructorArgs);
                                return true;
                            } catch (NoSuchMethodException e) {
                                return false;
                            }
                        })
                )
                .map(wrongClass -> wrongClass +
                        " " + this.stringifyOrigin() +
                        " and so it should have a constructor such as any of the following: " +
                        constructorsStrings.stream()
                                           .map(constructorString -> wrongClass.getSimpleName() + "(" + constructorString + ")")
                                           .collect(Collectors.joining(", ")));
    }
    
    private @NotNull Stream<String> findInheritanceErrors() {
        if (this.requirements.hasToExtendOneOfTheseClasses().length == 0) {
            return Stream.empty();
        }
        var requiredExtension = List.<Class<?>>of(this.requirements.hasToExtendOneOfTheseClasses());
        return this
                .getSourceClasses()
                .filter(annotatedClass -> requiredExtension
                        .stream()
                        .noneMatch(aClassThatShouldExtend -> aClassThatShouldExtend.isAssignableFrom(annotatedClass)))
                .map(wrongClass -> wrongClass +
                        " " + this.stringifyOrigin() +
                        " and so it should extend one of the following: " + requiredExtension);
    }
    
    private @NotNull Stream<Class<Object>> getSourceClasses() {
        return Stream.concat(
                PluginSearch.init().annotatedWith(this.constrainedClass).stream(),
                PluginSearch.init().extending(this.constrainedClass).stream()
        ).distinct();
    }
    
    private String stringifyOrigin() {
        if (this.constrainedClass.isAnnotation()) {
            return "is annotated with @" + this.constrainedClass.getName();
        }
        if (this.constrainedClass.isInterface()) {
            return "implements interface " + this.constrainedClass.getName();
        }
        if (Modifier.isAbstract(this.constrainedClass.getModifiers())) {
            return "extends abstract class " + this.constrainedClass.getName();
        }
        return "extends class " + this.constrainedClass.getName();
    }
}
