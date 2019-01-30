package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.fgcs.WorkflowSchedulingCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.fgcs.WorkflowSchedulingEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.fgcs.WorkflowSchedulingNetworkPlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.HEFTWorkflowScheduler;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.WorkflowScheduler;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;
import at.ac.tuwien.ec.workflow.MontageWorkflow;
import scala.Tuple2;
import scala.Tuple4;

public class FGCSMain {

	public static void main(String[] args) {
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);

		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> test = generateSamples(FGCSSetup.iterations);
		JavaRDD<Tuple2<MobileApplication,MobileCloudInfrastructure>> input = jscontext.parallelize(test);

		JavaPairRDD<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>> results = input.flatMapToPair(new 
				PairFlatMapFunction<Tuple2<MobileApplication,MobileCloudInfrastructure>, 
				WorkflowScheduling, Tuple4<Integer,Double,Double,Double>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<Tuple2<WorkflowScheduling, Tuple4<Integer,Double,Double,Double>>> call(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues)
					throws Exception {
				ArrayList<Tuple2<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>>> output = 
						new ArrayList<Tuple2<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>>>();
				WorkflowScheduler search = new HEFTWorkflowScheduler(inputValues);
				//RandomScheduler search = new RandomScheduler(inputValues);
				ArrayList<WorkflowScheduling> schedulings = (ArrayList<WorkflowScheduling>) search.findScheduling();
				if(schedulings != null)
					for(WorkflowScheduling os : schedulings) 
					{
						output.add(
								new Tuple2<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>>(os,
										new Tuple4<Integer,Double,Double,Double>(
												1,
												os.getRunTime(),
												os.getUserCost(),
												os.getReliability()
												)));
					}
				return output.iterator();
			}
		});

		System.out.println(results.first());

		JavaPairRDD<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>> aggregation = 
				results.reduceByKey(
						new Function2<Tuple4<Integer,Double,Double,Double>,
						Tuple4<Integer,Double,Double,Double>,
						Tuple4<Integer,Double,Double,Double>>()
						{
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							public Tuple4<Integer, Double, Double, Double> call(
									Tuple4<Integer, Double, Double, Double> off1,
									Tuple4<Integer, Double, Double, Double> off2) throws Exception {
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

		JavaPairRDD<WorkflowScheduling,Tuple4<Integer,Double,Double,Double>> histogram = 
				aggregation.mapToPair(
						new PairFunction<Tuple2<WorkflowScheduling,Tuple4<Integer, Double, Double, Double>>,
						WorkflowScheduling,Tuple4<Integer, Double, Double, Double>>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<WorkflowScheduling, Tuple4<Integer, Double, Double, Double>> call(
									Tuple2<WorkflowScheduling, Tuple4<Integer, Double, Double, Double>> arg0)
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

								return new Tuple2<WorkflowScheduling,Tuple4<Integer, Double, Double, Double>>(arg0._1,tNew);
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
			
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			MobileApplication app = new MontageWorkflow();
			MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
			WorkflowSchedulingCloudPlanner.setupCloudNodes(inf, FGCSSetup.cloudNum);
			WorkflowSchedulingEdgePlanner.setupEdgeNodes(inf);
			WorkflowSchedulingNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<MobileApplication,MobileCloudInfrastructure> singleSample = new Tuple2<MobileApplication,MobileCloudInfrastructure>(app,inf);
			samples.add(singleSample);
		}
		return samples;
	}
	
}
