package at.ac.tuwien.ec.scheduling.simulation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.uma.jmetal.util.JMetalLogger;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.OffloadSchedulingHistogram;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.WeightedFunctionResearch;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

import static java.util.Arrays.asList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 *
 * @author Vincenzo
 */
public class MonteCarloSimulation{

    private int times;
    private MobileCloudInfrastructure I;
    private MobileApplication A;
    
    //For one application, one user
    public MonteCarloSimulation(
    		int times,
    		MobileApplication A,
    		MobileCloudInfrastructure I) {

    	this.times = times;
    	this.A = A;
    	this.I = I;
    }
    
    public HashMap<String,OffloadSchedulingHistogram> startSimulation() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    	//HashMap<Deployment, Couple<Double, Double>> histogram = new HashMap<>();
    	HashMap<String,OffloadSchedulingHistogram> histograms = new HashMap<String,OffloadSchedulingHistogram>();
    	//DeploymentsHistogram histogram = new DeploymentsHistogram();

    	for(int i = 0; i < SimulationSetup.algorithms.length; i++)
    		histograms.put(SimulationSetup.algorithms[i], new OffloadSchedulingHistogram());
    	    	   	
    	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    	for(String algorithm : histograms.keySet()){
    		ArrayList<OffloadScheduling> depList = new ArrayList<OffloadScheduling>();
    		List<Future<ArrayList<OffloadScheduling>>> futures = new LinkedList<Future<ArrayList<OffloadScheduling>>>();
    		/*
    		 * long wExtractStart = System.currentTimeMillis();
    		 * MobileApplication A = extractWorkload(muWorkload);
    		 * long wExtractEnd = System.currentTimeMillis();
    		 * System.out.println("Workload extracted in secs: "+(wExtractEnd - wExtractStart)/1000.0);
    		 */
    		Class<? extends SimIteration> iteration = selectAlgorithm(algorithm);     		
    		for (int j = 0; j < times; j++) {
    			SimIteration search = iteration.getConstructor(null).newInstance(null);
    			//I = helper.setupInfrastructure();
    			//SimulationConstants.workload_runs = (int) Math.floor(ExponentialDistributionGenerator.getNext(SimulationConstants.workload_runs_lambda));
    			if(j%1 == 0)
    			{
    				//System.gc();
    				if(!SimulationSetup.batch)
    					System.out.println("Iteration " + j);
    			}
    			search.setChosenAlgorithm(algorithm);
    			//if(!algorithm.equals("nsgaIII"))
    			futures.add(executor.submit(search));
    			
    		}

    		{
    			for(int k = 0; k < futures.size(); k++)
    				try	{
    					ArrayList<OffloadScheduling> results = (ArrayList<OffloadScheduling>)futures.get(k).get();
    					if(results != null)
    						depList.addAll(results);
    				} catch (InterruptedException | ExecutionException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (NullPointerException | ArrayIndexOutOfBoundsException e1)
    			{
    					System.err.println("Error on " + algorithm);
    			} /*catch (TimeoutException e) {
    				// TODO Auto-generated catch block
    				System.err.println("Timeout expired on " + algorithm);
    			}*/
    			catch(Throwable t)
    			{
    				t.printStackTrace();
    			}
    		}
    		
    		double pos = depList.size();
    		double size = depList.size();
    		System.out.println("Deployment found for " + algorithm + ": " + size);
    		for (OffloadScheduling d : depList) {
    			if (histograms.get(algorithm).containsKey(d)) {
    				//Double newCount = histogram.get(d).getA() + 1.0; //montecarlo frequency
    				//Double newPos = histogram.get(d).getB() + ((pos + 1)/ (size+1));
    				double scoreUpdate = ((pos + 1)/ (size+1));
    				histograms.get(algorithm).update(d, scoreUpdate);
    			} else {
    				//histogram.put(d, new Couple(1.0, pos / (size + 1)));
    				histograms.get(algorithm).add(d, pos / (size+1));
    			}
    			pos--;
    		}
    		//System.out.println(I);

    	}
    	
    	executor.shutdown();
    	return histograms;
    }

    private Class<? extends SimIteration> selectAlgorithm(String algorithm) {
    	switch(algorithm){
		case "weighted":
			return WeightedFunctionResearch.class;
		/*case "mincost":
			singleSearch = new MINCostResearch(currentApp, I);
			break;
		case "maxbatt":
			singleSearch = new MAXBattery(currentApp, I);
			break;
		case "minmin":
			singleSearch = new MinMinResearch(currentApp, I);
			break;
		case "heft":
			singleSearch = new HeftResearch(currentApp, I);
			break;
		case "hbatt":
			singleSearch = new HeftBatteryResearch(currentApp, I);
			break;
		case "hcost":
			singleSearch = new HeftCostResearch(currentApp, I);
			break;
		case "echo":
			singleSearch = new HeftEchoResearch(currentApp,I);
			break;
		/*case "moheft":
			singleSearch = new MOHeftResearch(currentApp,I);
			break;
		case "nsgaIII":
			singleSearch = new NSGAIIIResearch(currentApp,I);
			break;
		case "moplan":
			singleSearch = new MOEdgePlanning(currentApp, I);
			break;
			*/
		default:
			return WeightedFunctionResearch.class;
		}
	}

	public MobileApplication extractWorkload(ArrayList<MobileApplication> workload){
    	MobileWorkload mWorkload = new MobileWorkload(workload);
    	return mWorkload;
    }
    	
}
