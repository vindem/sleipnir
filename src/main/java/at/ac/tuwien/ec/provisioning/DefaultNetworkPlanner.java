package at.ac.tuwien.ec.provisioning;

import static java.util.Arrays.asList;

import java.util.HashMap;

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
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

public class DefaultNetworkPlanner {
	
	static double wifiAvailableProbability = SimulationSetup.wifiAvailableProbability;
	
	/*static double firstHop3GBandwidth = 7.2 + exponentialGeneration(1.2);
	static double firstHopWiFiHQBandwidth = 32.0 + exponentialGeneration(2.0); 
	static double firstHopWiFiLQBandwidth = 4.0 + exponentialGeneration(1.0);
	static boolean wifiAvailable = RandomUtils.nextDouble() < wifiAvailableProbability;
	static QoSProfile qosUL = (wifiAvailable)? new QoSProfile(asList(
			new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
			new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.09),
			new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
			)) : new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(54.0, firstHop3GBandwidth), 0.9957),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));*/
	
	public static QoSProfile qosUL = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(54.0,12.2),1.0)));
	
	static double Cloud3GBandwidth = 3.6 + exponentialGeneration(1.6);
	static double CloudWiFiHQBandwidth = 16.0 + exponentialGeneration(1.6);
	static double CloudWiFiLQBandwidth = 2.0 + exponentialGeneration(1.0);
	static double cloudLatency = normalGeneration() * SimulationSetup.MAP_M;
	//double cloudLatency = normalGeneration();

	/*static QoSProfile qosCloudUL = (wifiAvailable)? new QoSProfile(asList(
			new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 0.9),
			new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency , CloudWiFiLQBandwidth), 0.09),
			new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
			)) : new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency, Cloud3GBandwidth), 0.9957),
					new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0.0), 0.0043)));*/
	
	static QoSProfile qosCloudUL = new QoSProfile(asList(new Tuple2<QoS,Double>(
		new QoS(54.0+300.0*SimulationSetup.MAP_M,6.8),1.0)));
	
	
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
	
	public static void setupNetworkConnections(MobileCloudInfrastructure inf)
	{
		for(MobileDevice d: inf.getMobileDevices().values())
		{

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

			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(d,en,qosUL);
				inf.addLink(en,d,qosUL);
			}
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */
			double Cloud3GBandwidth = exponentialGeneration(3.6);
			double CloudWiFiHQBandwidth = exponentialGeneration(16.0);
			double CloudWiFiLQBandwidth = exponentialGeneration(2.0);
			
			double cloudLatency = normalGeneration() * SimulationSetup.MAP_M;
			

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
				inf.addLink(d, cn, qosCloudUL);
				inf.addLink(cn, d, qosCloudUL);
			}

		}
		
		for(CloudDataCenter cn : inf.getCloudNodes().values())
		{
			
			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(en, cn, qosCloudUL);
				inf.addLink(cn, en, qosCloudUL);
			}
		}
		
		
	}
	
	public static void setupNetworkConnections(MobileDataDistributionInfrastructure inf)
	{
		for(MobileDevice d: inf.getMobileDevices().values())
		{

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

			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(d,en,qosUL);
				inf.addLink(en,d,qosUL);
			}
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */ 
			double Cloud3GBandwidth = exponentialGeneration(3.6);
			double CloudWiFiHQBandwidth = exponentialGeneration(16.0);
			double CloudWiFiLQBandwidth = exponentialGeneration(2.0);
			double cloudLatency = normalGeneration() * SimulationSetup.MAP_M;
			
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
				inf.addLink(d, cn, qosCloudUL);
				inf.addLink(cn, d, qosCloudUL);
			}
			
		}
		
		for(EdgeNode en : inf.getEdgeNodes().values())
		{
			//double cloudLatency = normalGeneration();
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
		for(EdgeNode en0 : inf.getEdgeNodes().values())
		{
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

			for(EdgeNode en1 : inf.getEdgeNodes().values()) 
			{
				if(en0.equals(en1))
					continue;
				inf.addLink(en0,en1,qosUL);
				inf.addLink(en1,en0,qosUL);
			}
		}
		
		for(IoTDevice iotD: inf.getIotDevices().values())
		{

			

			for(EdgeNode en : inf.getEdgeNodes().values())
			{
				inf.addLink(en, iotD, qosUL);
				inf.addLink(iotD, en, qosUL);
			}
				
			
			

			//for(MobileDevice dev : inf.getMobileDevices().values())
			//{
				//inf.addLink(iotD,dev,qosUL);
				//inf.addLink(dev,iotD,qosUL);
			//}
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */
			//double Cloud3GBandwidth = exponentialGeneration(3.6);
			//double CloudWiFiHQBandwidth = exponentialGeneration(16.0);
			//double CloudWiFiLQBandwidth = exponentialGeneration(2.0);
			

			for(CloudDataCenter cn : inf.getCloudNodes().values())
			{
				inf.addLink(cn, iotD, qosCloudUL);
				inf.addLink(iotD, cn, qosCloudUL);
			}
		}
	}
}


