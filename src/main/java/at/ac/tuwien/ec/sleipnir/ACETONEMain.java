package at.ac.tuwien.ec.sleipnir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.provisioning.DefaultIoTPlanner;
import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.provisioning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.workflow.faas.FaaSTestWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import at.ac.tuwien.ec.workflow.faas.OFWorkflow;
import at.ac.tuwien.ec.workflow.faas.IRWorkflow;
import at.ac.tuwien.ec.workflow.faas.IntrasafedWorkflow;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSCostlessPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.DealFWPPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.DealJSPPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.DealRandomPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FFDPRODPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSPlacementAlgorithm;
import at.ac.tuwien.ec.workflow.faas.placement.PEFTFaaSScheduler;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;


public class ACETONEMain {

	public static void main(String[] arg) throws IOException
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
			SimulationSetup.mobileNum = 360;
			SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
			SimulationSetup.x_max = 3119;
			SimulationSetup.y_max = 3224;
			break;
		case "LEOPOLDSTADT":
			SimulationSetup.MAP_M = 10;
			SimulationSetup.MAP_N = 10;
			SimulationSetup.iotDevicesNum = 100;
			SimulationSetup.mobileNum = 1000;
			SimulationSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
			SimulationSetup.x_max = 11098;
			SimulationSetup.y_max = 9099;
			break;
		case "SIMMERING":
			SimulationSetup.MAP_M = 12;
			SimulationSetup.MAP_N = 12;
			SimulationSetup.iotDevicesNum = 144;
			SimulationSetup.mobileNum = 1440;
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
		/*
		BufferedWriter writer = new BufferedWriter(new FileWriter(SimulationSetup.filename, true));
		try {

			writer.append("AREA:\t" + SimulationSetup.area+"\n");
			writer.append("IoT-NUM:\t" + SimulationSetup.iotDevicesNum+"\n");
			writer.append("MOBILE-NUM:\t" + SimulationSetup.mobileNum+"\n");
			writer.append("DATA-RATE:\t" + SimulationSetup.dataRate+"\n");
			writer.append("WORKLOAD:\t" + SimulationSetup.workloadType+"\n");
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/			

		for(String currAlgorithm : SimulationSetup.algorithms)
		{

			JavaPairRDD<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>> results = input.flatMapToPair(new 
					PairFlatMapFunction<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>, 
					FaaSWorkflowPlacement, Tuple4<Integer,Double, Double ,Double>>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Iterator<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer,Double, Double,Double>>> call(Tuple2<FaaSWorkflow, MobileDataDistributionInfrastructure> inputValues)
						throws Exception {
					ArrayList<Tuple2<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>>> output = 
							new ArrayList<Tuple2<FaaSWorkflowPlacement,Tuple4<Integer,Double, Double,Double>>>();
					//HEFTResearch search = new HEFTResearch(inputValues);

					FaaSPlacementAlgorithm search = null;
					//switch(SimulationSetup.placementAlgorithm)
					switch(currAlgorithm)
					{
					case "PEFT":
						search = new PEFTFaaSScheduler(inputValues);
						break;
					case "DEAL-JSP":
						search = new DealJSPPlacement(inputValues);
						break;
					case "DEAL-FW":
						search = new DealFWPPlacement(inputValues);
						break;
					case "DEAL-RND":
						search = new DealRandomPlacement(inputValues);
						break;
					case "FFD":
						search = new FFDPRODPlacement(inputValues);
						break;
					case "COSTLESS":
						search = new FaaSCostlessPlacement(inputValues);
						break;
					default:
						search = new PEFTFaaSScheduler(inputValues);
					}

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

						
			
			//Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> mostFrequent = histogram.max(new FrequencyComparator());
			//System.out.println("Most frequent:");
			//System.out.println(mostFrequent._1());
			//System.out.println(mostFrequent._2());
			
			/*
			try {
				writer.append("Most frequent:");
				writer.append(mostFrequent._1() + "\n" +
						mostFrequent._2()._1() + "\t \t \t" + mostFrequent._2()._2() + "\t \t \t" + mostFrequent._2()._3() + "\t \t \t" + mostFrequent._2()._4() + "\n" );
				writer.append("Results:\n");
			}
			catch(IOException e){
				e.printStackTrace();
			}*/

			
			/*
			histogram.foreach((VoidFunction<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>>>) tuple ->
			writer.append(
					tuple._1()+"\n"
					+tuple._2()._1()+"\t"
					+tuple._2()._2()+"\t \t \t"
					+tuple._2()._3()+"\t \t \t"
					+tuple._2()._4()+"\t \t \n"));
			*/
			
			histogram.saveAsTextFile(SimulationSetup.filename);;
			//histogram.foreach((VoidFunction<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>>>) tuple ->
			//placements.add(tuple));

			/*
			if(!solutions.isEmpty()) {
				for(String solution : solutions)
				{
					//Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> solution = solutions.next();
					System.out.println("AAAAAAAAAAAA!");					
					try {
						writer.append(solution);
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			System.out.println("");

			System.out.println("##############");
			try {
				writer.append("");
				writer.append("##################\n");
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}*/
		}
		//System.out.println(mostFrequent._1.values().size());

		//writer.close();
		jscontext.close();
	}

	private static Tuple3<Tuple2<Double, Double>, Tuple2<Double, Double>, Tuple2<Double, Double>> calculateCI(
			Tuple4<Integer, Double, Double, Double> means, Tuple3<Double, Double, Double> stdDeviations, double confidence) {
		Tuple2<Double,Double> latCI;
		Tuple2<Double,Double> exeCI;
		Tuple2<Double,Double> cstCI;
		
		latCI = new Tuple2<Double,Double>(means._2() - confidence*stdDeviations._1()/means._1(),
				means._2() + confidence*stdDeviations._1()/means._1());
		exeCI = new Tuple2<Double,Double>(means._3() - confidence*stdDeviations._2()/means._1(),
				means._3() + confidence*stdDeviations._2()/means._1());
		cstCI = new Tuple2<Double,Double>(means._4() - confidence*stdDeviations._3()/means._1(),
				means._4() + confidence*stdDeviations._3()/means._1());
		return new Tuple3<Tuple2<Double,Double>,Tuple2<Double,Double>,Tuple2<Double,Double>>(latCI, exeCI, cstCI);
	}

	private static Tuple3<Double,Double,Double> calculateStandardDeviation(FaaSWorkflowPlacement key,
			Iterator<Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>>> placements,
			Tuple4<Integer,Double,Double,Double> means) {
		
		double latencyStd = 0.0, executionTimeStd = 0.0, costStd = 0.0;
		
		while(placements.hasNext())
		{
			Tuple2<FaaSWorkflowPlacement, Tuple4<Integer, Double, Double, Double>> placement = placements.next();
			
			if(placement._1().equals(key))
			{
				latencyStd += Math.pow(placement._2()._2() - means._2(),2.0);
				executionTimeStd += Math.pow(placement._2()._3() - means._3(),2.0);
				costStd += Math.pow(placement._2()._4() - means._4(),2.0);
			}
		}
		
		latencyStd = latencyStd / ((double) means._1());
		executionTimeStd = executionTimeStd / ((double)means._1());
		costStd = costStd / ((double)means._1());
		
		return new Tuple3<Double,Double,Double>(latencyStd, executionTimeStd, costStd);
	}

	private static ArrayList<Tuple2<FaaSWorkflow, MobileDataDistributionInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>> samples = new ArrayList<Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure>>();
		//DataDistributionGenerator ddg = new DataDistributionGenerator(SimulationSetup.dataEntryNum);
		String[] workflowSub = {"moisture"};
		String[] workflowPub = {"moisture"};
		FaaSWorkflow faasWorkflow = new FaaSWorkflow(workflowPub, workflowSub);
		//System.out.println(faasWorkflow.getComponentNum());
		FaaSWorkflow faasWorkflowNew = null;
		for(int i = 0; i < SimulationSetup.numberOfApps; i++)
		{
			switch(SimulationSetup.selectedWorkflow)
			{
				case "IR":
					faasWorkflowNew = new IRWorkflow(i,workflowPub, workflowSub);
					break;
				case "OF":
					faasWorkflowNew = new OFWorkflow(i,workflowPub, workflowSub);
					break;
				case "IntraSafed":
					faasWorkflowNew = new IntrasafedWorkflow(i,workflowPub, workflowSub);
					break;
			}
			
			faasWorkflow.joinSequential(faasWorkflowNew);
		}
		System.out.println(faasWorkflow.getComponentNum());
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
			MobilityBasedNetworkPlanner.setupNetworkConnections(inf);
			MobilityBasedNetworkPlanner.setupMobileConnections(inf);
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
			if(arg.startsWith("-dataSize="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.dataMultiplier = Double.valueOf(pars[1]);
			}
			if(arg.startsWith("-placement="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.algorithms = pars[1].split(",");
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
					SimulationSetup.mobileNum = 360;
					SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
					SimulationSetup.x_max = 3119;
					SimulationSetup.y_max = 3224;
					break;
				case "LEOPOLDSTADT":
					SimulationSetup.MAP_M = 10;
					SimulationSetup.MAP_N = 10;
					SimulationSetup.iotDevicesNum = 100;
					SimulationSetup.mobileNum = 1000;
					SimulationSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
					SimulationSetup.x_max = 11098;
					SimulationSetup.y_max = 9099;
					break;
				case "SIMMERING":
					SimulationSetup.MAP_M = 12;
					SimulationSetup.MAP_N = 12;
					SimulationSetup.iotDevicesNum = 144;
					SimulationSetup.mobileNum = 1440;
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
			if(arg.startsWith("-radius="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.nCenters = Integer.parseInt(pars[1]);
			}
			if(arg.startsWith("-updateTime=") || arg.startsWith("-uT"))
			{
				String[] pars = arg.split("=");
				SimulationSetup.updateTime = Double.parseDouble(pars[1]);
			}
			if(arg.startsWith("-workflow="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.selectedWorkflow = pars[1];
			}
			if(arg.startsWith("-nApps="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.numberOfApps = Integer.parseInt(pars[1]);
			}
		}
	}
	
		

}
