package org.openmarkov.staticAnalysis;

import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.ExtensionTree;
import org.openmarkov.plugin.PluginSearch;

import java.util.Comparator;

public class PrintClassExtensionTree {
    
    public static void main(String[] args) {
        var search = PluginSearch.init().childrenOf(Potential.class).filter(ClassUtils::isConcrete).extensionTree();
        search.print();
        //new org.openmarkov.core.model.network.potential.AugmentedProbTablePotential(List.of(), PotentialRole.JOINT_PROBABILITY);
        search.breathFirstLevelOrderQueue()
              .reversed()
              .stream()
              .map(ExtensionTree::getCurrentClass)
              .filter(ClassUtils::isConcrete)
              .sorted(Comparator.comparing(Class::getName))
              .forEach(System.out::println);
        
        
    }
    
}
