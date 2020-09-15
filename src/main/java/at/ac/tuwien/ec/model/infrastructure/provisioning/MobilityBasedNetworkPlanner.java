package at.ac.tuwien.ec.model.infrastructure.provisioning;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.QoS;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

enum ConnectionAvailable
{
	ThreeG,
	FourG,
	FiveG
}

public class MobilityBasedNetworkPlanner {
	
	static double CloudWiFiHQBandwidth = 16.0 + exponentialGeneration(1.6);
	static double CloudWiFiLQBandwidth = 2.0 + exponentialGeneration(1.0);
		
	
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
		double cloudLatency = normalGeneration();
				
		for(EdgeNode en : inf.getEdgeNodes().values())
		{
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(cloudLatency, CloudWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(cloudLatency , CloudWiFiLQBandwidth), 0.1)
					//,new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					)); 

			for(CloudDataCenter cn : inf.getCloudNodes().values())
			{
				inf.addLink(en, cn, qosCloudUL);
				inf.addLink(cn, en, qosCloudUL);
			}
		}
		for(EdgeNode en0 : inf.getEdgeNodes().values())
		{
			double firstHopWiFiHQBandwidth = 32.0 + exponentialGeneration(2.0); 
			double firstHopWiFiLQBandwidth = 4.0 + exponentialGeneration(1.0);
			
			QoSProfile qosUL;//,qosDL;
			qosUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 0.9),
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiLQBandwidth), 0.1)
					//,new Tuple2<QoS,Double>(new QoS(Double.MAX_VALUE, 0), 0.01)
					));
					

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

			QoSProfile iotEdge = null,iotCloud = null;
			
			UniformRealDistribution distr = new UniformRealDistribution(0.0,1.0);
			double conn = distr.sample();
			ConnectionAvailable available;
			if(conn < 0.8)
				available = ConnectionAvailable.FourG;
			else if(conn < 0.95 && conn >= 0.8 )
				available = ConnectionAvailable.ThreeG;
			else
				available = ConnectionAvailable.FiveG;
			
			switch(available)
			{
			case FourG:
				iotEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(54.0,16.0 + exponentialGeneration(2.0)),1.0)));
				iotCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency,2.0 + exponentialGeneration(2.0)),1.0)));
				break;
			case ThreeG:
				iotEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(20.0,8.0 + exponentialGeneration(2.0)),1.0)));
				iotCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(20.0 + cloudLatency,1.0 + exponentialGeneration(2.0)),1.0)));
				break;
			case FiveG:
				iotEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(5.0,32.0 + exponentialGeneration(2.0)),1.0)));
				iotCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(5.0 + cloudLatency,4.0 + exponentialGeneration(2.0)),1.0)));
				break;
			}

			for(EdgeNode en : inf.getEdgeNodes().values())
			{
				inf.addLink(en, iotD, iotEdge);
				inf.addLink(iotD, en, iotEdge);
			}
				

			for(CloudDataCenter cn : inf.getCloudNodes().values())
			{
				inf.addLink(cn, iotD, iotCloud);
				inf.addLink(iotD, cn, iotCloud);
			}
		}
	}
	
	public static void setupMobileConnections(MobileDataDistributionInfrastructure inf)
	{
		double cloudLatency = normalGeneration();
		
		for(MobileDevice d: inf.getMobileDevices().values())
		{
			if(inf.getConnectionMap().outDegreeOf(d) > 0) {
				ArrayList<NetworkConnection> outEdges = new ArrayList<NetworkConnection>();
				outEdges.addAll(inf.getConnectionMap().outgoingEdgesOf(d));
				for(NetworkConnection conn : outEdges)
					inf.getConnectionMap().removeEdge(conn);
			}
			if(inf.getConnectionMap().inDegreeOf(d) > 0) {
				ArrayList<NetworkConnection> inEdges = new ArrayList<NetworkConnection>();
				inEdges.addAll(inf.getConnectionMap().incomingEdgesOf(d));
				inf.getConnectionMap().removeAllEdges(inEdges);
			}
					
			
			
			int distance = Integer.MAX_VALUE;
			EdgeNode targetEdgeNode = null;
			for(EdgeNode en : inf.getEdgeNodes().values()) 
				if(computeDistance(d,en) < distance)
					targetEdgeNode = en;
			
			UniformRealDistribution distr = new UniformRealDistribution(0.0,1.0);
			double conn = distr.sample();
			ConnectionAvailable available;
			
			QoSProfile mEdge = null,mCloud = null;
			
			if(conn < 0.8)
				available = ConnectionAvailable.FourG;
			else if(conn < 0.95 && conn >= 0.8 )
				available = ConnectionAvailable.ThreeG;
			else
				available = ConnectionAvailable.FiveG;
			
			switch(available)
			{
			case FourG:
				mEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(54.0,16.0 + exponentialGeneration(2.0)),1.0)));
				mCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(54.0 + cloudLatency,2.0 + exponentialGeneration(2.0)),1.0)));
				break;
			case ThreeG:
				mEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(20.0,8.0 + exponentialGeneration(2.0)),1.0)));
				mCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(20.0 + cloudLatency,1.0 + exponentialGeneration(2.0)),1.0)));
				break;
			case FiveG:
				mEdge = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(5.0,32.0 + exponentialGeneration(2.0)),1.0)));
				mCloud = new QoSProfile(asList(new Tuple2<QoS,Double>(new QoS(5.0 + cloudLatency,4.0 + exponentialGeneration(2.0)),1.0)));
				break;
			}
			
			
			inf.addLink(d,targetEdgeNode,mEdge);
			inf.addLink(targetEdgeNode,d,mEdge);
			
			/* Setting up latency and bandwidth profile between mobile devices and Cloud nodes.
			 * In this planner, there is a link between each mobile device and each Cloud node.
			 */ 
			

			for(CloudDataCenter cn : inf.getCloudNodes().values())
			{
				inf.addLink(d, cn, mCloud);
				inf.addLink(cn, d, mCloud);
			}
			
		}
	}
	
	
	
	private static double computeDistance(NetworkedNode u, NetworkedNode v) 
	{
		if( u instanceof CloudDataCenter || v instanceof CloudDataCenter )
			return SimulationSetup.cloudMaxHops;
		Coordinates c1,c2;
		c1 = u.getCoords();
		c2 = v.getCoords();
		return (Math.abs(c1.getLatitude()-c2.getLatitude()) 
				+ Math.max(0, 
						(Math.abs(c1.getLatitude()-c2.getLatitude())
								- Math.abs(c1.getLongitude()-c2.getLongitude()) )/2));
	}


}


