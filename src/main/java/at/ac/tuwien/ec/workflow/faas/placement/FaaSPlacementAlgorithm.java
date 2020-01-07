package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;

import at.ac.tuwien.ec.datamodel.algorithms.placement.DataPlacementAlgorithm;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;


public abstract class FaaSPlacementAlgorithm extends DataPlacementAlgorithm {

	private FaaSWorkflow currentWorkflow;
	private MobileDataDistributionInfrastructure currentInfrastructure;
	
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
	
	public boolean isSink(MobileSoftwareComponent msc)
	{
		return msc.equals(getCurrentWorkflow().getSink());
	}
	
	public boolean isSource(MobileSoftwareComponent msc)
	{
		return msc.equals(getCurrentWorkflow().getSource());
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4356975159345067995L;

	public abstract ArrayList<? extends Scheduling> findScheduling();

}
