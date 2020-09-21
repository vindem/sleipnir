package at.ac.tuwien.ec.provisioning.workflow;

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
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EntryPoint;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

public class WorkflowSchedulingNetworkPlanner {

	static double wifiAvailableProbability = FGCSSetup.wifiAvailableProbability;


	public static void setupNetworkConnections(MobileCloudInfrastructure inf)
	{

		
		for(EntryPoint ep : inf.getEntryPoints().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			double firstHop3GBandwidth = 7.2 + (new ExponentialDistribution(1.0)).sample();
			double firstHopWiFiHQBandwidth = 32.0 + (new ExponentialDistribution(2.0)).sample(); 
			double firstHopWiFiLQBandwidth = 4.0 + (new ExponentialDistribution(1.0)).sample();
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosUL;//,qosDL;
			qosUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
			for(EdgeNode en1 : inf.getEdgeNodes().values())
			{
					inf.addLink(ep,en1,qosUL);
					inf.addLink(en1,ep,qosUL);
			}
		}

		for(EntryPoint ep : inf.getEntryPoints().values()) 
		{
			double Cloud3GBandwidth = 3.6 + (new ExponentialDistribution(0.6)).sample();
			double CloudWiFiHQBandwidth = 1000.0 + (new ExponentialDistribution(10.0)).sample();
			double CloudWiFiLQBandwidth = 1000.0 + (new ExponentialDistribution(10.0)).sample();
			double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));

			for(CloudDataCenter cn1 : inf.getCloudNodes().values())
			{
				inf.addLink(ep, cn1, qosCloudUL);
				inf.addLink(cn1, ep, qosCloudUL);
			}
		}
		
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

		for(EdgeNode en0 : inf.getEdgeNodes().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			double firstHop3GBandwidth = 7.2 + (new ExponentialDistribution(1.0)).sample();
			double firstHopWiFiHQBandwidth = 32.0 + (new ExponentialDistribution(2.0)).sample(); 
			double firstHopWiFiLQBandwidth = 4.0 + (new ExponentialDistribution(1.0)).sample();
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosUL;//,qosDL;
			qosUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
			for(EdgeNode en1 : inf.getEdgeNodes().values())
				if(!en0.equals(en1)) 
				{
					inf.addLink(en0,en1,qosUL);
					inf.addLink(en1,en0,qosUL);
				}

		}
		/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
		 * In this planner, there is a link between each mobile device and each Cloud node.
		 */
		for(CloudDataCenter cn0 : inf.getCloudNodes().values()) 
		{
			double Cloud3GBandwidth = (new ExponentialDistribution(3.6)).sample();
			//double CloudWiFiHQBandwidth = (new ExponentialDistribution(1000.0)).sample();
			//double CloudWiFiLQBandwidth = (new ExponentialDistribution(1000.0)).sample();
			double CloudWiFiHQBandwidth = 1000.0;
			double CloudWiFiLQBandwidth = 1000.0;
			double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));

			for(CloudDataCenter cn1 : inf.getCloudNodes().values())
				if(!cn0.equals(cn1)) 
				{
				inf.addLink(cn0, cn1, qosCloudUL);
				inf.addLink(cn1, cn0, qosCloudUL);
				}
		}
		for(EdgeNode en : inf.getEdgeNodes().values()) 
		{
			double Cloud3GBandwidth = 3.6 + (new ExponentialDistribution(1.0)).sample();
			//double CloudWiFiHQBandwidth = (new ExponentialDistribution(1000.0)).sample();
			//double CloudWiFiLQBandwidth = (new ExponentialDistribution(1000.0)).sample();
			double CloudWiFiHQBandwidth = 1000.0;
			double CloudWiFiLQBandwidth = 1000.0;
			//double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			double cloudLatency = 300;
			boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)) : new QoSProfile(asList(
							new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
							new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));
			for(CloudDataCenter cn : inf.getCloudNodes().values()) 
			{
				inf.addLink(en, cn, qosCloudUL);
				inf.addLink(cn, en, qosCloudUL);
			}
		}
	}
}


