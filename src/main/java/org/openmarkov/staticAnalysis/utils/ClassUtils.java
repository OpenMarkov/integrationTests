package org.openmarkov.staticAnalysis.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A series of methods to work with classes using reflections.
 *
 * @author jrico
 */
public class ClassUtils {
    
    /**
     * Gets a sorted list of all superclasses and interfaces of a class.
     * <p>
     * In the resulting list, all interfaces appear before the classes, and both interfaces and classes are ordered by
     * inheritance. This means a class in the position {@code n} can be a child of all classes {@code n-1},
     * {@code n-2}... but it cannot be a child of classes in positions {@code n+1}, {@code n+2}...
     */
    public static ArrayList<Class<?>> extensionClassesOf(Class<?> aClass) {
        var superClasses = new ArrayList<Class<?>>();
        var superClass = aClass.getSuperclass();
        //Adds every superclass
        while (superClass != null) {
            superClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }
        //Adds all the interfaces of the class and its superclasses
        superClasses.addAll(
                Stream.concat(superClasses.stream(), Stream.of(aClass))
                      .flatMap(anotherSuperClass -> Arrays.stream(anotherSuperClass.getInterfaces()))
                      .collect(Collectors.toSet()));
        Comparator<Class<?>> compareExtension = (class1, class2) -> {
            if (class2.isAssignableFrom(class1)) {
                return 1;
            }
            if (class1.isAssignableFrom(class2)) {
                return -1;
            }
            return 0;
        };
        Comparator<Class<?>> compareIsInterface = Comparator.comparing(Class::isInterface);
        //Interfaces are before classes, and then they are ordered by inheritance
        superClasses.sort(compareIsInterface.reversed().thenComparing(compareExtension));
        return superClasses;
    }
    
    public static boolean isConcrete(Class<?> aClass) {
        return !aClass.isInterface() && !aClass.isAnnotation() && !Modifier.isAbstract(aClass.getModifiers());
    }
    
}
