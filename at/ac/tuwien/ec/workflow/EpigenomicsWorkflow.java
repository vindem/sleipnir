package at.ac.tuwien.ec.workflow;

import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class EpigenomicsWorkflow extends MobileApplication {

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
	public EpigenomicsWorkflow()
	{
		super();
		this.setUserId("entry0");
		
	}
	
	public EpigenomicsWorkflow(int wId)
	{
		super(wId);
		this.setUserId("entry0");
	}
	
	public EpigenomicsWorkflow(int wId, String uid)
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
		addComponent("fastQSplit"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("filterContams0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("filterContams1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("filterContams2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("filterContams3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("sol2sanger0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("sol2sanger1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("sol2sanger2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("sol2sanger3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("fastq2bfb0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("fastq2bfb1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("fastq2bfb2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("fastq2bfb3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("map0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("map1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("map2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("map3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("map3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("mapMerge"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("mapIndex"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("pileUp"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("SINK"+"_"+getWorkloadId()+","+getUserId(),
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
		, "fastQSplit"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 1
		addLink("fastQSplit"+"_"+getWorkloadId()+","+getUserId()
		, "filterContams0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastQSplit"+"_"+getWorkloadId()+","+getUserId()
		, "filterContams1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastQSplit"+"_"+getWorkloadId()+","+getUserId()
		, "filterContams2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastQSplit"+"_"+getWorkloadId()+","+getUserId()
		, "filterContams3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("filterContams0"+"_"+getWorkloadId()+","+getUserId()
		, "sol2sanger0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("filterContams1"+"_"+getWorkloadId()+","+getUserId()
		, "sol2sanger1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("filterContams2"+"_"+getWorkloadId()+","+getUserId()
		, "sol2sanger2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("filterContams3"+"_"+getWorkloadId()+","+getUserId()
		, "sol2sanger3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("sol2sanger0"+"_"+getWorkloadId()+","+getUserId()
		, "fastq2bfb0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("sol2sanger1"+"_"+getWorkloadId()+","+getUserId()
		, "fastq2bfb1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("sol2sanger2"+"_"+getWorkloadId()+","+getUserId()
		, "fastq2bfb2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("sol2sanger3"+"_"+getWorkloadId()+","+getUserId()
		, "fastq2bfb3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastq2bfb0"+"_"+getWorkloadId()+","+getUserId()
		, "map0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastq2bfb1"+"_"+getWorkloadId()+","+getUserId()
		, "map1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastq2bfb2"+"_"+getWorkloadId()+","+getUserId()
		, "map2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("fastq2bfb3"+"_"+getWorkloadId()+","+getUserId()
		, "map3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("map0"+"_"+getWorkloadId()+","+getUserId()
		, "mapMerge"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("map1"+"_"+getWorkloadId()+","+getUserId()
		, "mapMerge"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("map2"+"_"+getWorkloadId()+","+getUserId()
		, "mapMerge"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("map3"+"_"+getWorkloadId()+","+getUserId()
		, "mapMerge"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("mapMerge"+"_"+getWorkloadId()+","+getUserId()
		, "mapIndex"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("mapIndex"+"_"+getWorkloadId()+","+getUserId()
		, "pileUp"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("pileUp"+"_"+getWorkloadId()+","+getUserId()
		, "SINK"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
	}

}
