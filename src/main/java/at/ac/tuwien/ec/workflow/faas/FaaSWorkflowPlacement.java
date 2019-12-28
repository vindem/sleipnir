package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;

public class FaaSWorkflowPlacement extends OffloadScheduling {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1078404020343837193L;
	
	private double averageLatency=0,maxLatency=0,cost=0,energyConsumption=0;

	public Double getAverageLatency() {
		// TODO Auto-generated method stub
		return averageLatency;
	}

	public Double getMaxLatency() {
		// TODO Auto-generated method stub
		return maxLatency;
	}

	public Double getCost() {
		// TODO Auto-generated method stub
		return cost;
	}
	
	public Double getAverageEnergyConsumption()
	{
		return energyConsumption;
	}
	
	public void addAverageLatency(double averageLatency){
    	this.averageLatency += averageLatency;
    }
	
	public void addAverageLatency(MobileSoftwareComponent s, ComputationalNode v, MobileCloudInfrastructure I){
    	double tmp = s.getRuntimeOnNode(v, I);
		
    	s.setRunTime(tmp);
    	this.averageLatency += tmp;
    }
	
	
	public void addAverageLatency(MobileSoftwareComponent s, ComputationalNode u, ComputationalNode v, MobileCloudInfrastructure I){
    	double tmp;
		if(u==null)
    		tmp = s.getRuntimeOnNode(v, I);
		else
			tmp = s.getRuntimeOnNode(u, v, I);
    	s.setRunTime(tmp);
    	this.averageLatency += tmp;
    }
	
	public void addEnergyConsumption(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		//
		
	}

	public void addCost(MobileSoftwareComponent s, ComputationalNode v, MobileCloudInfrastructure i)
	{
		this.cost = v.computeCost(s,i);
	}
	
	
	
	
}
