package org.openmarkov.staticAnalysis;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.configuration.LocalPreference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.openmarkov.gui.configuration.OpenMarkovLocalPreferences;

public class ShowPreferences {
    
    public static void main(String[] args) {
        var sortedByName = new ArrayList<>(OpenMarkovLocalPreferences.getAllPreferences());
        sortedByName.sort(Comparator.comparing(ShowPreferences::pathNameOfPreference));
        for (LocalPreference<?> preference : sortedByName) {
            System.out.println(ShowPreferences.pathNameOfPreference(preference) + ": " + preference.get());
        }
    }
    
    private static @NotNull String pathNameOfPreference(LocalPreference<?> localPreference) {
        return localPreference.getPreferencePath()
                              .stream()
                              .collect(Collectors.joining("/"));
    }
    
}
