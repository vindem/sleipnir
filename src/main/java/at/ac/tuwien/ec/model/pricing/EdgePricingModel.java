package at.ac.tuwien.ec.model.pricing;

import java.io.Serializable;
import java.util.ArrayList;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class EdgePricingModel implements PricingModel,Serializable{

	private PricingModel cloudPricing = new CloudFixedPricingModel();
	
	public double computeCost(SoftwareComponent sc, ComputationalNode cn0, ComputationalNode cn, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return cloudPricing.computeCost(sc, cn0, i) + computeEdgePenalty(sc,cn0, i);
	}

	private double computeEdgePenalty(SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i) {
		double instCloudCost = cloudPricing.computeCost(sc,src,i);
    	double minCloudTime = 10e6;
    	double minEdgeTime = 10e6;
    	double minEdgeLatency = 10e6;
    	double minCloudLatency = 10e6;
    	double averageCloudLatency = 700.0;
    	double averageEdgeLatency = 97.5;
    	
    	NetworkedNode reference = i.getNodeById(sc.getUserId());
    	if(reference == null)
    	{
    		ArrayList<MobileDevice> tmp = new ArrayList<MobileDevice>();
    		tmp.addAll(i.getMobileDevices().values());
    		reference = tmp.get(0);
    	}
   	
    	for(NetworkConnection l : i.getOutgoingLinksFrom(reference))
    	{
    		NetworkedNode n = (NetworkedNode) l.getTarget();
    		if(i.getCloudNodes().containsValue(n)) 
    		{ 
    			if(l.getLatency() < minCloudLatency)
    				minCloudLatency = l.getLatency();
    			
    		}
    		else if(i.getEdgeNodes().containsValue(n) && l.getLatency() < minEdgeLatency)
    			minEdgeLatency = l.getLatency();
    	}
    	
    	for(CloudDataCenter c : i.getCloudNodes().values()){
    		double tmp = sc.getRuntimeOnNode(src, c, i);
    		if(tmp < minCloudTime)
    			minCloudTime = tmp;
    	}
    	
    	for(EdgeNode f : i.getEdgeNodes().values()){
    		double tmp = sc.getRuntimeOnNode(src, f, i);
    		if(tmp < minEdgeTime)
    			minEdgeTime = tmp;
    	}
    	
    	
    	double cloudCost = instCloudCost * minCloudTime;
    	/*average cloud latency + average service rate on cloud - average latency edge
       	*/ 
    	
    	double timeFactor =  averageCloudLatency + (1.0/OffloadingSetup.cloudMipsPerCore) - averageEdgeLatency;
    	
    	double penalty = (timeFactor / OffloadingSetup.Eta) 
    			- Math.sqrt((OffloadingSetup.Eta * cloudCost + timeFactor) / 
    					(Math.pow(OffloadingSetup.Eta, 2.0) * 4.0) );
    	
    	return penalty/100000.0;
    }

	@Override
	public double computeCost(SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return cloudPricing.computeCost(sc, src, i) + computeEdgePenalty(sc,src, i);
	}
	

}
