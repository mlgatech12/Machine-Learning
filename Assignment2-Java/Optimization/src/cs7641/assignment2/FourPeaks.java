package cs7641.assignment2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.SwapNeighbor;
import opt.example.*;
import opt.ga.CrossoverFunction;
import opt.ga.DiscreteChangeOneMutation;
import opt.ga.SingleCrossOver;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.StandardGeneticAlgorithm;
import opt.ga.SwapMutation;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;


public class FourPeaks {

    /** The n value */
    private static final int N = 200;
    /** The t value */
    private static final int T = N / 5;

    public static void write_output_to_file(String output_dir, String file_name, String results, boolean final_result) {
        try {
            if (final_result) {
                String augmented_output_dir = output_dir + "/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String full_path = augmented_output_dir + "/" + file_name;
                Path p = Paths.get(full_path);
                if (Files.notExists(p)) {
                    Files.createDirectories(p.getParent());
                }
                PrintWriter pwtr = new PrintWriter(new BufferedWriter(new FileWriter(full_path, true)));
                synchronized (pwtr) {
                    pwtr.println(results);
                    pwtr.close();
                }
            }
            else {
                String full_path = output_dir + "/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + file_name;
                Path p = Paths.get(full_path);
                Files.createDirectories(p.getParent());
                Files.write(p, results.getBytes());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public static void main(String[] args) {
    	check_optimal_params();
    }
    
    
    /*

    public static void main(String[] args) {
    	
        double start, end, time;
        int[] iters = {10,100,1000,2500,5000,8000,10000,15000,30000,50000};
        int testRuns = 3;
        
        System.out.println("start");
        String final_result = "";
        final_result = "Iterations" + "," + "Score_RHC" + "," + "Time_RHC" + "," + "Score_SA" + "," +
        "Time_SA" + "," + "Score_GA" + "," + "Time_GA" + "," + "Score_MIMIC" + "," + "Time_MIMIC";

        write_output_to_file("Optimization_Results", "four_peaks_results.csv", final_result, true);
        
        for (int iter : iters) {

            int sum_rhc = 0;
            int sum_sa = 0;
            int sum_ga = 0;
            int sum_mimic = 0;

            double time_rhc = 0;
            double time_sa = 0;
            double time_ga = 0;
            double time_mimic = 0;

            for (int j = 0; j < testRuns; j++) {
                int[] ranges = new int[N];
                Arrays.fill(ranges, 2);
                EvaluationFunction ef = new ContinuousPeaksEvaluationFunction(T);
                Distribution odd = new DiscreteUniformDistribution(ranges);
                NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
                MutationFunction mf = new DiscreteChangeOneMutation(ranges);
                CrossoverFunction cf = new SingleCrossOver();
                Distribution df = new DiscreteDependencyTree(.1, ranges);
                HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
                GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
                ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);


                start = System.nanoTime();
                RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);
                FixedIterationTrainer fit = new FixedIterationTrainer(rhc, iter);
                fit.train();
                end = System.nanoTime();
                time = end - start;
                time /= Math.pow(10, 9);
                sum_rhc += ef.value(rhc.getOptimal());
                time_rhc += time;
                
                start = System.nanoTime();
                SimulatedAnnealing sa = new SimulatedAnnealing(1E11, .95, hcp);
                fit = new FixedIterationTrainer(sa, iter);
                fit.train();
                end = System.nanoTime();
                time = end - start;
                time /= Math.pow(10, 9);
                sum_sa += ef.value(sa.getOptimal());
                time_sa += time;
                
                
                start = System.nanoTime();
                StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 100, 10, gap);
                fit = new FixedIterationTrainer(ga, iter);
                fit.train();
                end = System.nanoTime();
                time = end - start;
                time /= Math.pow(10, 9);
                sum_ga += ef.value(ga.getOptimal());
                time_ga += time;
                
                
                //System.out.println("ga: " + ef.value(ga.getOptimal()));
                //System.out.println(time);
                start = System.nanoTime();
                MIMIC mimic = new MIMIC(200, 20, pop);
                fit = new FixedIterationTrainer(mimic, iter);
                fit.train();
                end = System.nanoTime();
                time = end - start;
                time /= Math.pow(10, 9);
                sum_mimic += ef.value(mimic.getOptimal());
                time_mimic += time;
                //System.out.println("Mimic: " + ef.value(mimic.getOptimal()));
                //System.out.println(time);
             
            }

            int score_rhc = sum_rhc / testRuns;
            int score_sa = sum_sa / testRuns;
            int score_ga = sum_ga / testRuns;
            int score_mimic = sum_mimic / testRuns;

            double avg_time_rhc = time_rhc / testRuns;
            double avg_time_sa = time_sa / testRuns;
            double avg_time_ga = time_ga / testRuns;
            double avg_time_mimic = time_mimic / testRuns;

            final_result = "";
            final_result = iter + "," + Integer.toString(score_rhc) + "," + Double.toString(avg_time_rhc) + "," +
            		Integer.toString(score_sa) + "," + Double.toString(avg_time_sa) + "," +
            		Integer.toString(score_ga) + "," + Double.toString(avg_time_ga) + "," +
            		Integer.toString(score_mimic) + "," + Double.toString(avg_time_mimic);

            write_output_to_file("Optimization_Results", "four_peaks_results.csv", final_result, true);
        }
        System.out.println("end"); 
    }
    */
    
    public static void check_optimal_params() {
    	System.out.print("start four peaks check_optimal_params");
    	String final_result = "";
        double start, end, time;
        int[] popSize = {25, 50, 100, 200, 500, 1000, 1500, 2000, 50000, 100000};
        int[] toKeepSize = {10, 30, 75, 150, 300, 500, 550, 600, 800, 1000};
        int testRuns = 3;
        
        double score = 0;
        double time_mimic = 0;
        
        Random random = new Random();
        
        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        
        EvaluationFunction ef = new FourPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new SingleCrossOver();
        Distribution df = new DiscreteDependencyTree(.1, ranges);
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);

        
        final_result =  "Args" + "," + "Score" + "," + "Time";
        write_output_to_file("Optimization_Results", "four_peaks_optimization_results.csv", final_result, true);
        
        for (int j = 0; j < popSize.length; j++) {
        	start = System.nanoTime();
            MIMIC mimic = new MIMIC(200, 20, pop);
            FixedIterationTrainer fit = new FixedIterationTrainer(mimic, 5000);
            fit.train();
            end = System.nanoTime();
            time = end - start;
            time /= Math.pow(10, 9);
        	score = ef.value(mimic.getOptimal());
        	time_mimic = time;
        	
        	final_result = "";
        	final_result =  "(" + popSize[j] +"-" + toKeepSize[j] + ")" + "," + 
        			Double.toString(score) + "," + Double.toString(time_mimic);

            write_output_to_file("Optimization_Results", "four_peaks_optimization_results.csv", final_result, true);
        	
        }
        
        System.out.print("end four peaks check_optimal_params");
        
    }
    
}
