package at.ac.tuwien.ec.provisioning;

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
	
	private static final double MILLISECONDS_PER_SECOND = 1000.0;
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
		
		for(EdgeNode en : inf.getEdgeNodes().values())
		{
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS((normalGeneration() + 756.7)/MILLISECONDS_PER_SECOND, 3.0125), 0.9),
					new Tuple2<QoS,Double>(new QoS((normalGeneration() + 1038)/MILLISECONDS_PER_SECOND , 0.13125), 0.1)
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
			
			
			QoSProfile qosUL;//,qosDL;
			qosUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(109.35/MILLISECONDS_PER_SECOND, 8.3875), 0.9),
					new Tuple2<QoS,Double>(new QoS(212/MILLISECONDS_PER_SECOND, 4.2), 0.1)
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
			if(conn < 0.6)
				available = ConnectionAvailable.FourG;
			else if(conn < 0.9 && conn >= 0.6 )
				available = ConnectionAvailable.ThreeG;
			else
				available = ConnectionAvailable.FiveG;
			
			switch(available)
			{
			case FourG:
				iotEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(109.35/MILLISECONDS_PER_SECOND,8.3875),0.97)
						,new Tuple2<QoS,Double>(new QoS(212/MILLISECONDS_PER_SECOND,4.2),0.03)
						));
				iotCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(219.6/MILLISECONDS_PER_SECOND,6.02),0.9998)
						,new Tuple2<QoS,Double>(new QoS(326/MILLISECONDS_PER_SECOND,2.625),0.0002)
						));
				break;
			case ThreeG:
				iotEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(743.16/MILLISECONDS_PER_SECOND,3.0125),0.94)
						,new Tuple2<QoS,Double>(new QoS(1038/MILLISECONDS_PER_SECOND,0.13125),0.06)
						));
				iotCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(756.7/MILLISECONDS_PER_SECOND,1.3125),0.98)
						,new Tuple2<QoS,Double>(new QoS(1122/MILLISECONDS_PER_SECOND,0.13125),0.02)
						));
				break;
			case FiveG:
				iotEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(90.95/MILLISECONDS_PER_SECOND,14.5),0.91)
						,new Tuple2<QoS,Double>(new QoS(148/MILLISECONDS_PER_SECOND,2.75),0.09)
						));
				iotCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(172.18/MILLISECONDS_PER_SECOND,6.04),0.92)
						,new Tuple2<QoS,Double>(new QoS(211/MILLISECONDS_PER_SECOND,2.625),0.08)
						));
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
					
			
			ArrayList<EdgeNode> proximity = new ArrayList<EdgeNode>();
			
			for(EdgeNode en : inf.getEdgeNodes().values()) 
				if(computeDistance(d,en) <= 1.0)
					proximity.add(en);
			
			UniformRealDistribution distr = new UniformRealDistribution(0.0,1.0);
			double conn = distr.sample();
			
			ConnectionAvailable available;
			if(conn < 0.6)
				available = ConnectionAvailable.FourG;
			else if(conn < 0.9 && conn >= 0.6 )
				available = ConnectionAvailable.ThreeG;
			else
				available = ConnectionAvailable.FiveG;
			
			QoSProfile mEdge = null,mCloud = null;
			
			switch(available)
			{
			case FourG:
				mEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(109.35/MILLISECONDS_PER_SECOND,8.3875),0.97)
						,new Tuple2<QoS,Double>(new QoS(212/MILLISECONDS_PER_SECOND,4.2),0.03)
						));
				mCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS((normalGeneration()+219.6)/MILLISECONDS_PER_SECOND,6.02),0.9998)
						,new Tuple2<QoS,Double>(new QoS((normalGeneration()+326)/MILLISECONDS_PER_SECOND,2.625),0.0002)
						));
				break;
			case ThreeG:
				mEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(743.16,3.0125),0.94)
						,new Tuple2<QoS,Double>(new QoS(1038,0.13125),0.06)
						));
				mCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS((normalGeneration() + 756.7)/MILLISECONDS_PER_SECOND,1.3125),0.98)
						,new Tuple2<QoS,Double>(new QoS((normalGeneration() + 1122)/MILLISECONDS_PER_SECOND,0.13125),0.02)
						));
				break;
			case FiveG:
				mEdge = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS(90.95,14.5),0.91)
						,new Tuple2<QoS,Double>(new QoS(148,2.75),0.09)
						));
				mCloud = new QoSProfile(asList(
						new Tuple2<QoS,Double>(new QoS((normalGeneration() + 172.18)/MILLISECONDS_PER_SECOND,6.04),0.92)
						,new Tuple2<QoS,Double>(new QoS((normalGeneration() + 211)/MILLISECONDS_PER_SECOND,2.625),0.08)
						));
				break;
			}
			
			/*for(EdgeNode en : proximity)
			{
				inf.addLink(d,en,mEdge);
				inf.addLink(en,d,mEdge);
			}*/
			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(d,en,mEdge);
				inf.addLink(en,d,mEdge);
			}
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


