/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.variableElimination;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.algorithm.variableElimination.VariableEliminationCore;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.net.URL;
import java.util.List;

/**
 * @author Manuel Arias
 */
public class BayesianNetworkTest {
    
    @BeforeEach public void setUp() throws Exception {
    }
    
    //TODO: Most code here cannot compile due to changes in the structure
    
    /*
    @Test
    public void testBayesianNetworksInference() throws Exception {
        NetsRepository netsRepository = new NetsRepository();
        List<URL> bayesianNetworksURLList = netsRepository.getNetworks(BayesianNetworkType.getUniqueInstance());
        PGMXReader_0_2 reader = new PGMXReader_0_2();
        for (URL bayesianNetworkURL : bayesianNetworksURLList) {
            ProbNet probNet = reader.loadProbNet(bayesianNetworkURL.getFile(), bayesianNetworkURL.openStream());
            System.out.println("Checking network: " + bayesianNetworkURL.getFile());
            VariableEliminationCore elimination = new VariableEliminationCore(probNet);
            elimination.getPosteriorValues();
        }
    }
    */
    
    
}
