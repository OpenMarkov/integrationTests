package org.openmarkov.staticAnalysis;

import org.openmarkov.plugin.PluginSearch;

import java.util.Comparator;

public class ListClasses {
    
    public static void main(String[] args) {
        PluginSearch.init()
                    .childrenOf(Exception.class)
                    .stream()
                    .sorted(Comparator.comparing(Class::getName))
                    .forEach(System.out::println);
    }
    
}
