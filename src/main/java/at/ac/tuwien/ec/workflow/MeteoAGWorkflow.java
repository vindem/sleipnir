package at.ac.tuwien.ec.workflow;

import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class MeteoAGWorkflow extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 739162783366447018L;
	private static ExponentialDistribution inDataDistr = new ExponentialDistribution(5);
	private static ExponentialDistribution outDataDistr = new ExponentialDistribution(5);
	private static ExponentialDistribution miDistr = new ExponentialDistribution(2);
	//private static ConstantRealDistribution inDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution outDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution miDistr = new ConstantRealDistribution(20000);
	public MeteoAGWorkflow()
	{
		super();
		this.setUserId("entry0");
		
	}
	
	public MeteoAGWorkflow(int wId)
	{
		super(wId);
		this.setUserId("entry0");
	}
	
	public MeteoAGWorkflow(int wId, String uid)
	{
		super(wId,uid);
		this.setUserId("entry0");
	}
	
	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupTasks() {
		addComponent("SOURCE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);		
		addComponent("SIMULATION_INIT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("CASE_INIT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("RAMS_MAKEVFILE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		addComponent("RAMS_INIT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("RAMS_ALL"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		addComponent("REVU_COMPARE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("REVER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("REVU_ALL"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("RAMS_ALL"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
	}

	@Override
	public void setupLinks() {
		addLink("SOURCE"+"_"+getWorkloadId()+","+getUserId()
		, "SIMULATION_INIT"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 1
		addLink("SIMULATION_INIT"+"_"+getWorkloadId()+","+getUserId()
		, "CASE_INIT"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 2
		addLink("CASE_INIT"+"_"+getWorkloadId()+","+getUserId()
		, "RAMS_MAKEVFILE"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("RAMS_MAKEVFILE"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "RAMS_INIT"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "RAMS_ALL"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("RAMS_INIT"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("RAMS_ALL"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "REVU_COMPARE"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("REVU_COMPARE"+"_"+getWorkloadId()+","+getUserId()
		, "REVER"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("REVER"+"_"+getWorkloadId()+","+getUserId()
		, "REVU_ALL"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
				
	}

}
