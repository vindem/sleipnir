package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithIoTMobility;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;


public abstract class FaaSPlacementAlgorithm  {

	private FaaSWorkflow currentWorkflow;
	private MobileDataDistributionInfrastructure currentInfrastructure;
	protected double currentTime = 0.0;
	double updateCounter = 0.0;
	int updateIntervals = 0;
		
	protected double getCurrentTime() {
		return currentTime;
	}

	protected void setCurrentTime(double currentTime) {
		this.currentTime += currentTime;
	}
	
	protected boolean updateCondition()
	{
		return true;
		/*if(IoTFaaSSetup.updateTime == 0.0)
			return false;
		updateCounter = getCurrentTime() - (updateIntervals * IoTFaaSSetup.updateTime);
		if(updateCounter >= IoTFaaSSetup.updateTime)
		{
			updateIntervals++;
			updateCounter = 0.0;
			return true;
		}
		return false;*/
		//return Math.floor(getCurrentTime()) % SimulationSetup.updateTime == 0.0;
	}

	public MobileDataDistributionInfrastructure getInfrastructure() {
		return currentInfrastructure;
	}

	public void setInfrastructure(MobileDataDistributionInfrastructure currentInfrastructure) {
		this.currentInfrastructure = currentInfrastructure;
	}

	public FaaSWorkflow getCurrentWorkflow() {
		return currentWorkflow;
	}

	public void setCurrentWorkflow(FaaSWorkflow currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}
	

	protected void deploy(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg)
	{
		trg.deploy(msc);
		placement.put(msc,trg);
		//placement.addAverageLatency(msc, trg, currentInfrastructure);
		//placement.addCost(msc, trg, currentInfrastructure);
		//placement.addEnergyConsumption(msc, trg, currentInfrastructure);
	}
	
	protected void deploy(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg,
			ArrayList<IoTDevice> publisherDevices, ArrayList<MobileDevice> subscriberDevices)
	{
		deploy(placement, msc, trg);
		addAverageLatency(placement,msc,trg,publisherDevices,subscriberDevices);
		addCost(placement,msc,trg);
	}
	
	protected void addCost(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg) {
		double predTime = Double.MIN_VALUE;
		ComputationalNode maxTrg = null;
		for(MobileSoftwareComponent pred : getCurrentWorkflow().getPredecessors(msc))
		{
			ComputationalNode prevTarget = (ComputationalNode) placement.get(pred);
			double currTime = computeTransmissionTime(prevTarget,trg) + pred.getRunTime();
			if(Double.isFinite(currTime) && currTime > predTime) 
			{
				predTime = currTime;
				maxTrg = prevTarget;
			}
		}
		placement.addCost(trg.computeCost(msc, getInfrastructure()) + ((maxTrg == null)? 0.5 : 0.5 * getInfrastructure().getTransmissionTime(msc, maxTrg, trg)));
	}

	protected void addAverageLatency(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc,
			ComputationalNode trg, ArrayList<IoTDevice> publishers, ArrayList<MobileDevice> subscribers) {
		double currentExecutionLatency = 0.0;
		double predTime = 0.0;
		NetworkedNode maxTrg = null;
		for(MobileSoftwareComponent pred : getCurrentWorkflow().getPredecessors(msc))
		{
			NetworkedNode prevTarget = placement.get(pred);
			double currTime = computeTransmissionTime(prevTarget,trg) + pred.getRunTime();
			//System.out.println("Transmission: "+ computeTransmissionTime(prevTarget,trg) + " RT: "+pred.getRunTime());
			if(Double.isFinite(currTime) && currTime > predTime) 
			{
				predTime = currTime;
				maxTrg = prevTarget;
			}
		}
		if(isSource(msc))
		{
			currentExecutionLatency = 0.0;
			double maxLatency = Double.MIN_VALUE;
			
			for(IoTDevice publisher : publishers) 
			{
				double currTTime = computeTransmissionTime(publisher,trg);
				if(Double.isFinite(currTTime) && currTTime > maxLatency) 
					maxLatency = currTTime;
				
			}
			trg.setOutData(msc.getOutData());
			
			currentExecutionLatency = predTime + maxLatency + msc.getLocalRuntimeOnNode(trg, getInfrastructure());
			//placement.addAverageLatency(currentExecutionLatency);
			
			setCurrentTime(currentExecutionLatency);
			msc.setRunTime(currentExecutionLatency);
		}
		else if(isSink(msc))
		{
			double maxLatency = Double.MIN_VALUE;
			
			for(MobileDevice subscriber : subscribers) 
			{
				double currTTime = computeTransmissionTime(trg,subscriber);
				
				if(Double.isFinite(currTTime) && currTTime > maxLatency) 
					maxLatency = currTTime;
				
			}
			trg.setOutData(msc.getOutData());
			
			currentExecutionLatency = predTime + maxLatency + msc.getLocalRuntimeOnNode(trg, getInfrastructure());
			setCurrentTime(currentExecutionLatency);
			msc.setRunTime(currentExecutionLatency);
			
			placement.addAverageLatency(currentExecutionLatency / SimulationSetup.numberOfApps);
		}
		else
		{
			
			
			trg.setOutData(msc.getOutData());
			
			currentExecutionLatency = predTime + 
					computeTransmissionTime(maxTrg,trg) + msc.getLocalRuntimeOnNode(trg, getInfrastructure());
			//placement.addAverageLatency(currentExecutionLatency);
			setCurrentTime(currentExecutionLatency);
			msc.setRunTime(currentExecutionLatency);
		}
		//System.out.println(msc.getId()+":"+currentTime+"target:"+trg.getId());
		//System.out.println(currentTime);
	}

	protected double computeTransmissionTime(NetworkedNode src, ComputationalNode trg) {
		ConnectionMap connections = getInfrastructure().getConnectionMap();
		if(src == null || trg == null)
			return 0.0;
		double transmissionTime = connections.getDataTransmissionTime(src.getOutData(), src, trg);
		
		return transmissionTime;
	}
	
	public boolean isSink(MobileSoftwareComponent msc)
	{
		return msc.equals(getCurrentWorkflow().getSink()) ||
				msc.getId().contains("RESIZE") || msc.getId().contains("SEND_ALERT");
	}
	
	public boolean isSource(MobileSoftwareComponent msc)
	{
		return msc.equals(getCurrentWorkflow().getSource()) ||
				msc.getId().contains("IOT") || msc.getId().contains("EXTRACT");
	}
	
	protected boolean isValid(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
		//tasks with no computational load are always welcome :) (dummy tasks, used for DAG balancing)
		if(s.getMillionsOfInstruction() == 0.0)
			return true;
		
		boolean compatible = n.isCompatible(s); //checks if target node hardware capabilities match task requirements
		return compatible;
						
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
			
			if(getCurrentWorkflow().hasDependency((MobileSoftwareComponent) c,s))
			{
				ComponentLink link = getCurrentWorkflow().getDependency((MobileSoftwareComponent) c,s);
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
	
	protected void mobilityManagement() {
		int currentTimestamp;
		currentTimestamp = (int) Math.round(getCurrentTime());
		//System.out.println("TIMESTAMP: "+currentTimestamp);
		for(MobileDevice d : this.getInfrastructure().getMobileDevices().values()) 
			d.updateCoordsWithMobility((double)currentTimestamp);
		//System.out.println("ID: " + d.getId() + "COORDS: " + d.getCoords());
		
		MobilityBasedNetworkPlanner.setupMobileConnections(getInfrastructure());
		MobileDevicePlannerWithIoTMobility.updateDeviceSubscriptions(getInfrastructure(),
				IoTFaaSSetup.selectedWorkflow);
	}
	
	protected ArrayList<MobileSoftwareComponent> readyTasks(DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> taskDependencies)
	{
		ArrayList<MobileSoftwareComponent> readyTasks = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : taskDependencies.vertexSet())
			if(taskDependencies.incomingEdgesOf(msc).isEmpty())
				readyTasks.add(msc);
		return readyTasks;		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4356975159345067995L;

	public abstract ArrayList<? extends Scheduling> findScheduling();

}
