package cs7641.assignment2;

/* Updated by Nidhi Agrawal, derived from AbaloneTest.java */
import dist.*;
import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
public class Marketing_RHC {

    private static Instance[] instances = initializeInstances();
    private static Instance[] mktg_train_set = Arrays.copyOfRange(instances, 0, 31647);
    private static Instance[] mktg_test_set = Arrays.copyOfRange(instances, 31647, 45211);

    private static int inputLayer = 51, hiddenLayer = 10, outputLayer = 1;
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(instances);

    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[1];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[1];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[1];
    private static String[] oaNames = {"RHC"};
    private static String results = "";

    private static DecimalFormat df = new DecimalFormat("0.000");

    public static void main(String[] args) {
        for(int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                new int[] {inputLayer, hiddenLayer, outputLayer});
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        oa[0] = new RandomizedHillClimbing(nnop[0]);
        int[] iters = {10, 100, 500, 1000, 2500, 5000, 10000};
        //int[] iters = {2,5};
        String header = "";
        header = "Algo" + "," + "Iterations" + "," + "TrainingAccuracy" + "," + "TrainingTime" + "," + "TestingTime" ;
        write_output_to_file("Optimization_Results", "mktg_rhc_train_results.csv", header, true);

        header = "Algo" + "," + "Iterations" + "," + "TestingAccuracy" + "," + "TestingTime" ;
        write_output_to_file("Optimization_Results", "mktg_rhc_test_results.csv", header, true);
                
        
        for (int iter : iters) {
        	results = "";
        
	        for(int i = 0; i < oa.length; i++) {
	            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
	            train(oa[i], networks[i], oaNames[i], iter); //trainer.train();
	            end = System.nanoTime();
	            trainingTime = end - start;
	            trainingTime /= Math.pow(10,9);
	
	            Instance optimalInstance = oa[i].getOptimal();
	            networks[i].setWeights(optimalInstance.getData());
	
	            double predicted, actual;
	            start = System.nanoTime();
	            for(int j = 0; j < mktg_train_set.length; j++) {
	                networks[i].setInputValues(mktg_train_set[j].getData());
	                networks[i].run();
	
	                predicted = Double.parseDouble(mktg_train_set[j].getLabel().toString());
	                actual = Double.parseDouble(networks[i].getOutputValues().toString());
	                double trash = Math.abs(predicted - actual) < 0.5 ? correct++ : incorrect++;
	                
	
	            }
	            end = System.nanoTime();
	            testingTime = end - start;
	            testingTime /= Math.pow(10,9);
	            results = ""; 
	            results = oaNames[i] + "," + iter + ","  + df.format(correct / (correct + incorrect) * 100)
                + "," + df.format(trainingTime) +  "," + df.format(testingTime);
	            
	            write_output_to_file("Optimization_Results", "mktg_rhc_train_results.csv", results, true);
	            
	            start = System.nanoTime();
                correct = 0;
                incorrect = 0;
                for (int j = 0; j < mktg_test_set.length; j++) {
                    networks[i].setInputValues(mktg_test_set[j].getData());
					//System.out.println("input set:" + test_set[j].getData());
					//System.out.println("target:" + );
                    networks[i].run();

                    actual = Double.parseDouble(mktg_test_set[j].getLabel().toString());
                    predicted = Double.parseDouble(networks[i].getOutputValues().toString());
                    double trash = Math.abs(predicted - actual) < 0.5 ? correct++ : incorrect++;
                    
					//System.out.println("actual:" + actual + ", predicted:" + predicted);
					
                    
                }
                end = System.nanoTime();
                testingTime = end - start;
                testingTime /= Math.pow(10, 9);
                
                results = ""; 
                results = oaNames[i] + "," + iter + "," + df.format(correct / (correct + incorrect) * 100)
                + "," + df.format(testingTime);
	            
                write_output_to_file("Optimization_Results", "mktg_rhc_test_results.csv", results, true);
	            
	        }
	        
	        
	        
        }

        System.out.println(results);
    }

    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName, int iteration) {
        //System.out.println("\nError results for " + oaName + "\n---------------------------");

    	int trainingIterations = iteration;
        for(int i = 0; i < trainingIterations; i++) {
            oa.train();

            double error = 0;
            for(int j = 0; j < instances.length; j++) {
                network.setInputValues(instances[j].getData());
                network.run();

                Instance output = instances[j].getLabel(), example = new Instance(network.getOutputValues());
                example.setLabel(new Instance(Double.parseDouble(network.getOutputValues().toString())));
                error += measure.value(output, example);
            }

            //System.out.println(df.format(error));
        }
    }

    private static Instance[] initializeInstances() {

        double[][][] attributes = new double[45211][][];

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("src/cs7641/assignment2/bank_data_full.csv")));

            for(int i = 0; i < attributes.length; i++) {
                Scanner scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[51]; // 51 attributes
                attributes[i][1] = new double[1];

                for(int j = 0; j < 51; j++)
                    attributes[i][0][j] = Double.parseDouble(scan.next());

                attributes[i][1][0] = Double.parseDouble(scan.next());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Instance[] instances = new Instance[attributes.length];

        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            // classifications range from 0 to 30; split into 0 - 14 and 15 - 30
            instances[i].setLabel(new Instance(attributes[i][1][0]));
        }

        return instances;
    }
    
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
}
