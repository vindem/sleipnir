package at.ac.tuwien.ec.sleipnir;

import java.io.File;
import java.io.FileNotFoundException;
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
import at.ac.tuwien.ec.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.provisioning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.provisioning.edge.mo.MOEdgePlanning;
import at.ac.tuwien.ec.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTCostResearch;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HeftEchoResearch;

import at.ac.tuwien.ec.sleipnir.utils.ConfigFileParser;

import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling.NSGAIIIResearch;
import at.ac.tuwien.ec.scheduling.offloading.bruteforce.BruteForceRuntimeOffloader;
import at.ac.tuwien.ec.sleipnir.utils.MontecarloStatisticsPrinter;
import scala.Tuple2;
import scala.Tuple5;

public class OffloadingHelloWorld {
	
	public static void main(String[] arg)
	{
		ConfigFileParser.parseFile("./config/simulation.json");
		if(OffloadingSetup.antivirusDistr + OffloadingSetup.chessDistr+ OffloadingSetup.facebookDistr 
				+ OffloadingSetup.facerecDistr + OffloadingSetup.navigatorDistr != 1.0)
		{
			System.out.println("ERROR: App frequency must sum to 1!");
			return;
		}
		if(OffloadingSetup.antivirusDistr < 0.0 || OffloadingSetup.chessDistr < 0.0 || OffloadingSetup.facebookDistr 
				< 0.0 || OffloadingSetup.facerecDistr < 0.0 || OffloadingSetup.navigatorDistr < 0.0)
		{
			System.out.println("ERROR: App frequencies must be positive!");
			return;
		}
		if(Arrays.asList(arg).contains("-h") || Arrays.asList(arg).contains("-?")) {
			printUsageInfo();
			return;
		}
		processArgs(arg);
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		
		class FrequencyComparator implements Serializable,
			Comparator<Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>>
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			public int compare(Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double,Double, Double>> o1,
					Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double,Double, Double>> o2) {
				// TODO Auto-generated method stub
				return o1._2()._1() - o2._2()._1();
			}
			
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		Date date = new Date();		
		
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		
		
		
		setupAreaParameters();
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> inputSamples = generateSamples(OffloadingSetup.iterations);
		
		String filename = setupOutputFileName(dateFormat, date, OffloadingSetup.algoName);
		File outFile = new File(filename);
		PrintWriter writer;
		
		JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> histogram = runSparkSimulation(
				jscontext, inputSamples, OffloadingSetup.algoName);
			if(!outFile.exists())
			{
				outFile.getParentFile().mkdirs();
				try 
				{
					outFile.createNewFile();
					writer  = new PrintWriter(outFile,"UTF-8");
					writer.println(MontecarloStatisticsPrinter.getHeader());
					writer.println("Algorithm: " + OffloadingSetup.algoName);
					Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> mostFrequent = histogram.max(new FrequencyComparator());
					writer.println(mostFrequent._1().toString() + "\t" + mostFrequent._2()._1() + "\t" + mostFrequent._2()._2() 
						+ "\t" + mostFrequent._2()._3() + "\t" + mostFrequent._2()._4() + "\t" + mostFrequent._2()._5() );
					writer.flush();	
					writer.close();
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		System.out.println(histogram.first());			
		jscontext.close();
	}

	private static String setupOutputFileName(DateFormat dateFormat, Date date, String algoName) {
		return OffloadingSetup.outfile
				+ algoName +"/"
				+ dateFormat.format(date)
				+ "-" + OffloadingSetup.MAP_M
				+ "X"
				+ OffloadingSetup.MAP_N
				+ "-CLOUD=" + OffloadingSetup.cloudNum
				+ "-EDGE=" + OffloadingSetup.edgeNodes
				+ "-" + algoName
				+ ((OffloadingSetup.cloudOnly)? "-ONLYCLOUD": "-eta-" + OffloadingSetup.Eta)
				+ ".data";
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
						
						singleSearch = new HEFTResearch(inputValues);
						
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
														os.getExecutionTime()
														)));
							}
						return output.iterator();
					}
		});
		
		//System.out.println(results.first());
		
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
										(val._5()/OffloadingSetup.iterations)
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
			for(int j = 0; j< OffloadingSetup.mobileNum; j++)
				globalWorkload.joinParallel(generator.setupWorkload(OffloadingSetup.numberOfApps, "mobile_"+j));
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, OffloadingSetup.cloudNum);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			DefaultMobileDevicePlanner.setupMobileDevices(inf,OffloadingSetup.mobileNum);
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

	private static void setupAreaParameters()
	{
		switch(OffloadingSetup.area)
		{
		case "HERNALS":
			OffloadingSetup.MAP_M = 6;
			OffloadingSetup.MAP_N = 6;
			OffloadingSetup.mobilityTraceFile = "traces/hernals.coords";
			OffloadingSetup.x_max = 3119;
			OffloadingSetup.y_max = 3224;
			break;
		case "LEOPOLDSTADT":
			OffloadingSetup.MAP_M = 10;
			OffloadingSetup.MAP_N = 10;
			OffloadingSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
			OffloadingSetup.x_max = 11098;
			OffloadingSetup.y_max = 9099;
			break;
		case "SIMMERING":
			OffloadingSetup.MAP_M = 12;
			OffloadingSetup.MAP_N = 12;
			OffloadingSetup.mobilityTraceFile = "traces/simmering.coords";
			OffloadingSetup.x_max = 6720;
			OffloadingSetup.y_max = 5623;
			break;
		}
	}
	
	private static void processArgs(String[] args) {
		
		for(String s : args)
		{
			if(s.startsWith("-mobile="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.mobileNum = Integer.parseInt(tmp[1]);
				continue;
			}
			if(s.startsWith("-outfile="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.outfile = tmp[1];
				continue;
			}
			if(s.startsWith("-iter="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.iterations = Integer.parseInt(tmp[1]);
				continue;
			}
			
			if(s.startsWith("-cloud="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.cloudNum = Integer.parseInt(tmp[1]);
				continue;
			}
			
			if(s.startsWith("-wl-runs=")){
				String[] tmp = s.split("=");
				String[] input = tmp[1].split(",");
				int[] wlRuns = new int[input.length];
				for(int i = 0; i < input.length; i++)
					wlRuns[i] = Integer.parseInt(input[i]);
				OffloadingSetup.numberOfApps = wlRuns[0];
				continue;
			}
			
			if(s.startsWith("-navigatorMapSize="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.navigatorMapSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-antivirusFileSize="))
			{
				String[] tmp = s.split("=");
				// 1/input, to be used for lambda of exponential distribution
				OffloadingSetup.antivirusFileSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-facerecImageSize="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.facerecImageSize = (Double.parseDouble(tmp[1]) * 1e3);
				continue;
			}
			if(s.startsWith("-chessMi="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.chessMI = (Double.parseDouble(tmp[1]));
				continue;
			}
			
			if(s.startsWith("-eta="))
			{
				String[] tmp = s.split("=");
				OffloadingSetup.Eta = Double.parseDouble(tmp[1]);
				continue;
			}
						
			if(s.equals("-cloudonly"))
				OffloadingSetup.cloudOnly = true;
		}
	}

	private static void printUsageInfo() {
		// TODO Auto-generated method stub
		System.out.println("\n"
				+ "-h, -?\t"
				+ "Prints usage information\n"
				+ "-mobile=n\t"
				+ "Instantiates n mobile devices\n"
				+ "-cloud=n\t"
				+ "Instantiates n cloud nodes\n"
				+ "-wlRuns=n\t"
				+ "Each workflows has n applications\n"
				+ "-cloudonly\t"
				+ "Simulation uses only Cloud nodes\n"
				+ "-area=name\t"
				+ "Urban area where the offloading is performed (possible choices: HERNALS, LEOPOLDSTADT, SIMMERING)\n"
				+ "-eta=n\t"
				+ "Sets the eta parameter, which is necessary to set offloading cost (the higher the eta, the lower the cost).\n"
				+ "-outfile=string\t"
				+ "Saves output in file filename\n"
				+ "-iter=n\t"
				+ "Executes simulation for n iterations\n"
				+ "-navigatorMapSize=#\t"
				+ "Lambda parameter for size of navigator MAP (in kb)\n"
				+ "-antivirusFileSize=#\t"
				+ "Lambda parameter for size of antivirus file (in kb)\n"
				+ "-facerecImageSize=#\t"
				+ "Lambda parameter for size of image file (in kb) for Facerec app\n"
				+ "-chessMi=#\t"
				+ "Lambda parameter for computational size of Chess app \n"
				+ "-navigatorDistr=#\t"
				+ "Probability of NAVIGATOR app in workflow (must be between 0 and 1).\n"
				+ "-antivirusDistr=#\t"
				+ "Probability of ANTIVIRUS app in workflow (must be between 0 and 1).\n"
				+ "-facerecDistr=#\t"
				+ "Probability of FACEREC app in workflow (must be between 0 and 1).\n"
				+ "-chessDistr=#\t"
				+ "Probability of CHESS app in workflow (must be between 0 and 1).\n"
				+ "-facebookDistr=#\t"
				+ "Probability of FACEBOOK app in workflow (must be between 0 and 1).\n"
				+ "\n"
				+ "");
	}
	

}
