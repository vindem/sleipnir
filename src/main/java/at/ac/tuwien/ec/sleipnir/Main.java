package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;
import scala.Tuple2;

public class Main {
	
	public static void main(String[] arg)
	{
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> test = generateSamples(SimulationSetup.iterations);
		
		JavaRDD<Tuple2<MobileApplication,MobileCloudInfrastructure>> input = jscontext.parallelize(test);
		JavaRDD<OffloadScheduling> results = input.flatMap(new FlatMapFunction<Tuple2<MobileApplication,MobileCloudInfrastructure>, OffloadScheduling>() {

			@Override
			public Iterator<OffloadScheduling> call(
					Tuple2<MobileApplication, MobileCloudInfrastructure> arg0) throws Exception {
				MinMinResearch search = new MinMinResearch(arg0);
				return (Iterator<OffloadScheduling>) search.findScheduling().iterator();
			}});
		OffloadScheduling firstScheduling = results.first();
		System.out.println(firstScheduling);
		input.count();
		jscontext.close();
	}

	private static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>> samples = new ArrayList<Tuple2<MobileApplication,MobileCloudInfrastructure>>();
		for(int i = 0; i < iterations; i++)
		{
			MobileApplication fbApp = new FacebookApp(0,"mobile_0");
			MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, 3);
			EdgeAllCellPlanner.setupEdgeNodes(inf);
			DefaultMobileDevicePlanner.setupMobileDevices(inf,1);
			DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<MobileApplication,MobileCloudInfrastructure> singleSample = new Tuple2(fbApp,inf);
			samples.add(singleSample);
		}
		return samples;
	}

		//Creates samples for each spark worker
	

}
