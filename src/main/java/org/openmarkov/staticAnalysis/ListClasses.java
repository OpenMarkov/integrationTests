package org.openmarkov.staticAnalysis;

import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class ListClasses {
    
    public static void main(String[] args) {
        AtomicInteger index = new AtomicInteger();
        
        PluginSearch.full()
                    .childrenOf(LookAndFeel.class)
                    .stream()
                    .filter(ClassUtils::isConcrete)
                    .sorted(Comparator.comparing(Class::getName))
                    .forEach(classToPrint -> System.out.println(index.incrementAndGet() + " - " + classToPrint.getName()));
    }
    
}
