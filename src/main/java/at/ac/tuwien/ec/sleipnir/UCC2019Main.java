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
import org.apache.spark.scheduler.AllJobsCancelled;

import at.ac.tuwien.ec.datamodel.DataDistributionGenerator;
import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.placement.DataPlacementAlgorithm;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDCPUPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDPRODPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.L2NormPlacement;
import at.ac.tuwien.ec.datamodel.algorithms.placement.SteinerTreeHeuristic;
import at.ac.tuwien.ec.datamodel.algorithms.selection.BestFitCPU;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUDecreasing;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUIncreasing;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitDecreasingSizeVMPlanner;
import at.ac.tuwien.ec.datamodel.algorithms.selection.VMPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileBlockchainInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultIoTPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.scheduling.offloading.pos.PoSNearestNodeBroker;
import at.ac.tuwien.ec.scheduling.offloading.pos.PoSOffloadingAlgorithm;
import at.ac.tuwien.ec.scheduling.offloading.pos.ValidationOffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.pos.Z3PoSBroker;
import at.ac.tuwien.ec.scheduling.utils.blockchain.TransactionPool;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

public class UCC2019Main {
	
	
	
	public static void main(String[] arg)
	{
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);

		class FrequencyComparator implements Comparator<Tuple2<ValidationOffloadScheduling, Tuple5<Integer,Double,Double,Double,Double>>>, Serializable
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			@Override
			public int compare(Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double,Double, Double>> o1,
					Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double,Double, Double>> o2) {
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
			//SimulationSetup.mobileNum = 24;
			SimulationSetup.mobileNum = 12;
			SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
			SimulationSetup.x_max = 3119;
			SimulationSetup.y_max = 3224;
			break;
		case "LEOPOLDSTADT":
			SimulationSetup.MAP_M = 10;
			SimulationSetup.MAP_N = 10;
			SimulationSetup.iotDevicesNum = 100;
			//SimulationSetup.mobileNum = 40;
			SimulationSetup.mobileNum = 20;
			SimulationSetup.mobilityTraceFile = "traces/leopoldstadt.coords";
			SimulationSetup.x_max = 11098;
			SimulationSetup.y_max = 9099;
			break;
		case "SIMMERING":
			SimulationSetup.MAP_M = 12;
			SimulationSetup.MAP_N = 12;
			SimulationSetup.iotDevicesNum = 144;
			//SimulationSetup.mobileNum = 48;
			SimulationSetup.mobileNum = 24;
			SimulationSetup.mobilityTraceFile = "traces/simmering.coords";
			SimulationSetup.x_max = 6720;
			SimulationSetup.y_max = 5623;
			break;
		}

		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		Z3PoSBroker.loadLibrary();
		ArrayList<Tuple2<TransactionPool,MobileBlockchainInfrastructure>> test = generateSamples(SimulationSetup.iterations);

		JavaRDD<Tuple2<TransactionPool, MobileBlockchainInfrastructure>> input = jscontext.parallelize(test);
		
		JavaPairRDD<ValidationOffloadScheduling,Tuple5<Integer,Double, Double, Double,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<TransactionPool,MobileBlockchainInfrastructure>, 
				ValidationOffloadScheduling, Tuple5<Integer,Double, Double, Double,Double>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double, Double,Double>>> call(Tuple2<TransactionPool, MobileBlockchainInfrastructure> inputValues)
					throws Exception {
				ArrayList<Tuple2<ValidationOffloadScheduling,Tuple5<Integer,Double,Double, Double,Double>>> output = 
						new ArrayList<Tuple2<ValidationOffloadScheduling,Tuple5<Integer,Double, Double, Double,Double>>>();
				//HEFTResearch search = new HEFTResearch(inputValues);
				PoSOffloadingAlgorithm search = new Z3PoSBroker(inputValues);
				//PoSOffloadingAlgorithm search = new PoSNearestNodeBroker(inputValues);
				//RandomDataPlacementAlgorithm search = new RandomDataPlacementAlgorithm(new FirstFitDecreasingSizeVMPlanner(),inputValues);
				//SteinerTreeHeuristic search = new SteinerTreeHeuristic(currentPlanner, inputValues);
				ArrayList<ValidationOffloadScheduling> offloads = (ArrayList<ValidationOffloadScheduling>) search.findScheduling();
				if(offloads != null)
					for(ValidationOffloadScheduling dp : offloads) 
					{
						if(dp!=null)
							output.add(
									new Tuple2<ValidationOffloadScheduling,Tuple5<Integer,Double, Double, Double, Double>>(dp,
											new Tuple5<Integer,Double, Double,Double,Double>(
													1,
													dp.getRunTime(),
													dp.getUserCost(),
													dp.getBatteryLifetime(),
													dp.getSimTime()
											)));
					}
				return output.iterator();
			}
		});

		System.out.println(results.first());

		JavaPairRDD<ValidationOffloadScheduling,Tuple5<Integer,Double, Double, Double, Double>> aggregation = 
				results.reduceByKey(
						new Function2<Tuple5<Integer,Double, Double,Double,Double>,
						Tuple5<Integer,Double,Double,Double,Double>,
						Tuple5<Integer,Double,Double,Double,Double>>()
						{
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							public Tuple5<Integer, Double, Double, Double, Double> call(
									Tuple5<Integer, Double,Double, Double, Double> off1,
									Tuple5<Integer, Double,Double, Double, Double> off2) throws Exception {
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

		System.out.println(aggregation.first());

		JavaPairRDD<ValidationOffloadScheduling,Tuple5<Integer,Double, Double, Double, Double>> histogram = 
				aggregation.mapToPair(
						new PairFunction<Tuple2<ValidationOffloadScheduling,Tuple5<Integer, Double, Double, Double, Double>>,
						ValidationOffloadScheduling,Tuple5<Integer, Double, Double, Double,Double>>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> call(
									Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> arg0)
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

								return new Tuple2<ValidationOffloadScheduling,Tuple5<Integer, Double, Double, Double, Double>>(arg0._1,tNew);
							}


						}

						);

		


		
		JavaPairRDD<ValidationOffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>> histogramF = histogram.filter((t -> ( !Double.isNaN(t._2()._2()) && !Double.isNaN(t._2()._3()) 
				&& !Double.isNaN(t._2()._4()) && !Double.isNaN(t._2()._5()) )));
		Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> mostFrequent = histogramF.max(new FrequencyComparator());
		System.out.println(mostFrequent._2());
		double runtimes[], profits[], energies[], simtimes[];
		runtimes = new double[histogramF.collect().size()];
		profits = new double[histogramF.collect().size()];
		energies = new double[histogramF.collect().size()];
		simtimes = new double[histogramF.collect().size()];
		double runtimeAvg = 0, profitAvg = 0, energyAvg = 0, simtimeAvg = 0;
		int i = 0, nSamples = 0;
		for(Tuple2<ValidationOffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> t : histogramF.collect())
		{
			if(	!validNumber(t._2()._2()) && !validNumber(t._2()._3()) && !validNumber(t._2()._4()) && !validNumber(t._2()._5()))
				continue;
			
			nSamples++;
			runtimeAvg += t._2()._2();
			profitAvg += t._2()._3();
			energyAvg += t._2()._4();
			simtimeAvg += t._2()._5();
			
			runtimes[i] = t._2()._2();
			profits[i] = t._2()._3();
			energies[i] = t._2()._4();
			simtimes[i] = t._2()._5();		
		}
		
		runtimeAvg = runtimeAvg/nSamples;
		profitAvg = profitAvg/nSamples;
		energyAvg = energyAvg/nSamples;
		simtimeAvg = simtimeAvg/nSamples;
		
		double varRt = 0.0, varProfit = 0.0, varEnergy = 0.0, varSim = 0.0;
		
		for(i = 0; i < nSamples; i++)
		{
			varRt += (runtimes[i] - runtimeAvg) * (runtimes[i] - runtimeAvg);
			varProfit += (profits[i] - profitAvg) * (profits[i] - profitAvg);
			varEnergy += (energies[i] - energyAvg) * (energies[i] - energyAvg);
			varSim += (simtimes[i] - simtimeAvg) * (simtimes[i] - simtimeAvg);
		}

		varRt = varRt / (nSamples - 1);
		varProfit = varProfit / (nSamples - 1);
		varEnergy = varEnergy / (nSamples - 1);
		varSim = varSim / (nSamples - 1);
		
		System.out.println("RT: ["+(runtimeAvg - (1.96 * Math.sqrt(varRt)))+","+(runtimeAvg + 1.96 * Math.sqrt(varRt))+"]");
		System.out.println("PR: ["+(profitAvg - (1.96 * Math.sqrt(varProfit)))+","+(profitAvg + 1.96 * Math.sqrt(varProfit))+"]");
		System.out.println("EC: ["+(energyAvg - (1.96 * Math.sqrt(varEnergy)))+","+(energyAvg + 1.96 * Math.sqrt(varEnergy))+"]");
		System.out.println("ST: ["+(simtimeAvg - (1.96 * Math.sqrt(varSim)))+","+(simtimeAvg + 1.96 * Math.sqrt(varSim))+"]");
		jscontext.close();
	}

	private static boolean validNumber(Double num) {
		return Double.isFinite(num) && !Double.isNaN(num);
	}

	private static ArrayList<Tuple2<TransactionPool, MobileBlockchainInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<TransactionPool,MobileBlockchainInfrastructure>> samples = new ArrayList<Tuple2<TransactionPool,MobileBlockchainInfrastructure>>();
		TransactionPool pool = new TransactionPool();
		pool.generateTransactions();
		for(int i = 0; i < iterations; i++)
		{
			//ArrayList<DataEntry> globalWorkload = ddg.getGeneratedData();
			//WorkloadGenerator generator = new WorkloadGenerator();
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileBlockchainInfrastructure inf = new MobileBlockchainInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
			//RandomEdgePlanner.setupEdgeNodes(inf);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			//MobileDevicePlannerWithMobility.setupMobileDevices(inf,SimulationSetup.mobileNum);
			//DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<TransactionPool,MobileBlockchainInfrastructure> singleSample = new Tuple2<TransactionPool,MobileBlockchainInfrastructure>(pool,inf);
			samples.add(singleSample);
		}
		return samples;
	}

	private static void processArgs(String[] args)
	{
		for(String arg: args)
		{
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
					//SimulationSetup.iotDevicesNum = 36;
					SimulationSetup.mobileNum = 24;
					SimulationSetup.mobilityTraceFile = "traces/hernals.coords";
					SimulationSetup.x_max = 3119;
					SimulationSetup.y_max = 3224;
					break;
				case "LEOPOLDSTADT":
					SimulationSetup.MAP_M = 10;
					SimulationSetup.MAP_N = 10;
					//SimulationSetup.iotDevicesNum = 100;
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
		}
	}
	
		

}
