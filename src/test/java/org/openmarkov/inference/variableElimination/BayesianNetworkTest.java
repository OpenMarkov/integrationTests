/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.variableElimination;


import bitbucket.NetsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Manuel Arias
 */
public class BayesianNetworkTest {
    
    @BeforeEach public void setUp() throws Exception {
    }
    
    //TODO: Most code here cannot compile due to changes in the structure
    /*
    @Test
    public void testBayesianNetworksInference() throws IOException, ParserException {
        NetsRepository netsRepository = new NetsRepository();
        List<URL> bayesianNetworksURLList = netsRepository.getNetworks(BayesianNetworkType.getUniqueInstance());
        PGMXReader_0_2 reader = new PGMXReader_0_2();
        for (URL bayesianNetworkURL : bayesianNetworksURLList) {
            ProbNet probNet = null;
            try {
                probNet = reader.loadProbNet(bayesianNetworkURL.getFile(), bayesianNetworkURL.openStream());
                System.out.println("Checking network: " + bayesianNetworkURL.getFile());
            } catch (ParserException | IOException e) {
                System.err.println("Can not read network: " + bayesianNetworkURL.getFile());
                fail();
            }
            VariableEliminationCore elimination = null;
            try {
                elimination = new VariableEliminationCore(probNet);
            } catch (NotEvaluableNetworkException e) {
                System.err.println("Not evaluable network: " + bayesianNetworkURL.getFile());
                fail();
            }
            try {
                elimination.getPosteriorValues();
            } catch (Exception e) {
                System.err.println("VariableElimination inference fails in: " + bayesianNetworkURL.getFile());
                fail();
            }
        }
    }
    */
    
    
}
