open module org.openmarkov.integrationTests {
    requires org.jetbrains.annotations;
    requires org.json;
    requires org.openmarkov.annotation_processing;
    requires org.openmarkov.core;
    requires org.openmarkov.costeffectiveness;
    requires org.openmarkov.dbgenerator;
    requires org.openmarkov.full;
    requires org.openmarkov.gui;
    requires org.openmarkov.inference;
    requires org.openmarkov.io;
    requires org.openmarkov.io.database.elvira;
    requires org.openmarkov.io.database.excel;
    requires org.openmarkov.io.database.weka;
    requires org.openmarkov.learning.algorithm;
    requires org.openmarkov.learning.core;
    requires org.openmarkov.learning.gui;
    requires org.openmarkov.learning.metric;
    requires org.openmarkov.sensitivityanalysis;
    requires org.openmarkov.stochasticpropagationoutput;
    requires java.xml;
    requires org.junit.jupiter.api;
    
    exports org.openmarkov.integrationTests;
    exports org.openmarkov.integrationTests.localize;
}