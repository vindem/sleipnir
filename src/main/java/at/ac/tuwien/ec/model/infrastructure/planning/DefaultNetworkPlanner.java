package at.ac.tuwien.ec.model.infrastructure.planning;

import static java.util.Arrays.asList;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.QoS;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

public class DefaultNetworkPlanner {
	
	static double wifiAvailableProbability = SimulationSetup.wifiAvailableProbability;
	
	private static double exponentialGeneration(double lambda)
	{
		return new ExponentialDistribution(lambda).sample();
	}
	
	private static double normalGeneration()
	{
		double value;
		do
			value = new NormalDistribution(200.0,33.5).sample();
		while(value <= 0);
		return value;
	}
	
	public static void setupNetworkConnections(MobileDataDistributionInfrastructure inf)
	{
		for(MobileDevice d: inf.getMobileDevices().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			//double firstHop3GBandwidth = exponentialGeneration(7.2);
			//double firstHopWiFiHQBandwidth = exponentialGeneration(32.0); 
			//double firstHopWiFiLQBandwidth = exponentialGeneration(4.0);
			double firstHop3GBandwidth = 7.2 + exponentialGeneration(1.2);
			double firstHopWiFiHQBandwidth = 32.0 + exponentialGeneration(2.0); 
			double firstHopWiFiLQBandwidth = 4.0 + exponentialGeneration(1.0);
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosUL;//,qosDL;
			qosUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
			/* 
			 * In the newer version, we want to enable qos profiles for upload and download.
			 * For now, we use just one profile.
			 * qosDL = (wifiAvailable)? new QoSProfile(asList(
			 *	new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
			 *	new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
			 *	new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
			 *	)) : new QoSProfile(asList(
			 *			new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9959),
			 *			new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0041)));
			 */
			
			for(EdgeNode en : inf.getEdgeNodes().values())
				inf.addLink(d,en,qosUL);
			
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */
			//double Cloud3GBandwidth = exponentialGeneration(3.6);
        	//double CloudWiFiHQBandwidth = exponentialGeneration(16.0);
        	//double CloudWiFiLQBandwidth = exponentialGeneration(2.0);
			double Cloud3GBandwidth = 3.6 + exponentialGeneration(1.6);
        	double CloudWiFiHQBandwidth = 16.0 + exponentialGeneration(1.6);
        	double CloudWiFiLQBandwidth = 2.0 + exponentialGeneration(1.0);
        	double cloudLatency = normalGeneration() * SimulationSetup.MAP_M;
			//double cloudLatency = normalGeneration();
			
        	QoSProfile qosCloudUL;//,qosCloudDL
        	qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
        	
			for(CloudDataCenter cn : inf.getCloudNodes().values())
				inf.addLink(d, cn, qosCloudUL);
			
			
			
			
		}
		
		for(IoTDevice iot: inf.getIotDevices().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			double firstHop3GBandwidth = 7.2 + exponentialGeneration(1.2);
			double firstHopWiFiHQBandwidth = 32.0 + exponentialGeneration(2.0); 
			double firstHopWiFiLQBandwidth = 4.0 + exponentialGeneration(1.0);
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosUL;//,qosDL;
			qosUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
			/* 
			 * In the newer version, we want to enable qos profiles for upload and download.
			 * For now, we use just one profile.
			 * qosDL = (wifiAvailable)? new QoSProfile(asList(
			 *	new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
			 *	new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
			 *	new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
			 *	)) : new QoSProfile(asList(
			 *			new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9959),
			 *			new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0041)));
			 */
			
			for(EdgeNode en : inf.getEdgeNodes().values())
				inf.addLink(iot,en,qosUL);
			
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */
			double Cloud3GBandwidth = 3.6 + exponentialGeneration(1.6);
        	double CloudWiFiHQBandwidth = 16.0 + exponentialGeneration(1.6);
        	double CloudWiFiLQBandwidth = 2.0 + exponentialGeneration(1.0);
        	double cloudLatency = normalGeneration() * SimulationSetup.MAP_M;
			//double cloudLatency = normalGeneration();
			
        	QoSProfile qosCloudUL;//,qosCloudDL
        	qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
        	
			for(CloudDataCenter cn : inf.getCloudNodes().values())
				inf.addLink(iot, cn, qosCloudUL);
		
		}
	}

}
