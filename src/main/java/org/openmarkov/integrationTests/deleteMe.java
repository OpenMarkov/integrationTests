package org.openmarkov.integrationTests;

import org.openmarkov.core.exception.NotAllNodesHavePoliciesException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.dialog.ExceptionDialog;
import org.openmarkov.gui.exception.MissingPreferenceException;
import org.openmarkov.learning.algorithm.hillclimbing.HillClimbingAlgorithm;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;

import java.util.List;

public class deleteMe {
    
    public static void main(String[] args) {
        ProbNet net = new ProbNet();
        net.addNode(new Variable("My conditioning variable"), NodeType.CHANCE);
        net.addNode(new Variable("My variable without policy"), NodeType.CHANCE);
        net.addNode(new Variable("My other variable without policy"), NodeType.CHANCE);
        
        ExceptionDialog.show(new NotAllNodesHavePoliciesException(
                net.getNode("My conditioning variable"),
                List.of(
                        net.getNode("My variable without policy"),
                        net.getNode("My other variable without policy")
                ))
        );
        
        ExceptionDialog.show(new MissingPreferenceException("MyMissingPreference"));
    }
    
}
