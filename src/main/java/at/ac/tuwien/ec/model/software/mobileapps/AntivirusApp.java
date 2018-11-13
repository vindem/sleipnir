package at.ac.tuwien.ec.model.software.mobileapps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class AntivirusApp extends MobileApplication {

	@Override
	public void setupTasks() {
		
		double file_size = SimulationSetup.antivirusFileSize;
		addComponent("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				,4.0e3*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
        		,5e+3*SimulationSetup.task_multiplier
        		,false);
		addComponent("LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2e3*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
				,10e+3*SimulationSetup.task_multiplier
				);
		addComponent("SCAN_FILE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,2,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e3*SimulationSetup.task_multiplier
				,file_size*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
				);
		addComponent("COMPARE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,1,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e3*SimulationSetup.task_multiplier
				,file_size + 1e3*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
				);
		addComponent("ANTIVIRUS_OUTPUT"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,ExponentialDistributionGenerator.getNext(1.0/2.0) + 2.0
				,2e3*SimulationSetup.task_multiplier
				,1e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
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
