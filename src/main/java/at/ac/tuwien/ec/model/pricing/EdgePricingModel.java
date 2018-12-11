package at.ac.tuwien.ec.model.pricing;

import java.io.Serializable;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class EdgePricingModel implements PricingModel,Serializable{

	private PricingModel cloudPricing = new CloudFixedPricingModel();
	
	public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return cloudPricing.computeCost(sc, cn, i) + computeEdgePenalty(sc,cn,i);
	}

	private double computeEdgePenalty(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
		double instCloudCost = cloudPricing.computeCost(sc, cn, i);
    	double minCloudTime = 10e6;
    	double minEdgeTime = 10e6;
    	double minEdgeLatency = 10e6;
    	double minCloudLatency = 10e6;
    	
    	for(NetworkConnection l : i.getOutgoingLinksFrom(i.getNodeById(sc.getUserId())))
    	{
    		ComputationalNode n = l.getTarget();
    		if(i.getCloudNodes().containsValue(n) && l.getLatency() < minCloudLatency)
    			minCloudLatency = l.getLatency();
    		else if(i.getEdgeNodes().containsValue(n) && l.getLatency() < minEdgeLatency)
    			minEdgeLatency = l.getLatency();
    		else 
    			continue;
    	}
    	
    	for(CloudDataCenter c : i.getCloudNodes().values()){
    		double tmp = sc.getRuntimeOnNode(c, i);
    		if(tmp < minCloudTime)
    			minCloudTime = tmp;
    	}
    	
    	for(EdgeNode f : i.getEdgeNodes().values()){
    		double tmp = sc.getRuntimeOnNode(f, i);
    		if(tmp < minEdgeTime)
    			minEdgeTime = tmp;
    	}
    	
    	
    	double cloudCost = instCloudCost * minCloudTime;
    	/*average cloud latency + average service rate on cloud - average latency edge
    	  200.0 is the average of normal distribution for Cloud latency
    	  54.0 the lambda of exponential distribution for latency
    	*/ 
    	double timeFactor =  200.0 + (1.0/SimulationSetup.cloudCoreNum) - 54.0;
    	
    	double penalty = (timeFactor / SimulationSetup.Eta) 
    			- Math.sqrt((SimulationSetup.Eta * cloudCost + timeFactor) / 
    					(Math.pow(SimulationSetup.Eta, 2.0) * 8.0) );
    	
    	return penalty/1000;
    }
	

}
