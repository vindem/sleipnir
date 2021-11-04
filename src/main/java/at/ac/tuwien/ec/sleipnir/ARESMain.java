package at.ac.tuwien.ec.sleipnir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.jgrapht.Graph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.model.software.mobileapps.AntivirusApp;
import at.ac.tuwien.ec.model.software.mobileapps.ChessApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacerecognizerApp;
import at.ac.tuwien.ec.model.software.mobileapps.NavigatorApp;
import at.ac.tuwien.ec.model.software.mobileapps.WorkloadGenerator;
import at.ac.tuwien.ec.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.provisioning.ares.FirstStageAresPlanner;
import at.ac.tuwien.ec.provisioning.ares.FirstStageAresSolution;
import at.ac.tuwien.ec.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.provisioning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.provisioning.edge.mo.MOEdgePlanning;
import at.ac.tuwien.ec.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.provisioning.triobj.TriobjPlanner;
import at.ac.tuwien.ec.provisioning.triobj.TriobjSolution;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.PEFTEnergyScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.PEFTOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HEFTCostResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HeftEchoResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.RandomScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling.NSGAIIIResearch;
import at.ac.tuwien.ec.scheduling.offloading.bruteforce.BruteForceRuntimeOffloader;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.utils.MontecarloStatisticsPrinter;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

public class ARESMain {
	
	public static void main(String[] arg)
	{
		processArgs(arg);
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		
		class FrequencyComparator implements Serializable,
			Comparator<Tuple2<FirstStageAresSolution, Tuple5<Integer, Double, Double, Double, Double>>>
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			public int compare(Tuple2<FirstStageAresSolution, Tuple5<Integer, Double, Double,Double, Double>> o1,
					Tuple2<FirstStageAresSolution, Tuple5<Integer, Double, Double,Double, Double>> o2) {
				// TODO Auto-generated method stub
				return o1._2()._1() - o2._2()._1();
			}
			
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		Date date = new Date();		
		
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<MobileApplication,MobileDataDistributionInfrastructure>> inputSamples = generateSamples(SimulationSetup.iterations);
		PrintWriter[] writers = new PrintWriter[SimulationSetup.algorithms.length];
		int writerIndex = 0;
		int iteration = 0;
		for(String algoName : SimulationSetup.algorithms)
		{
			/*String filename = SimulationSetup.outfile
					+ algoName +"/"
					+ dateFormat.format(date)
					+ "-" + SimulationSetup.MAP_M
					+ "X"
					+ SimulationSetup.MAP_N
					+ "-edge-planning="
					+ SimulationSetup.edgePlanningAlgorithm
					+ "-" + SimulationSetup.mobileApplication
					+ "-lambdaLatency=" + SimulationSetup.lambdaLatency
					+ "-CLOUD=" + SimulationSetup.cloudNum
					+ "-EDGE=" + SimulationSetup.edgeNodes
					+ "-" + selectAppArguments(SimulationSetup.mobileApplication)
					+ "-" + algoName
							+ ((algoName.equals("weighted"))? 
									"-alpha="+SimulationSetup.EchoAlpha
									+"-beta="+SimulationSetup.EchoBeta
									+"-gamma="+SimulationSetup.EchoGamma
									: "") 
							+ ((SimulationSetup.cloudOnly)? "-ONLYCLOUD":
								"-eta-" + SimulationSetup.Eta)
							+ ".data";*/
			String filename = SimulationSetup.outfile
					+ algoName +"/" + dateFormat.format(date)+ "_ARES-ParetoFront-" + iteration + ".data";
			//String filename = SimulationSetup.outfile
				//	+ algoName +"/" + dateFormat.format(date)+ "_TRIOBJ-ParetoFront-" + iteration + ".data";
			File outFile = new File(filename);
			iteration++;
			JavaPairRDD<FirstStageAresSolution, Tuple5<Integer, Double, Double, Double, Double>> histogram = runSparkSimulation(
					jscontext, inputSamples, algoName);
			//JavaPairRDD<TriobjSolution, Tuple5<Integer, Double, Double, Double, Double>> histogram = runSparkSimulation(
				//	jscontext, inputSamples, algoName);
			if(!outFile.exists())
			{
				outFile.getParentFile().mkdirs();
				try {
					outFile.createNewFile();
					writers[writerIndex] = new PrintWriter(outFile,"UTF-8");
					writers[writerIndex].println(MontecarloStatisticsPrinter.getHeader());
					writers[writerIndex].println("Algorithm: " + algoName);
					
					Iterator<FirstStageAresSolution> planIterator = histogram.keys().toLocalIterator();
					//Iterator<TriobjSolution> planIterator = histogram.keys().toLocalIterator();
					while(planIterator.hasNext()) {
						//Tuple2<InfrastructureProvisioningPlan, Tuple5<Integer, Double, Double, Double, Double>> mostFrequent = histogram.max(new FrequencyComparator());
						//writers[writerIndex].println(mostFrequent._1().toString() + "\t" + mostFrequent._2()._1() + "\t" + mostFrequent._2()._2() 
						//+ "\t" + mostFrequent._2()._ght3() + "\t" + mostFrequent._2()._4() + "\t" + mostFrequent._2()._5() );
						FirstStageAresSolution currPlan = planIterator.next();
						//TriobjSolution currPlan = planIterator.next();
						writers[writerIndex].println(currPlan.toString() );
						System.out.println(currPlan);
					}
					
					String secondPhaseInputFile = "/home/vincenzo/eclipse-workspace/reliability solutions/stage2-100-reloaded.txt";
					File outFile2 = new File("/home/vincenzo/eclipse-workspace/reliability solutions/Stage2_100_latest.txt"); 
					
					outFile2.createNewFile();
					PrintWriter fileWriter2 = new PrintWriter(outFile2,"UTF-8");
					
						FileReader reader = new FileReader(secondPhaseInputFile);
						BufferedReader csvReader = new BufferedReader(reader);
						String tmp;
						boolean haveOriginal = false;
						FirstStageAresSolution augmentedFrom = null, secondPhaseAugmented;
						double minEnergyConsumption = Double.MAX_VALUE;
						FirstStageAresSolution minEnrg = null;
						while((tmp = csvReader.readLine()) != null)
						{
							if(tmp.contains(", "))
							{
								if(haveOriginal)
								{
									secondPhaseAugmented = new FirstStageAresSolution(tmp);
									if(secondPhaseAugmented.getEnergyConsumption() < minEnergyConsumption)
									{
										minEnrg = secondPhaseAugmented;
										minEnergyConsumption = secondPhaseAugmented.getEnergyConsumption();
									}
									fileWriter2.println("Original: ");
									fileWriter2.println("Latency:\t"+secondPhaseAugmented.getAverageDistance()+"\tEnergy:\t"
											+secondPhaseAugmented.getEnergyConsumption()+"\tActive energy:\t"+secondPhaseAugmented.getActiveEnergy());
									fileWriter2.println("Augmented: ");
									fileWriter2.println("Latency:\t"+secondPhaseAugmented.getAverageDistance()+"\tEnergy:\t"
									+secondPhaseAugmented.getEnergyConsumption()+"\tActive energy:\t"+secondPhaseAugmented.getActiveEnergy());
									haveOriginal = false;
								}
								else
								{
									augmentedFrom = new FirstStageAresSolution(tmp);
									if(augmentedFrom.getEnergyConsumption() < minEnergyConsumption)
									{
										minEnrg = augmentedFrom;
										minEnergyConsumption = augmentedFrom.getEnergyConsumption();
									}
									haveOriginal = true;
								}
							}
							else if(tmp.contains("Original") || tmp.contains("Augmented"))
							{
								;
							}
							else
								fileWriter2.println(tmp);
						}
						System.out.println("####################");
						System.out.println(minEnrg);
						csvReader.close();
						fileWriter2.close();
					}
						//csvReader.readLine();
						//csvReader.readLine();
						//csvReader.readLine();
						
						//String original = "2, 3, 5, 8, 13, 14, 16, 17, 19, 21, 24, 26, 30, 31, 32, 34, 35, 38, 40, 41, 43, 44, 47, 49, 52, 53, 54, 55, 57, 59, 62, 67, 70, 74, 75, 77, 84, 86, 87, 88, 90, 92, 94, 96, 97, 98, 99, 100, 102, 104, 106, 110, 111, 112, 114, 116, 118, 120, 122, 124, 129, 131, 132, 133, 138, 139, 141, 142, 144, 147, 148, 149, 151, 152, 153, 154, 157, 159, 166, 167, 168, 171, 172, 174, 176, 177, 181, 182, 183, 187, 189, 190, 192, 194, 196, 198, 199, 201, 205, 206, 208, 209, 210, 211, 212, 213, 214, 216, 219, 220, 223, 229, 230, 231, 233, 234, 237, 239, 242, 243, 246, 247, 249, 250, 251, 252, 253, 254, 257, 258, 260, 262, 263, 264, 265, 267, 269, 273, 277, 278, 279, 281, 282, 283, 286, 287, 288, 289, 292, 293, 294, 300, 303, 304, 305, 306, 307, 309, 313, 315, 321, 322, 328, 330, 334, 340, 343, 344, 348, 349, 350, 351, 353, 356, 357, 358, 359, 360, 362, 366, 367, 370, 374, 375, 377, 380, 381, 382, 383, 384, 388, 389, 390, 391, 392, 393, 395, 397, 403, 404, 406, 408, 411, 412, 414, 415, 416, 417, 420, 424, 425, 426, 428, 429, 432, 433, 434, 435, 437, 438, 439, 440, 442, 444, 446, 447, 449, 450, 451, 453, 455, 457, 458, 460, 463, 465, 467, 470, 471, 472, 473, 476, 480, 481, 484, 486, 488, 491, 496, 500";
						//String sPAugmented = "2, 3, 5, 8, 13, 14, 16, 17, 19, 21, 24, 26, 30, 31, 32, 34, 35, 38, 40, 41, 43, 44, 47, 49, 52, 53, 54, 55, 57, 59, 62, 67, 70, 74, 75, 77, 84, 86, 87, 88, 90, 92, 94, 96, 97, 98, 99, 100, 102, 104, 106, 110, 111, 112, 114, 116, 118, 120, 122, 124, 129, 131, 132, 133, 138, 139, 141, 142, 144, 147, 148, 149, 151, 152, 153, 154, 157, 159, 166, 167, 168, 171, 172, 174, 176, 177, 181, 182, 183, 187, 189, 190, 192, 194, 196, 198, 199, 201, 205, 206, 208, 209, 210, 211, 212, 213, 214, 216, 219, 220, 223, 229, 230, 231, 233, 234, 237, 239, 242, 243, 246, 247, 249, 250, 251, 252, 253, 254, 257, 258, 260, 262, 263, 264, 265, 267, 269, 273, 277, 278, 279, 281, 282, 283, 286, 287, 288, 289, 292, 293, 294, 300, 303, 304, 305, 306, 307, 309, 313, 315, 321, 322, 328, 330, 334, 340, 343, 344, 348, 349, 350, 351, 353, 356, 357, 358, 359, 360, 362, 366, 367, 370, 374, 375, 377, 380, 381, 382, 383, 384, 388, 389, 390, 391, 392, 393, 395, 397, 403, 404, 406, 408, 411, 412, 414, 415, 416, 417, 420, 424, 425, 426, 428, 429, 432, 433, 434, 435, 437, 438, 439, 440, 442, 444, 446, 447, 449, 450, 451, 453, 455, 457, 458, 460, 463, 465, 467, 470, 471, 472, 473, 476, 480, 481, 484, 486, 488, 491, 496, 500";
						//String aFrom = "2, 3, 5, 8, 13, 14, 16, 17, 19, 21, 24, 26, 30, 31, 32, 34, 35, 38, 40, 41, 43, 44, 47, 49, 52, 53, 54, 55, 57, 59, 62, 67, 70, 74, 75, 77, 84, 86, 87, 88, 90, 92, 94, 96, 97, 98, 99, 100, 102, 104, 106, 110, 111, 112, 114, 116, 118, 120, 122, 124, 129, 131, 132, 133, 138, 139, 141, 142, 144, 147, 148, 149, 151, 152, 153, 154, 157, 159, 166, 167, 168, 171, 172, 174, 176, 177, 181, 182, 183, 187, 189, 190, 192, 194, 196, 198, 199, 201, 205, 206, 208, 209, 210, 211, 212, 213, 214, 216, 219, 220, 223, 229, 230, 231, 233, 234, 237, 239, 242, 243, 246, 247, 249, 250, 251, 252, 253, 254, 257, 258, 260, 262, 263, 264, 265, 267, 269, 273, 277, 278, 279, 281, 282, 283, 286, 287, 288, 289, 292, 293, 294, 300, 303, 304, 305, 306, 307, 309, 313, 315, 321, 322, 328, 330, 334, 340, 343, 344, 348, 349, 350, 351, 353, 356, 357, 358, 359, 360, 362, 366, 367, 370, 374, 375, 377, 380, 381, 382, 383, 384, 388, 389, 390, 391, 392, 393, 395, 397, 403, 404, 406, 408, 411, 412, 414, 415, 416, 417, 420, 424, 425, 426, 428, 429, 432, 433, 434, 435, 437, 438, 439, 440, 442, 444, 446, 447, 449, 450, 451, 453, 455, 457, 458, 460, 463, 465, 467, 470, 471, 472, 473, 476, 480, 481, 484, 486, 488, 491, 496, 500";
						
						//InfrastructureProvisioningPlan secondPhaseSelected = new InfrastructureProvisioningPlan(inputSamples.get(0)._2(),original);
						//csvReader.readLine();
						//InfrastructureProvisioningPlan secondPhaseAugmented = new InfrastructureProvisioningPlan(inputSamples.get(0)._2(),sPAugmented);
						//csvReader.readLine();
						//InfrastructureProvisioningPlan augmentedFrom = new InfrastructureProvisioningPlan(inputSamples.get(0)._2(),aFrom);
						//csvReader.close();
						//System.out.println("################");
						//System.out.println("Second phase selected:");
						//System.out.println(secondPhaseSelected);
						
					
					
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//histogram.saveAsTextFile(filename+"_001");
			//writers[writerIndex].println("a");
			//System.out.println(histogram.first());
			//histogram.saveAsTextFile("filedimerda");
			writerIndex++;
			
			//System.out.println(histogram.first()._1());
			
		}
		for(PrintWriter writer : writers)
		{
			writer.close();
			writer.flush();
		}		
		jscontext.close();
	}

	private static JavaPairRDD<FirstStageAresSolution, Tuple5<Integer, Double, Double, Double, Double>> runSparkSimulation(
			JavaSparkContext jscontext, ArrayList<Tuple2<MobileApplication, MobileDataDistributionInfrastructure>> inputSamples, String algoritmName) {
		JavaRDD<Tuple2<MobileApplication,MobileDataDistributionInfrastructure>> input = jscontext.parallelize(inputSamples);
		
		JavaPairRDD<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<MobileApplication,MobileDataDistributionInfrastructure>, 
				FirstStageAresSolution, Tuple5<Integer,Double,Double,Double,Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<Tuple2<FirstStageAresSolution, Tuple5<Integer,Double,Double,Double,Double>>> call(Tuple2<MobileApplication, MobileDataDistributionInfrastructure> inputValues)
							throws Exception {
						//ArrayList<Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>> output = 
						//		new ArrayList<Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>>();
						ArrayList<Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>> output = 
										new ArrayList<Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>>();
						OffloadScheduler singleSearch;
						
						ArrayList<FirstStageAresSolution> solutions;
						/*switch(SimulationSetup.placementAlgorithm)
						{
						case "RANDOM":
							RandomEdgePlanner.setupEdgeNodes(infrastructure);
							DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
							break;
						case "ALL":
							EdgeAllCellPlanner.setupEdgeNodes(infrastructure);
							DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
							break;
						case "ares":
							FirstStageAresPlanner planner = new FirstStageAresPlanner(inputValues);
							planner.setupEdgeNodes(infrastructure);
							break;
						default:
							FirstStageAresPlanner aresP = new FirstStageAresPlanner(inputValues);
							aresP.setupEdgeNodes(infrastructure);
						}*/
						
						System.out.println("Executing planner");
						//FirstStageAresPlanner aresP = new FirstStageAresPlanner();
						FirstStageAresPlanner aresP = new FirstStageAresPlanner();
						System.out.println("Retrieving solutions");
						//solutions = (ArrayList<FirstStageAresSolution>) aresP.getSolutionList();
						solutions = (ArrayList<FirstStageAresSolution>) aresP.getSolutionList();
						System.out.println("Solutions retrieved!");
						/*switch(algoritmName){
						case "weighted":
							singleSearch = new HeftEchoResearch(inputValues._1(), infrastructure);
							break;
						case "heft":
							singleSearch = new HEFTResearch(inputValues._1(), infrastructure);
							break;
						case "hbatt":
							singleSearch = new HEFTBattery(inputValues._1(), infrastructure);
							break;
						case "hcost":
							singleSearch = new HEFTCostResearch(inputValues._1(), infrastructure);
							break;
						case "bforce-rt":
							singleSearch = new BruteForceRuntimeOffloader(inputValues._1(), infrastructure);
							break;
						case "nsgaIII":
							singleSearch = new NSGAIIIResearch(inputValues._1(), infrastructure);
							break;
						
						default:
							
						}*/
						//ArrayList<FirstStageAresSolution> temp = new ArrayList<FirstStageAresSolution>();
						//for(FirstStageAresSolution solution : solutions) {
						ArrayList<FirstStageAresSolution> temp = new ArrayList<FirstStageAresSolution>();
						for(FirstStageAresSolution solution : solutions) {
							//aresP.applySolutionToInfrastructure(solution, infrastructure);
							//ArrayList<InfrastructureProvisioningPlan> offloads = (ArrayList<InfrastructureProvisioningPlan>) singleSearch.findScheduling();
							//if(offloads != null)
								//for(InfrastructureProvisioningPlan os : offloads) 
								//{
							
									//System.out.println(solution);
									//plan.setInfEnergyConsumption(solution.getEnergyConsumption());
									//plan.setRunTime(solution.getAverageDistance());
									
							/*if(!temp.contains(solution)) 
							{
								temp.add(solution);
								output.add(
										new Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>
										(solution,
												new Tuple5<Integer,Double,Double,Double,Double>(
														1,
														solution.getAverageDistance(),
														0.0,
														solution.getEnergyConsumption(),
														0.0
														)));
							}*/
							if(!temp.contains(solution)) 
							{
								temp.add(solution);
								output.add(
										new Tuple2<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>>
										(solution,
												new Tuple5<Integer,Double,Double,Double,Double>(
														1,
														solution.getAverageDistance(),
														0.0,
														solution.getEnergyConsumption(),
														0.0
														)));
							}
						}
						return output.iterator();
					}
		});
		
		//System.out.println(results.first());
		
		JavaPairRDD<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>> aggregation = 
				results.reduceByKey(
				new Function2<Tuple5<Integer,Double,Double,Double,Double>,
				Tuple5<Integer,Double,Double,Double,Double>,
				Tuple5<Integer,Double,Double,Double,Double>>()
				{
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple5<Integer, Double, Double, Double, Double> call(
							Tuple5<Integer, Double, Double, Double, Double> off1,
							Tuple5<Integer, Double, Double, Double, Double> off2) throws Exception {
						// TODO Auto-generated method stub
						return new Tuple5<Integer, Double, Double, Double, Double>(
								off1._1() + off2._1(),
								off1._2() + off2._2(),
								off1._3() + off2._3(),
								off1._4() + off2._4(),
								off1._5() + off2._5()
								);
					}
					
				}
			);
		
		//System.out.println(aggregation.first());
		
		JavaPairRDD<FirstStageAresSolution,Tuple5<Integer,Double,Double,Double,Double>> histogram = 
				aggregation.mapToPair(
						new PairFunction<Tuple2<FirstStageAresSolution,Tuple5<Integer, Double, Double, Double, Double>>,
						FirstStageAresSolution,Tuple5<Integer, Double, Double, Double, Double>>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<FirstStageAresSolution, Tuple5<Integer, Double, Double, Double, Double>> call(
									Tuple2<FirstStageAresSolution, Tuple5<Integer, Double, Double, Double, Double>> arg0)
											throws Exception {
								Tuple5<Integer, Double, Double, Double, Double> val = arg0._2();
								Tuple5<Integer, Double, Double, Double, Double> tNew 
								= new Tuple5<Integer, Double, Double, Double, Double>
								(
										val._1(),
										val._2()/val._1(),
										val._3()/val._1(),
										val._4()/val._1(),
										(val._5()/SimulationSetup.iterations)/1e6
										);

								return new Tuple2<FirstStageAresSolution,Tuple5<Integer, Double, Double, Double, Double>>(arg0._1,tNew);
							}


						}

						);
		return histogram;
	}

	public static ArrayList<Tuple2<MobileApplication, MobileDataDistributionInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<MobileApplication,MobileDataDistributionInfrastructure>> samples = new ArrayList<Tuple2<MobileApplication,MobileDataDistributionInfrastructure>>();
		SimulationSetup.iotDevicesCoordinates = readIoTCoordsFromFile("/home/vincenzo/eclipse-workspace/Sleipnir/traffic-lights/SensorNodes.csv");
		SimulationSetup.admissibleEdgeCoordinates = readEdgeCoordinatesFromFile("/home/vincenzo/eclipse-workspace/Sleipnir/traffic-lights/EdgeNodes.csv");
		SimulationSetup.failureProbList = readFailureProbFromFile("/home/vincenzo/eclipse-workspace/reliability solutions/failure.txt");
		samples.add(new Tuple2<MobileApplication,MobileDataDistributionInfrastructure>(new FacebookApp(0), new MobileDataDistributionInfrastructure()));
		System.out.println("Sampling completed!");
		return samples;
	}

		private static ArrayList<Double> readFailureProbFromFile(String string) {
			ArrayList<Double> failures = new ArrayList<Double>(); 
			try
			{
				FileReader reader = new FileReader(string);
				BufferedReader csvReader = new BufferedReader(reader);
				csvReader.readLine();
				String row;
				while((row = csvReader.readLine()) != null)
					failures.add(Double.parseDouble(row));
				csvReader.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			return failures;
		}
	

		private static ArrayList<Coordinates> readIoTCoordsFromFile(String filepath) {
		ArrayList<Coordinates> coords = new ArrayList<Coordinates>();
		
		try {
			FileReader reader = new FileReader(filepath);
			BufferedReader csvReader = new BufferedReader(reader);
			csvReader.readLine();
			String row;
			while((row = csvReader.readLine()) != null) 
			{
				String[] data = row.split(",");
				coords.add(new Coordinates(Double.parseDouble(data[1]), Double.parseDouble(data[2])));
			}
			csvReader.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return coords;
	}

	//Creates samples for each spark worker
	private static String selectAppArguments(String targetApp) {
		String tmp = "";
		switch(targetApp){
		case "NAVI": 
			tmp+="maps_size="+OffloadingSetup.navigatorMapSize;
			break;
		case "ANTIVIRUS":
			tmp+="file_size="+OffloadingSetup.antivirusFileSize;
			break;
		case "FACEREC":
			tmp+="image_size="+OffloadingSetup.facerecImageSize;
			break;
		case "CHESS":
			tmp+="chess_mi="+OffloadingSetup.chessMI;
			break;
		case "FACEBOOK":
			tmp+="image_size="+OffloadingSetup.facebookImageSize;
			break;
		}
		return tmp;
	}


	private static void processArgs(String[] args) {
		for(String s : args)
		{
			if(s.startsWith("-mapM="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.MAP_M = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-mapN="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.MAP_N = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-edgePlanning="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.edgePlanningAlgorithm = tmp[1];
				continue;
			}
			if(s.startsWith("-mobile="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.mobileNum = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-traceIn="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.electricityTraceFile = tmp[1];
				continue;
			}
			if(s.startsWith("-outfile="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.outfile = tmp[1];
				continue;
			}
			if(s.startsWith("-iter="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.iterations = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-battery="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.batteryCapacity = Double.parseDouble(tmp[1]);
				continue;
			}
			if(s.startsWith("-cloud="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.cloudNum = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-edge="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.edgeNodes = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-wl-runs=")){
				String[] tmp = s.split("=");
				String[] input = tmp[1].split(",");
				int[] wlRuns = new int[input.length];
				for(int i = 0; i < input.length; i++)
					wlRuns[i] = Integer.parseInt(input[i]);
				SimulationSetup.numberOfApps = wlRuns[0];
				continue;
			}
			if(s.equals("-batch"))
			{
				SimulationSetup.batch = true;
				continue;
			}
			if(s.startsWith("-map-size="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.navigatorMapSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-file-size="))
			{
				String[] tmp = s.split("=");
				// 1/input, to be used for lambda of exponential distribution
				OffloadingSetup.antivirusFileSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-image-size="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.facerecImageSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-latency="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.lambdaLatency = (int) (Double.parseDouble(tmp[1]));
				continue;
			}
			if(s.startsWith("-chess-mi="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.chessMI = (1.0/Double.parseDouble(tmp[1]));
				continue;
			}
			if(s.startsWith("-alpha="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.EchoAlpha = Double.parseDouble(tmp[1]);
			}
			if(s.startsWith("-beta="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.EchoBeta = Double.parseDouble(tmp[1]);
			}
			if(s.startsWith("-gamma="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.EchoGamma = Double.parseDouble(tmp[1]);
			}
			if(s.startsWith("-eta="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.Eta = Double.parseDouble(tmp[1]);
				continue;
			}
			if(s.startsWith("-app="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.mobileApplication = tmp[1];
				continue;
			}
			if(s.startsWith("-algo="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.algorithms = tmp[1].split(",");
				continue;
			}
			if(s.equals("-cloudonly"))
				SimulationSetup.cloudOnly = true;
		}
	}
	
	private static ArrayList<Coordinates> readEdgeCoordinatesFromFile(String filepath) {
		ArrayList<Coordinates> coords = new ArrayList<Coordinates>();
		try {
			FileReader reader = new FileReader(filepath);
			BufferedReader csvReader = new BufferedReader(reader);
			csvReader.readLine();
			String row;
			while((row = csvReader.readLine()) != null) 
			{
				String[] data = row.split(",");
				coords.add(new Coordinates(Double.parseDouble(data[1]), Double.parseDouble(data[2])));
			}
			csvReader.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return coords;
	}

}
