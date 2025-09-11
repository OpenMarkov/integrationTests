package org.openmarkov.staticAnalysis;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import org.openmarkov.core.exception.*;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ExceptionInstantations {
    
    
    private static final List<Class<? extends Throwable>> IGNORED_EXCEPTIONS = List.of(
            UnrecoverableException.class,
            UnreacheableException.class,
            NotSupportedOperationException.class,
            InvalidArgumentException.class,
            NonProjectablePotentialException.PotentialCannotBeConvertedToATable.class
    );
    
    private static boolean shouldCheckException(Class<Throwable> throwableClass) {
        if (!IOpenMarkovException.class.isAssignableFrom(throwableClass)) {
            return false;
        }
        var isAnIgnoredException = ExceptionInstantations.IGNORED_EXCEPTIONS
                .stream()
                .anyMatch(ignoredException ->
                                  ignoredException.isAssignableFrom(throwableClass));
        if (isAnIgnoredException) {
            return false;
        }
        return true;
    }
    
    public static void main(String[] args) throws IOException {
        ParseUtils.baseOpenMarkovParsedClasses()
                  .sorted(Comparator
                                  .comparing((CompilationUnit unit) -> unit
                                          .getPackageDeclaration()
                                          .map(d -> d.getNameAsString())
                                          .orElse(""))
                                  .thenComparing(unit -> unit
                                          .getPrimaryTypeName().orElse(""))
                  )
                  .flatMap(parsedClass -> parsedClass
                          .findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)
                          .stream())
                  .filter(objectCreationExpr ->
                                  Throwable.class.isAssignableFrom(ParseUtils.classOf(objectCreationExpr.getType())))
                  .filter(objectCreationExpr ->
                                  ExceptionInstantations.shouldCheckException((Class<Throwable>) ParseUtils.classOf(objectCreationExpr.getType())))
                  .forEach(objectCreationExpr -> {
                      CompilationUnit origin = ParseUtils.sourceOf(objectCreationExpr);
                      Optional<Range> range = objectCreationExpr.getRange();
                      String packageName = origin.getPackageDeclaration().map(NodeWithName::getNameAsString)
                                                 .orElse("");
                      String className = origin.getPrimaryTypeName().orElse(null);
                      String qualifiedName = packageName + "." + className;
                      var methodName = ParseUtils
                              .superSearch(objectCreationExpr, CallableDeclaration.class)
                              .map(CallableDeclaration::getNameAsString)
                              .orElse("aMethod");
                      int line = range.get().begin.line;
                      String exceptionClassName = ParseUtils.classOf(objectCreationExpr.getType()).getSimpleName();
                      
                      System.out.println(String.format("%s at %s.%s(%s.java:%d)", exceptionClassName,
                                                       qualifiedName, methodName, className, line));
                  });
    }
    
    
}
