package org.openmarkov.staticAnalysis;

import org.openmarkov.core.action.AddFindingEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.plugin.PluginSearch;

import java.util.Comparator;

public class ListClasses {
    
    public static void main(String[] args) {
        PluginSearch.init()
                    .childrenOf(PNConstraint.class)
                    .stream()
                    .sorted(Comparator.comparing(Class::getName))
                    .forEach(System.out::println);
    }
    
}
