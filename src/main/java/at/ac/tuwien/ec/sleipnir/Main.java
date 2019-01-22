package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HeftEchoResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

public class Main {
	
	public static void main(String[] arg)
	{
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> test = generateSamples(SimulationSetup.iterations);
		
		JavaRDD<Tuple2<MobileApplication,MobileCloudInfrastructure>> input = jscontext.parallelize(test);
		
		JavaPairRDD<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<MobileApplication,MobileCloudInfrastructure>, 
				OffloadScheduling, Tuple5<Integer,Double,Double,Double,Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<Tuple2<OffloadScheduling, Tuple5<Integer,Double,Double,Double,Double>>> call(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues)
							throws Exception {
						ArrayList<Tuple2<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>>> output = 
								new ArrayList<Tuple2<OffloadScheduling,Tuple5<Integer,Double,Double,Double,Double>>>();
						HEFTResearch search = new HEFTResearch(inputValues);
						//RandomScheduler search = new RandomScheduler(inputValues);
						ArrayList<OffloadScheduling> offloads = search.findScheduling();
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
		
		System.out.println(histogram.first());
		
		jscontext.close();
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
	

}
