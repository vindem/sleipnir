package at.ac.tuwien.ec.sleipnir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EntryPoint;
import at.ac.tuwien.ec.model.infrastructure.provisioning.workflow.WorkflowSchedulingCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.workflow.WorkflowSchedulingEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.workflow.WorkflowSchedulingFixedNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.workflow.WorkflowSchedulingNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.workflow.WorkflowSchedulingTerminalsPlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.HEFTMaxReliability;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.HEFTMinCostScheduler;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.HEFTWorkflowScheduler;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.PEFTWorkflowScheduler;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.SchedulingTester;
import at.ac.tuwien.ec.scheduling.workflow.algorithms.WorkflowScheduler;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;
import at.ac.tuwien.ec.workflow.BWAWorkflow;
import at.ac.tuwien.ec.workflow.EpigenomicsWorkflow;
import at.ac.tuwien.ec.workflow.MeteoAGWorkflow;
import at.ac.tuwien.ec.workflow.MontageWorkflow;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class FGCSMain {
	
	public static void main(String[] args) {
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);

		class FrequencyComparator implements Comparator<Tuple2<WorkflowScheduling, Tuple5<Integer,Double,Double,Double,Double>>>, Serializable
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			@Override
			public int compare(Tuple2<WorkflowScheduling, Tuple5<Integer, Double, Double, Double,Double>> o1,
					Tuple2<WorkflowScheduling, Tuple5<Integer, Double, Double, Double,Double>> o2) {
				// TODO Auto-generated method stub
				return o2._2()._1() - o1._2()._1();
			}
			
		}
		
		double[] mipsConf = {1000,5000,10000,20000};
		//double[] mipsConf = {1000,2500,4000,5000};
		double[] inOutDataConf = {62500,125000,250000,500000};


		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);

		String[][] outRT = new String[mipsConf.length + 1][inOutDataConf.length + 1];
		String[][] outCost = new String[mipsConf.length + 1][inOutDataConf.length + 1];
		String[][] outRel = new String[mipsConf.length + 1][inOutDataConf.length + 1];
		
		outRT[0][0] = "\t";
		outCost[0][0] = "\t";
		outRel[0][0] = "\t";
		
		for(int i = 1; i < mipsConf.length + 1; i++)
		{
			outRT[i][0] = Double.toString(mipsConf[i-1]);
			outCost[i][0] = Double.toString(mipsConf[i-1]);
			outRel[i][0] = Double.toString(mipsConf[i-1]);
		}
		
		for(int i = 1; i < inOutDataConf.length + 1; i++ ) 
		{
			outRT[0][i] = Double.toString(inOutDataConf[i-1]);
			outCost[0][i] = Double.toString(inOutDataConf[i-1]);
			outRel[0][i] = Double.toString(inOutDataConf[i-1]);
		}
		

		for(int i = 0; i < mipsConf.length; i++)
			for(int j = 0; j < inOutDataConf.length; j++)
			{
				FGCSSetup.workflowMips = mipsConf[i];
				FGCSSetup.workflowIndata = inOutDataConf[j];
				FGCSSetup.workflowOutData = inOutDataConf[j];
				ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> test = generateSamples(FGCSSetup.iterations);
				JavaRDD<Tuple2<MobileApplication,MobileCloudInfrastructure>> input = jscontext.parallelize(test);

				JavaPairRDD<WorkflowScheduling,Tuple5<Integer,Double,Double,Double,Double>> results = input.flatMapToPair(new 
						PairFlatMapFunction<Tuple2<MobileApplication,MobileCloudInfrastructure>, 
						WorkflowScheduling, Tuple5<Integer,Double,Double,Double, Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<Tuple2<WorkflowScheduling, Tuple5<Integer,Double,Double,Double,Double>>> call(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues)
							throws Exception {
						ArrayList<Tuple2<WorkflowScheduling,Tuple5<Integer,Double,Double,Double,Double>>> output = 
								new ArrayList<Tuple2<WorkflowScheduling,Tuple5<Integer,Double,Double,Double, Double>>>();
						WorkflowScheduler search = new HEFTWorkflowScheduler(inputValues);
						//WorkflowScheduler search = new SchedulingTester(inputValues);
						search.setEntryNode((ComputationalNode) inputValues._2.getNodeById("entry0"));
						//RandomScheduler search = new RandomScheduler(inputValues);
						ArrayList<WorkflowScheduling> schedulings = (ArrayList<WorkflowScheduling>) search.findScheduling();
						if(schedulings != null)
							for(WorkflowScheduling os : schedulings) 
							{
								output.add(
										new Tuple2<WorkflowScheduling,Tuple5<Integer,Double,Double,Double,Double>>(os,
												new Tuple5<Integer,Double,Double,Double,Double>(
														1,
														os.getRunTime(),
														os.getUserCost(),
														os.getReliability(),
														os.getWallClock()
														)));
							}
						return output.iterator();
					}
				});

				System.out.println(results.first());

				JavaPairRDD<WorkflowScheduling,Tuple5<Integer,Double,Double,Double,Double>> aggregation = 
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
									public Tuple5<Integer, Double, Double, Double,Double> call(
											Tuple5<Integer, Double, Double, Double,Double> off1,
											Tuple5<Integer, Double, Double, Double,Double> off2) throws Exception {
										// TODO Auto-generated method stub
										return new Tuple5<Integer, Double, Double, Double,Double>(
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

				JavaPairRDD<WorkflowScheduling,Tuple5<Integer,Double,Double,Double,Double>> histogram = 
						aggregation.mapToPair(
								new PairFunction<Tuple2<WorkflowScheduling,Tuple5<Integer, Double, Double, Double,Double>>,
								WorkflowScheduling,Tuple5<Integer, Double, Double, Double,Double>>()
								{

									private static final long serialVersionUID = 1L;

									@Override
									public Tuple2<WorkflowScheduling, Tuple5<Integer, Double, Double, Double, Double>> call(
											Tuple2<WorkflowScheduling, Tuple5<Integer, Double, Double, Double, Double>> arg0)
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

										return new Tuple2<WorkflowScheduling,Tuple5<Integer, Double, Double, Double,Double>>(arg0._1,tNew);
									}


								}

								);
				
				Tuple2<WorkflowScheduling, Tuple5<Integer,Double,Double,Double,Double>> mostFrequent = histogram.max(new FrequencyComparator());
				System.out.println(mostFrequent);
				outRT[i+1][j+1] = Double.toString(mostFrequent._2()._2());
				outCost[i+1][j+1] = Double.toString(mostFrequent._2()._3());
				outRel[i+1][j+1] = Double.toString(mostFrequent._2()._4());
			}
		jscontext.close();

		try {
			BufferedWriter RTwriter = new BufferedWriter(new FileWriter("RT.out"));
			BufferedWriter Costwriter = new BufferedWriter(new FileWriter("COST.out"));
			BufferedWriter RLwriter = new BufferedWriter(new FileWriter("RL.out"));
						
			for(int i = 0; i < mipsConf.length + 1; i++)
			{
				for(int j = 0; j < inOutDataConf.length + 1; j++)
				{
					RTwriter.write(outRT[i][j] + "\t");
					Costwriter.write(outCost[i][j] + "\t");
					RLwriter.write(outRel[i][j] + "\t");
				}
				RTwriter.write("\n");
				Costwriter.write("\n");
				RLwriter.write("\n");
				RTwriter.flush();
				Costwriter.flush();
				RLwriter.flush();
			}
			RTwriter.close();
			Costwriter.close();
			RLwriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

	private static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> samples = new ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>>();
		for(int i = 0; i < iterations; i++)
		{
			
			MontageWorkflow app1 = new MontageWorkflow(0);
			MontageWorkflow app2 = new MontageWorkflow(1);
			MontageWorkflow app3 = new MontageWorkflow(2);
			MontageWorkflow app4 = new MontageWorkflow(3);
			MontageWorkflow app5 = new MontageWorkflow(4);
			app4.joinSequentially(app5);
			app3.joinSequentially(app4);
			app2.joinSequentially(app3);
			app1.joinSequentially(app2);
			MobileApplication app = app1;
			MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
			WorkflowSchedulingCloudPlanner.setupCloudNodes(inf, FGCSSetup.cloudNum);
			WorkflowSchedulingEdgePlanner.setupEdgeNodes(inf);
			WorkflowSchedulingTerminalsPlanner.setupTerminals(inf);
			WorkflowSchedulingFixedNetworkPlanner.setupNetworkConnections(inf);			
			Tuple2<MobileApplication,MobileCloudInfrastructure> singleSample = new Tuple2<MobileApplication,MobileCloudInfrastructure>(app,inf);
			samples.add(singleSample);
		}
		return samples;
	}
	
}
