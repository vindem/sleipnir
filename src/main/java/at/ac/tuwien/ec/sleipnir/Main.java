package at.ac.tuwien.ec.sleipnir;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
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
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTCostResearch;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HeftEchoResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.PEFTEnergyScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.PEFTOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.RandomScheduler;
import at.ac.tuwien.ec.scheduling.offloading.bruteforce.BruteForceRuntimeOffloader;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

public class Main {
	
	public static void main(String[] arg)
	{
		processArgs(arg);
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		Date date = new Date();		
		
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> inputSamples = generateSamples(SimulationSetup.iterations);
		PrintWriter[] writers = new PrintWriter[SimulationSetup.algorithms.length];
		int writerIndex = 0;
		
		for(String algoName : SimulationSetup.algorithms)
		{
			String filename = SimulationSetup.outfile
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
							+ ".data";
			
			JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> histogram = runSparkSimulation(
					jscontext, inputSamples, algoName);
			
			try {
				writers[writerIndex] = new PrintWriter(filename,"UTF-8");
				writers[writerIndex].println(MontecarloStatisticsPrinter.getHeader());
				writers[writerIndex].println("Algorithm: " + algoName);
				runSparkSimulation(jscontext, inputSamples, algoName);
				writers[writerIndex].println(histogram.first());
				writerIndex++;
			} 
			catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(histogram.first());
		}
		for(PrintWriter writer : writers)
		{
			writer.close();
			writer.flush();
		}		
		jscontext.close();
	}

	private static JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> runSparkSimulation(
			JavaSparkContext jscontext, ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples, String algoritmName) {
		JavaRDD<Tuple2<MobileApplication,MobileCloudInfrastructure>> input = jscontext.parallelize(inputSamples);
		
		JavaPairRDD<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<MobileApplication,MobileCloudInfrastructure>, 
				OffloadScheduling, Tuple5<Integer,Double,Double,Double,Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<Tuple2<OffloadScheduling, Tuple5<Integer,Double,Double,Double,Double>>> call(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues)
							throws Exception {
						ArrayList<Tuple2<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>>> output = 
								new ArrayList<Tuple2<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>>>();
						OffloadScheduler singleSearch;
						switch(algoritmName){
						case "weighted":
							singleSearch = new HeftEchoResearch(inputValues);
							break;
						case "heft":
							singleSearch = new HEFTResearch(inputValues);
							break;
						case "hbatt":
							singleSearch = new HEFTBattery(inputValues);
							break;
						case "hcost":
							singleSearch = new HEFTCostResearch(inputValues);
							break;
						//case "nsgaIII":
							//singleSearch = new NSGAIIIResearch(currentApp,I);
							//break;
						//case "moplan":
							//singleSearch = new MOEdgePlanning(currentApp, I);
							//break;
						default:
							singleSearch =  new HEFTResearch(inputValues);
						}
						
						ArrayList<OffloadScheduling> offloads = (ArrayList<OffloadScheduling>) singleSearch.findScheduling();
						if(offloads != null)
							for(OffloadScheduling os : offloads) 
							{
								output.add(
										new Tuple2<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>>(os,
												new Tuple5<Integer,Double,Double,Double,Double>(
														1,
														os.getRunTime(),
														os.getUserCost(),
														os.getBatteryLifetime(),
														os.getProviderCost()
														)));
							}
						return output.iterator();
					}
		});
		
		System.out.println(results.first());
		
		JavaPairRDD<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>> aggregation = 
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
		
		JavaPairRDD<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>> histogram = 
				aggregation.mapToPair(
						new PairFunction<Tuple2<OffloadScheduling,Tuple5<Integer, Double, Double, Double, Double>>,
						OffloadScheduling,Tuple5<Integer, Double, Double, Double, Double>>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> call(
									Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> arg0)
											throws Exception {
								Tuple5<Integer, Double, Double, Double, Double> val = arg0._2();
								Tuple5<Integer, Double, Double, Double, Double> tNew 
								= new Tuple5<Integer, Double, Double, Double, Double>
								(
										val._1(),
										val._2()/val._1(),
										val._3()/val._1(),
										val._4()/val._1(),
										val._5()/val._1()
										);

								return new Tuple2<OffloadScheduling,Tuple5<Integer, Double, Double, Double, Double>>(arg0._1,tNew);
							}


						}

						);
		return histogram;
	}

	private static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> samples = new ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>>();
		for(int i = 0; i < iterations; i++)
		{
			MobileWorkload globalWorkload = new MobileWorkload();
			WorkloadGenerator generator = new WorkloadGenerator();
			for(int j = 0; j< SimulationSetup.mobileNum; j++)
				globalWorkload.joinParallel(generator.setupWorkload(SimulationSetup.appNumber, "mobile_"+j));
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			DefaultMobileDevicePlanner.setupMobileDevices(inf,SimulationSetup.mobileNum);
			DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<MobileApplication,MobileCloudInfrastructure> singleSample = new Tuple2<MobileApplication,MobileCloudInfrastructure>(globalWorkload,inf);
			samples.add(singleSample);
		}
		return samples;
	}

		//Creates samples for each spark worker
	private static String selectAppArguments(String targetApp) {
		String tmp = "";
		switch(targetApp){
		case "NAVI": 
			tmp+="maps_size="+SimulationSetup.navigatorMapSize;
			break;
		case "ANTIVIRUS":
			tmp+="file_size="+SimulationSetup.antivirusFileSize;
			break;
		case "FACEREC":
			tmp+="image_size="+SimulationSetup.facerecImageSize;
			break;
		case "CHESS":
			tmp+="chess_mi="+SimulationSetup.chess_mi;
			break;
		case "FACEBOOK":
			tmp+="image_size="+SimulationSetup.facebookImageSize;
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
				SimulationSetup.batteryCapacity = Double.parseDouble(tmp[1]);
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
				//SimulationSetup.appNumber = wlRuns;
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
				SimulationSetup.navigatorMapSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-file-size="))
			{
				String[] tmp = s.split("=");
				// 1/input, to be used for lambda of exponential distribution
				SimulationSetup.antivirusFileSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-image-size="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.facerecImageSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-latency="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.lambdaLatency = (int) (Double.parseDouble(tmp[1]));
				continue;
			}
			if(s.startsWith("-chess-mi="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.chess_mi = (1.0/Double.parseDouble(tmp[1]));
				continue;
			}
			if(s.startsWith("-alpha="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.EchoAlpha = Double.parseDouble(tmp[1]);
			}
			if(s.startsWith("-beta="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.EchoBeta = Double.parseDouble(tmp[1]);
			}
			if(s.startsWith("-gamma="))
			{
				String[] tmp = s.split("=");
				SimulationSetup.EchoGamma = Double.parseDouble(tmp[1]);
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
				SimulationSetup.mobileApplication = tmp[1];
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
	

}
