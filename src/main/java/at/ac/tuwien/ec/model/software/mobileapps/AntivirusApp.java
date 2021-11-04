package at.ac.tuwien.ec.model.software.mobileapps;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class AntivirusApp extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7200390062022627285L;

	public AntivirusApp()
	{
		super();
	}
	
	public AntivirusApp(int wId){
		super(wId);
	}
	
	public AntivirusApp(int wId, String uid)
	{
		super(wId,uid);
	}
	
	@Override
	public void setupTasks() {
		
		double file_size = OffloadingSetup.antivirusFileSize;
		ExponentialDistribution fileDistr = new ExponentialDistribution(file_size);
		addComponent("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				,4.0e2
				,5e+2
        		,5e+2
        		,false);
		addComponent("LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2e2
				,5e+2
				,10e+2
				);
		addComponent("SCAN_FILE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,2,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e2
				,fileDistr.sample()
				,5e+2
				);
		addComponent("COMPARE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,1,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e2
				,fileDistr.sample()
				,5e+2
				);
		addComponent("ANTIVIRUS_OUTPUT"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,ExponentialDistributionGenerator.getNext(1.0/2.0) + 2.0
				,2e2
				,1e2
				,5e2
				,false
				);
	}

	@Override
	public void setupLinks() {
		addLink("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId(), "LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId(), "SCAN_FILE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				5);
		addLink("LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId(),"COMPARE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("SCAN_FILE"+"_"+getWorkloadId()+","+getUserId(),"COMPARE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("COMPARE"+"_"+getWorkloadId()+","+getUserId(),"ANTIVIRUS_OUTPUT"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
	}

	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub
		
	}

}
