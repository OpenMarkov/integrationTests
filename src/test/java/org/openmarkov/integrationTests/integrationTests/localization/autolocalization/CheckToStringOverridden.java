package org.openmarkov.integrationTests.integrationTests.localization.autolocalization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.plugin.Filter;
import org.openmarkov.plugin.PluginLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.fail;

/**
 * See the method {@link CheckToStringOverridden#addToStringOnMissingClasses()}, which is the purpose of this test
 * class.
 *
 * @author jrico
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CheckToStringOverridden {
    
    /**
     * This tests searches for classes extending {@link Localizable}, and if they don't override
     * {@link Object#toString()}, it modifies their .java file to add said method.
     * <p>
     * The classes it investigate for are minimal, so if you have a class A extending {@link Localizable} and a class B
     * extending A, only A is required to override {@link Object#toString()}. The class B would be ignored for this
     * matter as when the class A overrides {@link Object#toString()}, it will also be overridden for B.
     * <p>
     * After overriding {@link Object#toString()} for these classes who missed doing it, it will check if there was
     * classes that still don't have {@link Object#toString()} overridden (This might happen if their .java file could
     * not be modified), and if there is any, the test will fail and list the classes that aren't overriding
     * {@link Object#toString()}.
     * <p>
     * Important: This test won't add {@link Object#toString()} for some type of classes, such as anonymous classes or
     * inner classes.
     */
    @Test
    public void addToStringOnMissingClasses() {
        var superClassesMissingToString = CheckToStringOverridden.classesMissingToString(
                CheckToStringOverridden.getSuperLocalizablesClasses());
        var unfixedSuperClasses = new HashSet<>(superClassesMissingToString);
        superClassesMissingToString.forEach(superClassMissingToString -> {
            var classLocation = CheckToStringOverridden.getClassLocation(superClassMissingToString);
            if (classLocation == null) return;
            try {
                var classContents = Files.readString(classLocation.toPath());
                var lastBracketLocation = classContents.lastIndexOf('}');
                classContents = classContents.substring(0, lastBracketLocation) +
                        "\n" +
                        "    @Override public String toString() {\n" +
                        "        return this.localize();\n" +
                        "    }\n" + classContents.substring(lastBracketLocation);
                Files.writeString(classLocation.toPath(), classContents);
                unfixedSuperClasses.remove(superClassMissingToString);
            } catch (IOException e) {
            }
        });
        if (!unfixedSuperClasses.isEmpty()) {
            fail("Some classes implementing " + Localizable.class.getName() + " don't override toString()."
                         + System.lineSeparator()
                         + "Add a to string on the following classes like this '@Override public String toString() { return this.localize(); }':"
                         + System.lineSeparator()
                         + unfixedSuperClasses.stream()
                                              .map(Class::getName)
                                              .sorted()
                                              .map(name -> "\t- " + name)
                                              .collect(Collectors.joining(System.lineSeparator())));
        }
    }
    
    /**
     * Takes a list of classes, and returns those not overriding {@link Object#toString()}.
     *
     * @param superLocalizables the original list of classes to investigate.
     * @return the classes not overriding {@link Object#toString()}.
     */
    private static @NotNull List<Class<Localizable>> classesMissingToString(List<Class<Localizable>> superLocalizables) {
        return superLocalizables
                .stream()
                .filter(localizableClass -> {
                    try {
                        return localizableClass.getDeclaredMethod("toString") == null;
                    } catch (NoSuchMethodException e) {
                        return true;
                    }
                })
                .toList();
    }
    
    /**
     * Gets every class implementing {@link Localizable} that is not extending another class that also implements
     * {@link Localizable}
     *
     * @return every class implementing {@link Localizable} directly.
     */
    private static @NotNull List<Class<Localizable>> getSuperLocalizablesClasses() {
        return CheckToStringOverridden
                .getAllLocalizablesClasses()
                .stream()
                .filter(localizableClass ->
                                CheckToStringOverridden.getSuperClasses(localizableClass)
                                                       .stream()
                                                       .noneMatch(Localizable.class::isAssignableFrom))
                .toList();
    }
    
    /**
     * Gets the file location of a class.
     *
     * @param originClass The class to extract its file location from
     * @return the file location of a class.
     */
    public static @Nullable File getClassLocation(Class<?> originClass) {
        try {
            URL classUrl = originClass.getResource(originClass.getSimpleName() + ".class");
            String path = new File(classUrl.getFile()).getAbsolutePath();
            path = path.replace("target\\classes", "src\\main\\java");
            path = path.substring(0, path.length() - ".class".length());
            path += ".java";
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    
    /**
     * Gets all classes implementing {@link Localizable}.
     *
     * @return all classes implementing {@link Localizable}.
     */
    private static @NotNull List<Class<Localizable>> getAllLocalizablesClasses() {
        return new PluginLoader()
                .loadAllPlugins(Filter.filter().toImplement(Localizable.class))
                .stream()
                .map(localizableClass -> (Class<Localizable>) localizableClass)
                .filter(localizableClass -> !localizableClass.isInterface())
                .toList();
    }
    
    /**
     * Gets a list of all classes extended by the parameterized class.
     *
     * @param baseClass the class to get all of its superclasses from.
     * @return a list of all classes extended by the parameterized class.
     */
    private static ArrayList<Class<?>> getSuperClasses(Class<?> baseClass) {
        var superClasses = new ArrayList<Class<?>>();
        while (true) {
            baseClass = baseClass.getSuperclass();
            if (baseClass == null) {
                return superClasses;
            }
            superClasses.add(baseClass);
        }
    }
    
    
}

