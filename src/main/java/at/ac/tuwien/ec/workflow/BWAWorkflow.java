package at.ac.tuwien.ec.workflow;

import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class BWAWorkflow extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 739162783366447018L;
	private static ExponentialDistribution inDataDistr = new ExponentialDistribution(500000);
	private static ExponentialDistribution outDataDistr = new ExponentialDistribution(600000);
	private static ExponentialDistribution miDistr = new ExponentialDistribution(20000);
	//private static ConstantRealDistribution inDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution outDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution miDistr = new ConstantRealDistribution(20000);
	public BWAWorkflow()
	{
		super();
		this.setUserId("entry0");
		
	}
	
	public BWAWorkflow(int wId)
	{
		super(wId);
		this.setUserId("entry0");
	}
	
	public BWAWorkflow(int wId, String uid)
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
		addComponent("bwa:split1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
		addComponent("bwa:split2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
		addComponent("bwa:bwaindex"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
		addComponent("BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		addComponent("bwa:bwa1ain"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
		addComponent("bwa:concat"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
		addComponent("SINK"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,miDistr.sample()
        		,inDataDistr.sample()
        		,outDataDistr.sample()
        		);
	}

	@Override
	public void setupLinks() {
		addLink("SOURCE"+"_"+getWorkloadId()+","+getUserId()
		, "bwa:split1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("SOURCE"+"_"+getWorkloadId()+","+getUserId()
		, "bwa:split2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("SOURCE"+"_"+getWorkloadId()+","+getUserId()
		, "bwa:bwaindex"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 1
		addLink("bwa:split1"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bwa:split2"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bwa:bwaindex"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "bwa:bwa1ain"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bwa:bwa1ain"+"_"+getWorkloadId()+","+getUserId()
		, "bwa:concat"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bwa:concat"+"_"+getWorkloadId()+","+getUserId()
		, "SINK"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		
	}

}
