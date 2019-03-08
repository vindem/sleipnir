package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.commons.lang.math.RandomUtils;
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

import at.ac.tuwien.ac.datamodel.DataDistributionGenerator;
import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ac.datamodel.placement.algorithms.RandomDataPlacementAlgorithm;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultIoTPlanner;
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
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HeftEchoResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

public class SC2019Main {
	
	public static void main(String[] arg)
	{
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>> test = generateSamples(SimulationSetup.iterations);
		
		JavaRDD<Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure>> input = jscontext.parallelize(test);
		
		JavaPairRDD<DataPlacement,Tuple2<Integer,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>, 
				DataPlacement, Tuple2<Integer,Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<Tuple2<DataPlacement, Tuple2<Integer,Double>>> call(Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure> inputValues)
							throws Exception {
						ArrayList<Tuple2<DataPlacement,Tuple2<Integer,Double>>> output = 
								new ArrayList<Tuple2<DataPlacement,Tuple2<Integer,Double>>>();
						//HEFTResearch search = new HEFTResearch(inputValues);
						RandomDataPlacementAlgorithm search = new RandomDataPlacementAlgorithm(inputValues);
						ArrayList<DataPlacement> offloads = (ArrayList<DataPlacement>) search.findScheduling();
						if(offloads != null)
							for(DataPlacement dp : offloads) 
							{
								output.add(
										new Tuple2<DataPlacement,Tuple2<Integer,Double>>(dp,
												new Tuple2<Integer,Double>(
														1,
														dp.getAverageLatency()
														)));
							}
						return output.iterator();
					}
		});
		
		System.out.println(results.first());
		
		JavaPairRDD<DataPlacement,Tuple2<Integer,Double>> aggregation = 
				results.reduceByKey(
				new Function2<Tuple2<Integer,Double>,
				Tuple2<Integer,Double>,
				Tuple2<Integer,Double>>()
				{
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<Integer, Double> call(
							Tuple2<Integer, Double> off1,
							Tuple2<Integer, Double> off2) throws Exception {
						// TODO Auto-generated method stub
						return new Tuple2<Integer, Double>(
								off1._1() + off2._1(),
								off1._2() + off2._2()
								);
					}
					
				}
			);
		
		//System.out.println(aggregation.first());
		
		JavaPairRDD<DataPlacement,Tuple2<Integer,Double>> histogram = 
				aggregation.mapToPair(
						new PairFunction<Tuple2<DataPlacement,Tuple2<Integer, Double>>,
						DataPlacement,Tuple2<Integer, Double>>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<DataPlacement, Tuple2<Integer, Double>> call(
									Tuple2<DataPlacement, Tuple2<Integer, Double>> arg0)
									throws Exception {
								Tuple2<Integer, Double> val = arg0._2();
								Tuple2<Integer, Double> tNew 
									= new Tuple2<Integer, Double>
									(
										val._1(),
										val._2()/val._1()
									);
								
								return new Tuple2<DataPlacement,Tuple2<Integer, Double>>(arg0._1,tNew);
							}

							
						}
				
				);
				
		System.out.println(histogram.first());
		
		jscontext.close();
	}

	private static ArrayList<Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>> samples = new ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>>();
		DataDistributionGenerator ddg = new DataDistributionGenerator(SimulationSetup.dataEntryNum);
		for(int i = 0; i < iterations; i++)
		{
			ArrayList<DataEntry> globalWorkload = ddg.getGeneratedData();
			WorkloadGenerator generator = new WorkloadGenerator();
			
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileDataDistributionInfrastructure inf = new MobileDataDistributionInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			DefaultMobileDevicePlanner.setupMobileDevices(inf,SimulationSetup.mobileNum);
			DefaultIoTPlanner.setupIoTNodes(inf, SimulationSetup.iotDevicesNum);
			DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> singleSample = new Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>(globalWorkload,inf);
			samples.add(singleSample);
		}
		return samples;
	}

		//Creates samples for each spark worker
	

}
