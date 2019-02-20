package at.ac.tuwien.ec.scheduling.workflow.algorithms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.net.NetworkTopologyWithNodeGroup;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;


public abstract class WorkflowScheduler extends SimIteration implements Serializable{
	
	protected ComputationalNode entryNode;
	
	public WorkflowScheduler()
	{

	}

	public abstract ArrayList<? extends Scheduling> findScheduling();

	private boolean isOffloadPossibleOn(SoftwareComponent s, ComputationalNode n){
		if(s.getUserId().equals(n.getId()))
			return true;
		NetworkConnection link = currentInfrastructure.getLink(s.getUserId(),n.getId());
		if(link!=null)
			return link.getBandwidth() > 0 && link.getLatency() > 0;
		return false;
	}

	private boolean checkLinks(WorkflowScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		for (MobileSoftwareComponent c : deployment.keySet()) {
			if(!c.getUserId().equals(s.getUserId()))
				continue;
			
			if(currentApp.hasDependency(c,s))
			{
				ComponentLink link = currentApp.getDependency(c,s);
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

	

	protected boolean isValid(WorkflowScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		CloudDataCenter c;
		EdgeNode e;
		boolean available;
		if(s.getMillionsOfInstruction() == 0)
			return true;
		boolean compatible = n.isCompatible(s);
		boolean reachable = checkReachability(deployment,s,n);
		if(n instanceof CloudDataCenter) 
		{
			c = (CloudDataCenter) n;
			available = c.isAvailableAt(0);
		}
		else if(n instanceof EdgeNode)
		{
			e = (EdgeNode) n;
			available = e.isAvailableAt(0);
		}
		else 
			available = true;
		return compatible && reachable && available;
						
	}

	private boolean checkReachability(WorkflowScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		ArrayList<MobileSoftwareComponent> preds = currentApp.getPredecessors(s);
		for(MobileSoftwareComponent p : preds)
		{
			ComputationalNode source = deployment.get(p);
			if(!source.equals(n) && currentInfrastructure.getLink(source, n) == null)
				return false;
		}
		return true;
	}

	protected synchronized void deploy(WorkflowScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		deployment.put(s, n);
		//if(!s.getId().startsWith("SOURCE") || !s.getId().startsWith("SINK"))
		//{
			deployment.addCost(s,n, currentInfrastructure);
			deployment.addRuntime(s, s.getRunTime());
			deployment.addReliability(s,n,currentInfrastructure);
			//System.out.println(deployment + " " + deployment.size());
			n.deploy(s);
		//}
	}

	protected void undeploy(WorkflowScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		if (deployment.containsKey(s)) {
			n.undeploy(s);
			deployment.removeRuntime(s, n, currentInfrastructure);
			deployment.removeCost(s, n, currentInfrastructure);
			deployment.removeReliability(s,n,currentInfrastructure);
			deployment.remove(s);
			
		}
		// System.out.println("UNDEP"+deployment);
	}

	public void setEntryNode(ComputationalNode node){
		this.entryNode = node;
	}

}
