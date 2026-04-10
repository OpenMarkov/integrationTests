package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAllStringBuilderUses {
    
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(parsedClass -> parsedClass.compilationUnit().findAll(MethodCallExpr.class).stream())
                  .forEach(methodCallExpr -> {
                      ResolvedMethodDeclaration resolvedMethodDeclaration;
                      try {
                          resolvedMethodDeclaration = methodCallExpr.resolve();
                      } catch (RuntimeException ex) {
                          resolvedMethodDeclaration = null;
                      }
                      if (!(resolvedMethodDeclaration instanceof ReflectionMethodDeclaration reflectionMethodDeclaration)) {
                          return;
                      }
                      try {
                          var field = forceGetField(reflectionMethodDeclaration, "method", Method.class);
                          if (!field.getDeclaringClass().equals(StringBuilder.class)) {
                              return;
                          }
                          System.out.println((messageIndex.incrementAndGet())
                                                     + " " +
                                                     ParseUtils.getSourceLine(methodCallExpr) + ": " + System.lineSeparator() + methodCallExpr.toString()
                                                                                                                                              .trim());
                          
                      } catch (ReflectiveOperationException ignored) {
                      
                      }
                  });
        
    }
    
    private static <T> T forceGetField(Object source, String fieldName, Class<T> resType) throws ReflectiveOperationException {
        Source sourceElement = Source.of(source);
        ArrayList<Class<?>> classesOfSource = ClassUtils.extensionClassesOf(sourceElement.getTargetClass());
        classesOfSource.add(0, sourceElement.getTargetClass());
        Field field = classesOfSource
                .stream()
                .map(subclass -> {
                    try {
                        return subclass.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .get();
        field.setAccessible(true);
        return resType.cast(field.get(source));
    }
    
    public sealed interface Source {
        record StaticClass(Class<?> aClass) implements Source {
        }
        
        record Instance(Object object) implements Source {
        }
        
        private static Source of(Object object) {
            if (object instanceof Class<?> aClass) {
                return new StaticClass(aClass);
            }
            return new Instance(object);
        }
        
        private Class<?> getTargetClass() {
            return switch (this) {
                case Instance instance -> instance.object.getClass();
                case StaticClass staticClass -> staticClass.aClass;
            };
        }
        
        private Object getInstance() {
            return switch (this) {
                case Instance instance -> instance.object;
                case StaticClass staticClass -> null;
            };
        }
    }
    
}
