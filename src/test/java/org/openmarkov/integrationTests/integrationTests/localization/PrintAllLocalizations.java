package org.openmarkov.integrationTests.integrationTests.localization;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.StringDatabase;

import java.util.Comparator;

public class PrintAllLocalizations {
    
    @Test
    public void printAllLocalizations() {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        stringDatabase.getAllBundles().values().stream()
                      .flatMap(bundle -> bundle.getKeys().stream())
                      .sorted(Comparator.comparing(string -> string))
                      .forEach(key -> System.out.println(key + " - " + stringDatabase.getString(key)));
    }
    
}
