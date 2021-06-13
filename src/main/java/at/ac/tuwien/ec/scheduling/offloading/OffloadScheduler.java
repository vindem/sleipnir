package at.ac.tuwien.ec.scheduling.offloading;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.net.NetworkTopologyWithNodeGroup;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;


public abstract class OffloadScheduler extends SimIteration implements Serializable{
	
		
	
	private static final long serialVersionUID = 3536972473535149228L;
	private double currentTime = 0.0;

	public OffloadScheduler()
	{

	}

	public abstract ArrayList<? extends Scheduling> findScheduling();
	
	public void postTaskScheduling(OffloadScheduling scheduling)
	{
		int currentTimestamp = (int) Math.floor(scheduling.getRunTime());
		for(MobileDevice d : this.getInfrastructure().getMobileDevices().values()) 
			d.updateCoordsWithMobility((double)currentTimestamp);
	}

	protected boolean isOffloadPossibleOn(MobileSoftwareComponent s, ComputationalNode n){
		if(s.getUserId().equals(n.getId()))
			return true;
		NetworkConnection link = currentInfrastructure.getLink(s.getUserId(),n.getId());
		if(link!=null)
			return link.getBandwidth() > 0 && link.getLatency() > 0;
		return false;
	}

	protected boolean checkLinks(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		for (SoftwareComponent c : deployment.keySet()) {
			if(!c.getUserId().equals(s.getUserId()))
				continue;
			
			if(currentApp.hasDependency((MobileSoftwareComponent) c,s))
			{
				ComponentLink link = currentApp.getDependency((MobileSoftwareComponent) c,s);
				if(link==null)
					return false;
				QoSProfile requirements = link.getDesiredQoS();
				if(currentInfrastructure.getTransmissionTime(s, currentInfrastructure.getNodeById(s.getUserId()), n)
						> currentInfrastructure.getDesiredTransmissionTime(s,
								currentInfrastructure.getNodeById(s.getUserId()),
								n,
								requirements));
			}
		}
		return true;
	}

	

	protected boolean isValid(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		if(s.getMillionsOfInstruction() == 0)
			return true;
		double consOnMobile = (currentInfrastructure.getMobileDevices().containsKey(n.getId()))? 
				n.getCPUEnergyModel().computeCPUEnergy(s, n, currentInfrastructure) :
					currentInfrastructure.getNodeById(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, n, currentInfrastructure) ;
				boolean compatible = n.isCompatible(s);
				boolean offloadPossible = isOffloadPossibleOn(s, n);
				boolean consAcceptable = ((MobileDevice)currentInfrastructure.getNodeById(s.getUserId())).getEnergyBudget() - consOnMobile >= 0;
				boolean linksOk = checkLinks(deployment,s,n);
				return compatible && offloadPossible && consAcceptable;// && linksOk;
						
	}

	/**
	 * Adds task to the current deployment, updating its values and hardware availability
	 * @param deployment the current OffloadScheduling
	 * @param s the MobileSoftwareComponent
	 * @param n the target ComputationalNode
	 */
	protected synchronized void deploy(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		deployment.put(s, n);
		deployment.addCost(s,n, currentInfrastructure);
		deployment.addEnergyConsumption(s, n, currentInfrastructure);
		deployment.addProviderCost(s,n,currentInfrastructure);
		deployment.addRuntime(s, n, currentInfrastructure);
		n.deploy(s); //updates hardware availability
	}

	/**
	 * Removes task to the current deployment, updating values and hardware availability
	 * @param deployment the current OffloadScheduling
	 * @param s the MobileSoftwareComponent
	 * @param n the target ComputationalNode
	 */
	protected void undeploy(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		if (deployment.containsKey(s)) {
			n.undeploy(s); //updates hardware availability
			deployment.removeRuntime(s, n, currentInfrastructure);
			deployment.removeCost(s, n, currentInfrastructure);
			deployment.removeEnergyConsumption(s, n, currentInfrastructure);
			deployment.removeProviderCost(s,n,currentInfrastructure);
			deployment.remove(s);
		}
	}

	

	

}
