package at.ac.tuwien.ec.sleipnir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import at.ac.tuwien.ec.datamodel.DataDistributionGenerator;
import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.placement.DataPlacementAlgorithm;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDCPUEdgePlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDCPUPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDPRODPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.L2NormPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.SteinerTreeHeuristic;
import at.ac.tuwien.ec.datamodel.algorithms.selection.BestFitCPU;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUDecreasing;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUIncreasing;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitDecreasingSizeContainerPlanner;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultIoTPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.workflow.faas.FaaSTestWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSCostlessPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSDistancePlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSPlacementAlgorithm;
import scala.Tuple2;
import scala.Tuple4;


public class ACETONEMain {
	
	public static void main(String[] arg)
	{
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		
		class FrequencyComparator implements Comparator<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer,Double,Double,Double>>>, Serializable
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			@Override
			public int compare(Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double,Double>> o1,
					Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double,Double>> o2) {
				// TODO Auto-generated method stub
				return o1._2()._1() - o2._2()._1();
			}
			
		}
				
		processArgs(arg);
		switch(SimulationSetup.area)
		{
		case "HERNALS":
			SimulationSetup.MAP_M = 6;
			SimulationSetup.MAP_N = 6;
			SimulationSetup.iotDevicesNum = 36;
			SimulationSetup.mobileNum = 24;
			SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
			SimulationSetup.x_max = 3119;
			SimulationSetup.y_max = 3224;
			break;
		case "LEOPOLDSTADT":
			SimulationSetup.MAP_M = 10;
			SimulationSetup.MAP_N = 10;
			SimulationSetup.iotDevicesNum = 100;
			SimulationSetup.mobileNum = 40;
			SimulationSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
			SimulationSetup.x_max = 11098;
			SimulationSetup.y_max = 9099;
			break;
		case "SIMMERING":
			SimulationSetup.MAP_M = 12;
			SimulationSetup.MAP_N = 12;
			SimulationSetup.iotDevicesNum = 144;
			SimulationSetup.mobileNum = 48;
			SimulationSetup.mobilityTraceFile = "traces/simmering.coords";
			SimulationSetup.x_max = 6720;
			SimulationSetup.y_max = 5623;
			break;
		}
		SimulationSetup.dataEntryNum = (int) (SimulationSetup.iotDevicesNum * SimulationSetup.mobileNum * SimulationSetup.dataRate);
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>> test = generateSamples(SimulationSetup.iterations);
		
		JavaRDD<Tuple2<FaaSWorkflow, MobileDataDistributionInfrastructure>> input = jscontext.parallelize(test);
				
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(SimulationSetup.filename, true));
			writer.append("AREA:\t" + SimulationSetup.area+"\n");
			writer.append("IoT-NUM:\t" + SimulationSetup.iotDevicesNum+"\n");
			writer.append("MOBILE-NUM:\t" + SimulationSetup.mobileNum+"\n");
			writer.append("DATA-RATE:\t" + SimulationSetup.dataRate+"\n");
			writer.append("WORKLOAD:\t" + SimulationSetup.workloadType+"\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
		
		
			JavaPairRDD<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>> results = input.flatMapToPair(new 
					PairFlatMapFunction<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>, 
					FaaSWorkflowPlacement, Tuple4<Integer,Double, Double,Double>>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Iterator<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer,Double, Double,Double>>> call(Tuple2<FaaSWorkflow, MobileDataDistributionInfrastructure> inputValues)
						throws Exception {
					ArrayList<Tuple2<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>>> output = 
							new ArrayList<Tuple2<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>>>();
					//HEFTResearch search = new HEFTResearch(inputValues);
					FaaSPlacementAlgorithm search;
					switch(SimulationSetup.placementAlgorithm)
					{
					
					default:
						//search = new FFDCPUEdgePlacement(inputValues);
						//search = new FFDCPUPlacement(inputValues);
						search = new FaaSDistancePlacement(inputValues);
						//search = new FaaSCostlessPlacement(inputValues);
						//search = new FFDPRODPlacement(inputValues);
					}
					//RandomDataPlacementAlgorithm search = new RandomDataPlacementAlgorithm(new FirstFitDecreasingSizeVMPlanner(),inputValues);
					//SteinerTreeHeuristic search = new SteinerTreeHeuristic(currentPlanner, inputValues);
					ArrayList<FaaSWorkflowPlacement> offloads = (ArrayList<FaaSWorkflowPlacement>) search.findScheduling();
					if(offloads != null)
						for(FaaSWorkflowPlacement dp : offloads) 
						{
							if(dp!=null)
								output.add(
										new Tuple2<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double, Double>>(dp,
												new Tuple4<Integer,Double, Double,Double>(
														1,
														dp.getAverageLatency(),
														dp.getExecutionTime(),
														dp.getCost()
														)));
						}
					return output.iterator();
				}
			});

			//System.out.println(results.first());

			JavaPairRDD<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double, Double>> aggregation = 
					results.reduceByKey(
							new Function2<Tuple4<Integer,Double, Double,Double>,
							Tuple4<Integer,Double,Double,Double>,
							Tuple4<Integer,Double,Double,Double>>()
							{
								/**
								 * 
								 */
								private static final long serialVersionUID = 1L;

								@Override
								public Tuple4<Integer, Double, Double, Double> call(
										Tuple4<Integer, Double,Double, Double> off1,
										Tuple4<Integer, Double,Double, Double> off2) throws Exception {
									// TODO Auto-generated method stub
									return new Tuple4<Integer, Double, Double, Double>(
											off1._1() + off2._1(),
											off1._2() + off2._2(),
											off1._3() + off2._3(),
											off1._4() + off2._4()
											);
								}

							}
							);

			//System.out.println(aggregation.first());

			JavaPairRDD<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double, Double>> histogram = 
					aggregation.mapToPair(
							new PairFunction<Tuple2<FaaSWorkflowPlacement,Tuple4<Integer, Double, Double, Double>>,
							FaaSWorkflowPlacement,Tuple4<Integer, Double, Double, Double>>()
							{

								private static final long serialVersionUID = 1L;

								@Override
								public Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> call(
										Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> arg0)
												throws Exception {
									Tuple4<Integer, Double, Double, Double> val = arg0._2();
									Tuple4<Integer, Double, Double, Double> tNew 
									= new Tuple4<Integer, Double, Double, Double>
									(
											val._1(),
											val._2()/val._1(),
											val._3()/val._1(),
											val._4()/val._1()
											);

									return new Tuple2<FaaSWorkflowPlacement,Tuple4<Integer, Double, Double, Double>>(arg0._1,tNew);
								}


							}

							);
			
			Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> mostFrequent = histogram.max(new FrequencyComparator());
			//try {
				// writer.append("\n");
				// writer.append("VM PLACEMENT: " + SimulationSetup.placementAlgorithm+"\n");
				 //writer.append("#\tFREQUENCY\tAVERAGE-RT\tMAX-RT\tCOST\tENTRY-NUM\n");
				 //writer.append(" \t"+mostFrequent._2()._1()+"\t"+mostFrequent._2()._2()+"\t"
					//	 +mostFrequent._2()._3()+"\t"+mostFrequent._2()._4() + "\t"+mostFrequent._1.values().size()+"\n");
			//} catch (IOException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			
			System.out.println(mostFrequent._1());
			System.out.println(mostFrequent._2());
			//System.out.println(mostFrequent._1.values().size());
		
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jscontext.close();
	}

	private static ArrayList<Tuple2<FaaSWorkflow, MobileDataDistributionInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>> samples = new ArrayList<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>>();
		DataDistributionGenerator ddg = new DataDistributionGenerator(SimulationSetup.dataEntryNum);
		String[] workflowSub = {"moisture"};
		String[] workflowPub = {"moisture"};
		FaaSWorkflow faasWorkflow = new FaaSTestWorkflow(0,workflowPub, workflowSub);
		for(int i = 1; i < SimulationSetup.numberOfApps; i++)
		{
			FaaSWorkflow faasWorkflowNew = new FaaSTestWorkflow(i,workflowPub, workflowSub);
			faasWorkflow.joinParallel(faasWorkflowNew);
		}
		
		for(int i = 0; i < iterations; i++)
		{
			//ArrayList<DataEntry> globalWorkload = ddg.getGeneratedData();
			//WorkloadGenerator generator = new WorkloadGenerator();
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileDataDistributionInfrastructure inf = new MobileDataDistributionInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
			//RandomEdgePlanner.setupEdgeNodes(inf);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			DefaultIoTPlanner.setupIoTNodes(inf, SimulationSetup.iotDevicesNum);
			MobileDevicePlannerWithMobility.setupMobileDevices(inf,SimulationSetup.mobileNum);
			DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> singleSample = 
					new Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>(faasWorkflow,inf);
			samples.add(singleSample);
		}
		return samples;
	}

	private static void processArgs(String[] args)
	{
		for(String arg: args)
		{
			if(arg.startsWith("-placement="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.placementAlgorithm = pars[1];
			}
			if(arg.startsWith("-filename="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.filename = pars[1];
			}
			if(arg.startsWith("-area="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.area = pars[1];
				switch(pars[1])
				{
				case "HERNALS":
					SimulationSetup.MAP_M = 6;
					SimulationSetup.MAP_N = 6;
					SimulationSetup.iotDevicesNum = 36;
					SimulationSetup.mobileNum = 24;
					SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
					SimulationSetup.x_max = 3119;
					SimulationSetup.y_max = 3224;
					break;
				case "LEOPOLDSTADT":
					SimulationSetup.MAP_M = 10;
					SimulationSetup.MAP_N = 10;
					SimulationSetup.iotDevicesNum = 100;
					SimulationSetup.mobileNum = 40;
					SimulationSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
					SimulationSetup.x_max = 11098;
					SimulationSetup.y_max = 9099;
					break;
				case "SIMMERING":
					SimulationSetup.MAP_M = 12;
					SimulationSetup.MAP_N = 12;
					SimulationSetup.iotDevicesNum = 144;
					SimulationSetup.mobileNum = 48;
					SimulationSetup.mobilityTraceFile = "traces/simmering.coords";
					SimulationSetup.x_max = 6720;
					SimulationSetup.y_max = 5623;
					break;
				}
			}
			if(arg.startsWith("-traffic="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.traffic = pars[1];
			}
			if(arg.startsWith("-workload="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.workloadType = pars[1];
			}
			if(arg.startsWith("-iter="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.iterations = Integer.parseInt(pars[1]);
			}
			if(arg.startsWith("-dr="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.dataRate = Double.parseDouble(pars[1]);
			}
				
		}
	}
	
		

}
