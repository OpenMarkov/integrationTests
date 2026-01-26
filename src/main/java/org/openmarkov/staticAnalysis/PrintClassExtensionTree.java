package org.openmarkov.staticAnalysis;

import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.ExtensionTree;
import org.openmarkov.plugin.PluginSearch;

import java.util.Comparator;

public class PrintClassExtensionTree {
    
    public static void main(String[] args) {
        PluginSearch.init()
                    .childrenOf(Potential.class)
                    .filter(ClassUtils::isConcrete)
                    .extensionTree()
                    .print();
    }
    
}
