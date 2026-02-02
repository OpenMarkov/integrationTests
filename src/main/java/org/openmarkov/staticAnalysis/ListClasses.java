package org.openmarkov.staticAnalysis;

import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.common.PotentialPanelPlugin;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class ListClasses {
    
    public static void main(String[] args) {
        AtomicInteger index = new AtomicInteger();
        
        PluginSearch.full()
                    .extending(LayoutAlgorithm2D.class)
                    .stream()
                    //.filter(ClassUtils::isConcrete)
                    .sorted(Comparator.comparing(Class::getName))
                    .forEach(classToPrint -> System.out.println(index.incrementAndGet() + " - " + classToPrint.getName()));
    }
    
}
