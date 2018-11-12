package at.ac.tuwien.ec.model.infrastructure.planning;

import static java.util.Arrays.asList;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.QoS;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

public class DefaultNetworkPlanner {
	
	static double wifiAvailableProbability = SimulationSetup.wifiAvailableProbability;
	
	
	public static void setupNetworkConnections(MobileCloudInfrastructure inf)
	{
		for(MobileDevice d: inf.getMobileDevices())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			double firstHop3GBandwidth = (new ExponentialDistribution(1.0/7.2)).sample();
			double firstHopWiFiHQBandwidth = (new ExponentialDistribution(1.0/32.0)).sample(); 
			double firstHopWiFiLQBandwidth = (new ExponentialDistribution(1.0/4.0)).sample();
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
			
			for(EdgeNode en : inf.getEdgeNodes())
				inf.addLink(d,en,qosUL);
			
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */
			double Cloud3GBandwidth = (new ExponentialDistribution(1.0/3.6)).sample();
        	double CloudWiFiHQBandwidth = (new ExponentialDistribution(1.0/16.0)).sample();
        	double CloudWiFiLQBandwidth = (new ExponentialDistribution(1.0/2.0)).sample();
        	double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			
        	QoSProfile qosCloudUL;//,qosCloudDL
        	qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , firstHopWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, firstHop3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
        	
			for(CloudDataCenter cn : inf.getCloudNodes())
				inf.addLink(d, cn, qosCloudUL);
			
			
		}
	}

}
