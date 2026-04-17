/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterOfVariables;
import org.openmarkov.inference.algorithm.huginPropagation.HuginForest;
import org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination;
import org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize;
import org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn;
import org.openmarkov.inference.heuristic.rollout.RolloutCostFunction;
import org.openmarkov.inference.heuristic.rollout.RolloutCriterion;
import org.openmarkov.inference.heuristic.rollout.RolloutElimination;
import org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination;
import org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill;
import org.openmarkov.integrationTests.inference.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Benchmark that compares all elimination heuristics (including rollout parameter
 * variants) on the largest Bayesian networks in the repository.
 * <p>
 * For each network/configuration pair, reports treewidth (max clique variables - 1)
 * and junction-tree sum (total size of all clique tables). The junction tree is built
 * via {@link HuginForest}, which is the actual structure used by OpenMarkov's
 * inference engine.
 * <p>
 * Configurations include:
 * <ul>
 *   <li>7 baseline heuristics: Simple, MinFill, WMFill, MCS, CanoMoral, LA-MF, LA-MCS</li>
 *   <li>8 rollout variants: depth {1,2} x base {WMF,MCS} x criterion {JTS,MCT}</li>
 * </ul>
 * <p>
 * Heuristics are parallelized per network row: all configurations for one network
 * run simultaneously, then results are collected before loading the next network.
 * <p>
 * This test always passes; its output is diagnostic only.
 *
 * @author Manuel Arias
 */
@Disabled
public class RolloutBenchmarkTest {

    private static final int TIMEOUT_MINUTES = 10;
    private static final int MAX_NETWORKS = 10;

    private static final long ERROR   = Long.MIN_VALUE;
    private static final long TIMEOUT = Long.MIN_VALUE + 1;

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    private record Config(String name,
                          BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> factory) {}

    private static List<Config> buildConfigs() {
        List<Config> configs = new ArrayList<>();

        // Baselines
        configs.add(new Config("Simple", SimpleElimination::new));
        configs.add(new Config("MFill", MinimalFillIn::new));
        configs.add(new Config("WMFill", WeightedMinFill::new));
        configs.add(new Config("MCS", minimalCliqueSize::new));
        configs.add(new Config("CanoM", CanoMoralElimination::new));

        // Rollout: depth x costFunction x criterion
        for (int d : new int[]{1, 2}) {
            for (var cf : new Object[][]{{"WMF", RolloutCostFunction.WEIGHTED_MIN_FILL},
                                         {"MCS", RolloutCostFunction.MIN_CLIQUE_SIZE}}) {
                for (var cr : new Object[][]{{"JTS", RolloutCriterion.JUNCTION_TREE_SUM},
                                             {"MCT", RolloutCriterion.MAX_CLIQUE_TABLE_SIZE}}) {
                    String cfName = (String) cf[0];
                    RolloutCostFunction costFn = (RolloutCostFunction) cf[1];
                    String crName = (String) cr[0];
                    RolloutCriterion criterion = (RolloutCriterion) cr[1];
                    String name = "R(" + d + "," + cfName + "," + crName + ")";
                    int depth = d;
                    configs.add(new Config(name, (net, vars) ->
                            new RolloutElimination(net, vars, depth, 3, costFn, criterion)));
                }
            }
        }
        return configs;
    }

    // -------------------------------------------------------------------------
    // Main benchmark
    // -------------------------------------------------------------------------

    @Test
    public void benchmarkOnRealNetworks() {
        List<ProbNet> networks = loadLargestBayesianNetworks();
        if (networks.isEmpty()) {
            System.out.println("No networks available for benchmark.");
            return;
        }

        List<Config> configs = buildConfigs();
        int C = configs.size();
        int N = networks.size();
        long[][] treewidths = new long[N][C];
        long[][] jtSums     = new long[N][C];
        long[][] timesMs    = new long[N][C];

        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Rollout benchmark: " + N + " networks, " + C + " configs, "
                + numCores + " threads.");
        System.out.println();

        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        try {
            for (int i = 0; i < N; i++) {
                String netName = getNetworkName(networks.get(i));
                int vars = networks.get(i).getVariables().size();
                System.out.println("Network " + (i + 1) + "/" + N + ": " + netName
                        + " (" + vars + " vars)");

                @SuppressWarnings("unchecked")
                Future<long[]>[] rowFutures = new Future[C];
                for (int j = 0; j < C; j++) {
                    rowFutures[j] = submitConfig(executor, networks.get(i), configs.get(j));
                }
                for (int j = 0; j < C; j++) {
                    long[] result = collectResult(rowFutures[j]);
                    treewidths[i][j] = result[0];
                    jtSums[i][j]     = result[1];
                    timesMs[i][j]    = result[2];
                }
            }
        } finally {
            executor.shutdownNow();
        }

        String[] networkNames = new String[N];
        int[] varCounts = new int[N];
        for (int i = 0; i < N; i++) {
            networkNames[i] = getNetworkName(networks.get(i));
            varCounts[i]    = networks.get(i).getVariables().size();
        }

        System.out.println();
        printTable("TREEWIDTH (lower is better)", networkNames, varCounts, configs, treewidths, timesMs);
        System.out.println();
        printTable("JUNCTION-TREE SUM (lower is better)", networkNames, varCounts, configs, jtSums, timesMs);
        System.out.println();
        printRanking("RANKING by avg normalized JT-sum", networkNames, configs, jtSums);
    }

    // -------------------------------------------------------------------------
    // Heuristic execution
    // -------------------------------------------------------------------------

    private Future<long[]> submitConfig(ExecutorService executor, ProbNet net, Config config) {
        return executor.submit(() -> {
            long t0 = System.currentTimeMillis();
            ProbNet netCopy = moralize(net.copy());
            List<List<Variable>> variablesToEliminate = new ArrayList<>();
            variablesToEliminate.add(netCopy.getVariables());
            EliminationHeuristic heuristic = config.factory.apply(netCopy, variablesToEliminate);
            HuginForest forest = new HuginForest(netCopy.copy(), heuristic);
            long elapsed = System.currentTimeMillis() - t0;
            return new long[]{computeTreewidth(forest), computeSum(forest), elapsed};
        });
    }

    private static long[] collectResult(Future<long[]> future) {
        try {
            return future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new long[]{TIMEOUT, TIMEOUT, -1};
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return new long[]{ERROR, ERROR, -1};
        }
    }

    private static long computeTreewidth(HuginForest forest) {
        return forest.getNodes().stream()
                .mapToInt(c -> c.getVariables().size())
                .max()
                .orElse(0) - 1;
    }

    private static long computeSum(HuginForest forest) {
        long sum = 0;
        for (ClusterOfVariables cluster : forest.getNodes()) {
            sum += cluster.size();
        }
        return sum;
    }

    // -------------------------------------------------------------------------
    // Graph preparation
    // -------------------------------------------------------------------------

    /**
     * Moralizes a Bayesian network and converts it to an undirected graph:
     * <ol>
     *   <li>Adds undirected edges between co-parents of every node.</li>
     *   <li>Replaces all directed edges with undirected ones.</li>
     * </ol>
     * Required before variable elimination so that the heuristic's simulation
     * matches HuginForest's actual junction tree construction.
     */
    private static ProbNet moralize(ProbNet net) {
        // Step 1: marry co-parents
        for (Node node : new ArrayList<>(net.getNodes())) {
            List<Node> parents = node.getParents();
            for (int i = 0; i < parents.size() - 1; i++) {
                for (int j = i + 1; j < parents.size(); j++) {
                    if (!parents.get(i).isNeighbor(parents.get(j))) {
                        net.addLink(parents.get(i), parents.get(j), false);
                    }
                }
            }
        }
        // Step 2: replace directed edges with undirected ones
        for (var link : new ArrayList<>(net.getLinks())) {
            if (link.isDirected()) {
                Node from = link.getFrom();
                Node to = link.getTo();
                net.removeLink(from, to, true);
                if (!from.isNeighbor(to)) {
                    net.addLink(from, to, false);
                }
            }
        }
        return net;
    }

    // -------------------------------------------------------------------------
    // Network loading
    // -------------------------------------------------------------------------

    private List<ProbNet> loadLargestBayesianNetworks() {
        List<ProbNet> all = Util.readProbNetsDB(BayesianNetworkType.getUniqueInstance());
        all = Util.filterNonPureTablePotentialProbNets(all);
        BayesianNetworkType bnType = BayesianNetworkType.getUniqueInstance();
        all = all.stream()
                .filter(n -> n.getNetworkType() == bnType)
                .filter(n -> n.getName() == null || !n.getName().contains("-1-0"))
                .collect(Collectors.toList());
        all.sort(Comparator.comparingInt((ProbNet n) -> n.getVariables().size()).reversed());
        return all.subList(0, Math.min(MAX_NETWORKS, all.size()));
    }

    // -------------------------------------------------------------------------
    // Output formatting
    // -------------------------------------------------------------------------

    private void printTable(String title, String[] netNames, int[] varCounts,
                            List<Config> configs, long[][] values, long[][] timesMs) {
        int nameW = 30;
        int varsW = 6;
        int colW  = 18;
        int C = configs.size();

        System.out.println("=== " + title + " ===");
        System.out.println();

        // Header
        StringBuilder hdr = new StringBuilder();
        hdr.append(padRight("Network", nameW));
        hdr.append(padRight("Vars", varsW));
        for (Config c : configs) {
            hdr.append(padRight(c.name, colW));
        }
        System.out.println(hdr);
        System.out.println("-".repeat(nameW + varsW + colW * C));

        // Data rows
        for (int i = 0; i < netNames.length; i++) {
            StringBuilder row = new StringBuilder();
            row.append(padRight(netNames[i], nameW));
            row.append(padRight(String.valueOf(varCounts[i]), varsW));
            for (int j = 0; j < C; j++) {
                String val;
                if (values[i][j] == ERROR)   val = "ERR";
                else if (values[i][j] == TIMEOUT) val = "T/O";
                else {
                    String metric = formatValue(values[i][j]);
                    val = timesMs[i][j] >= 0 ? metric + " (" + timesMs[i][j] + "ms)" : metric;
                }
                row.append(padRight(val, colW));
            }
            System.out.println(row);
        }
    }

    private void printRanking(String title, String[] netNames, List<Config> configs,
                              long[][] jtSums) {
        int C = configs.size();
        int N = netNames.length;

        System.out.println("=== " + title + " ===");
        System.out.println();

        double[] avgNormalized = new double[C];
        int[] validNets = new int[C];

        for (int i = 0; i < N; i++) {
            long best = Long.MAX_VALUE;
            for (int j = 0; j < C; j++) {
                if (jtSums[i][j] > 0 && jtSums[i][j] < best) {
                    best = jtSums[i][j];
                }
            }
            if (best == Long.MAX_VALUE) continue;

            for (int j = 0; j < C; j++) {
                if (jtSums[i][j] > 0) {
                    avgNormalized[j] += (double) jtSums[i][j] / best;
                    validNets[j]++;
                }
            }
        }

        Integer[] indices = new Integer[C];
        for (int j = 0; j < C; j++) {
            indices[j] = j;
            if (validNets[j] > 0) avgNormalized[j] /= validNets[j];
            else avgNormalized[j] = Double.MAX_VALUE;
        }
        Arrays.sort(indices, (a, b) -> Double.compare(avgNormalized[a], avgNormalized[b]));

        System.out.printf("  %-4s  %-22s  %s%n", "Rank", "Configuration", "Avg normalized JT-sum");
        System.out.println("  " + "-".repeat(55));
        for (int rank = 0; rank < C; rank++) {
            int j = indices[rank];
            String score = avgNormalized[j] == Double.MAX_VALUE ? "N/A"
                    : String.format("%.4f", avgNormalized[j]);
            System.out.printf("  %-4d  %-22s  %s%n", rank + 1, configs.get(j).name, score);
        }
    }

    private static String formatValue(long v) {
        if (v >= 1_000_000_000L) return String.format("%.2fG", v / 1_000_000_000.0);
        if (v >= 1_000_000) return String.format("%.2fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    private static String getNetworkName(ProbNet net) {
        String name = net.getName();
        if (name == null || name.isBlank()) name = "unnamed";
        return abbreviate(name, 29);
    }

    private static String abbreviate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width - 1) + " ";
        return s + " ".repeat(width - s.length());
    }
}
