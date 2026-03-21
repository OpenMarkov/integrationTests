/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterOfVariables;
import org.openmarkov.inference.algorithm.huginPropagation.HuginForest;
import org.openmarkov.integrationTests.inference.util.Util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Compares the four main elimination heuristics on the largest Bayesian networks in the repository.
 * <p>
 * For each network and each heuristic, it measures the treewidth (max clique size - 1) with a
 * 2-minute timeout. A lower treewidth means a better heuristic.
 * <p>
 * This test is diagnostic: it always passes but prints a comparison table to stdout.
 * Use it to detect broken heuristics (e.g. one that does not terminate).
 *
 * @author Generated for OpenMarkov
 */
public class HeuristicComparisonTest {

    private static final int TIMEOUT_MINUTES = 2;
    private static final int MAX_NETWORKS = 10;

    private static final String[] HEURISTIC_CLASS_NAMES = {
            "org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination",
            "org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn",
            "org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize",
            "org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination",
            "org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill"
    };

    @Test
    public void compareHeuristicsOnLargeNetworks() throws IOException {
        List<ProbNet> networks = loadLargestBayesianNetworks();
        if (networks.isEmpty()) {
            System.out.println("No networks available for comparison.");
            return;
        }

        // Column widths for the table
        int nameWidth = 30;
        int colWidth = 20;

        // Print header
        System.out.println();
        System.out.println("=== Elimination Heuristic Comparison (treewidth = max clique vars - 1) ===");
        System.out.println();
        String header = padRight("Network", nameWidth) + padRight("Vars", 6);
        for (String className : HEURISTIC_CLASS_NAMES) {
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            header += padRight(simpleName, colWidth);
        }
        System.out.println(header);
        System.out.println("-".repeat(nameWidth + 6 + colWidth * HEURISTIC_CLASS_NAMES.length));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            for (ProbNet net : networks) {
                String networkName = getNetworkName(net);
                int numVars = net.getVariables().size();
                StringBuilder row = new StringBuilder();
                row.append(padRight(networkName, nameWidth));
                row.append(padRight(String.valueOf(numVars), 6));

                for (String className : HEURISTIC_CLASS_NAMES) {
                    String result = runHeuristicWithTimeout(executor, net, className);
                    row.append(padRight(result, colWidth));
                }
                System.out.println(row);
            }
        } finally {
            executor.shutdownNow();
        }
        System.out.println();
    }

    private String runHeuristicWithTimeout(ExecutorService executor, ProbNet net, String heuristicClassName) {
        Future<String> future = executor.submit(() -> {
            try {
                Class<?> hClass = Class.forName(heuristicClassName);
                List<List<Variable>> variablesToEliminate = new ArrayList<>();
                variablesToEliminate.add(net.getVariables());
                Constructor<?> ctor = hClass.getConstructor(ProbNet.class, List.class);
                EliminationHeuristic heuristic = (EliminationHeuristic) ctor.newInstance(net, variablesToEliminate);
                HuginForest forest = new HuginForest(net.copy(), heuristic);
                int treewidth = computeTreewidth(forest);
                int sumCliques = computeSumCliques(forest);
                return treewidth + " (sum=" + sumCliques + ")";
            } catch (Exception e) {
                return "ERROR: " + e.getClass().getSimpleName();
            }
        });

        try {
            return future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "TIMEOUT (>" + TIMEOUT_MINUTES + "min)";
        } catch (ExecutionException e) {
            return "ERROR: " + e.getCause().getClass().getSimpleName();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "INTERRUPTED";
        }
    }

    private int computeTreewidth(HuginForest forest) {
        return forest.getNodes().stream()
                .mapToInt(c -> c.getVariables().size())
                .max()
                .orElse(0) - 1;
    }

    private int computeSumCliques(HuginForest forest) {
        int sum = 0;
        for (ClusterOfVariables cluster : forest.getNodes()) {
            sum += cluster.size();
        }
        return sum;
    }

    private List<ProbNet> loadLargestBayesianNetworks() throws IOException {
        // Load all BNs from the repository, keep only pure-TablePotential ones (triangulation-compatible)
        List<ProbNet> all = Util.readProbNetsDB(BayesianNetworkType.getUniqueInstance());
        all = Util.filterNonPureTablePotentialProbNets(all);
        // Util.readProbNetsDB already sorts ascending by variable count; reverse for largest-first
        all.sort(Comparator.comparingInt((ProbNet n) -> n.getVariables().size()).reversed());
        return all.subList(0, Math.min(MAX_NETWORKS, all.size()));
    }

    private String getNetworkName(ProbNet net) {
        String name = net.getName();
        if (name == null || name.isBlank()) {
            name = "unnamed";
        }
        return name.length() > 29 ? name.substring(0, 29) : name;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) {
            return s.substring(0, width - 1) + " ";
        }
        return s + " ".repeat(width - s.length());
    }
}
