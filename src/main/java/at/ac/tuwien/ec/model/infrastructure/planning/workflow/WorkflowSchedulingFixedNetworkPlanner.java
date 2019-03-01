package at.ac.tuwien.ec.model.infrastructure.planning.workflow;

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

public class WorkflowSchedulingFixedNetworkPlanner {

	static double wifiAvailableProbability = FGCSSetup.wifiAvailableProbability;


	public static void setupNetworkConnections(MobileCloudInfrastructure inf)
	{

		double[] latencies = {15.0,54.0,15.0,15.0,54.0,15.0};
		double[] bws = {32.0,7.2,4.0,4.0,7.2,32.0};
		int k = 0;
		for(EntryPoint ep : inf.getEntryPoints().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			QoSProfile qosUL;//,qosDL;
			qosUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(latencies[k], bws[k]),1.0)));
			k++;
			for(EdgeNode en1 : inf.getEdgeNodes().values())
			{
					inf.addLink(ep,en1,qosUL);
					inf.addLink(en1,ep,qosUL);
			}
		}

		for(EntryPoint ep : inf.getEntryPoints().values()) 
		{
			//double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			double cloudLatency = 200; 
			QoSProfile qosCloudUL;
			qosCloudUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(cloudLatency,1000.0),1.0)));
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

		
		k = 0;
		for(EdgeNode en0 : inf.getEdgeNodes().values())
		{
			/*
			 * Setting up latency and bandwidth profile between mobile device and Edge nodes.
			 * In this planner, there is a link between each mobile device and each edge node.
			 */
			QoSProfile qosUL;//,qosDL;
			qosUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(latencies[k], bws[k]), 1.0)));
			k++;
			for(EdgeNode en1 : inf.getEdgeNodes().values())
				if(!en0.equals(en1)) 
				{
					inf.addLink(en1,en0,qosUL);
				}

		}
		/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
		 * In this planner, there is a link between each mobile device and each Cloud node.
		 */
		for(CloudDataCenter cn0 : inf.getCloudNodes().values()) 
		{
			//double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			double cloudLatency = 200.0;
			QoSProfile qosCloudUL;
			qosCloudUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(cloudLatency,1000.0),1.0)));

			for(CloudDataCenter cn1 : inf.getCloudNodes().values())
				if(!cn0.equals(cn1)) 
				{
				inf.addLink(cn0, cn1, qosCloudUL);
				inf.addLink(cn1, cn0, qosCloudUL);
				}
		}
		k=0;
		for(EdgeNode en : inf.getEdgeNodes().values()) 
		{
			//double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			QoSProfile qosCloudUL;
			qosCloudUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(latencies[k],bws[k]),1.0)));
			k++;
			for(CloudDataCenter cn : inf.getCloudNodes().values()) 
			{
				inf.addLink(cn, en, qosCloudUL);
			}
			
		}
		for(EdgeNode en : inf.getEdgeNodes().values()) 
		{
			//double cloudLatency = (new NormalDistribution(200.0, 33.5)).sample();
			QoSProfile qosCloudUL;
			qosCloudUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(200.0,1000.0),1.0)));
			for(CloudDataCenter cn : inf.getCloudNodes().values()) 
			{
				inf.addLink(en, cn, qosCloudUL);
			}
			
		}
	}
}


