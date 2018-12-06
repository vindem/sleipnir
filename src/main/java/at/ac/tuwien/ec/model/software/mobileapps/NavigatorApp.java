package at.ac.tuwien.ec.model.software.mobileapps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class NavigatorApp extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8523061215581247638L;

	public double data_variance = 1e+3;
	
	double config_panel_mips = 1.0e3;
    double gps_mips = 1.0e3;
    double control_mips = 15e3;
    double maps_mips = 15.0e3;
    double path_calc_mips = 20.0e3;
    double traffic_mips = 15.0e3;
    double voice_synth_mips = 15e3;
    double gui_mips = 5e3;
    double speed_mips = 5e3;
	
	public NavigatorApp(){
		super();
	}
	
	public NavigatorApp(int wId){
		super(wId);
	}
	
	public NavigatorApp(int wId, String uid){
		super(wId,uid);
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
		double data_size = SimulationSetup.navigatorMapSize;
		addComponent("CONF_PANEL"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 0.1, 1)
				,this.getUserId()
				//,2.0 + ExponentialDistributionGenerator.getNext(config_panel_mips)*1e-1
        		,2.0e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,false
        		);
        addComponent("GPS"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 0.5, 1)
        		,this.getUserId()
        		//,2.0 + ExponentialDistributionGenerator.getNext(gps_mips)*1e-1
        		,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,false
        		);
        addComponent("CONTROL"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 1, 1)
        		,this.getUserId()
        		//,5.0 + ExponentialDistributionGenerator.getNext(control_mips)*1e-1
        		,5.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		);
        addComponent("MAPS"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 2, 5)
        		,this.getUserId()
        		//,10.0 + ExponentialDistributionGenerator.getNext(maps_mips)*1e-1
        		,10.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,data_size*SimulationSetup.task_multiplier
        		);
        addComponent("PATH_CALC"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 2, 10)
        		,this.getUserId()
        		//,15.0 + ExponentialDistributionGenerator.getNext(path_calc_mips)*1e-1
        		,15.0e3*SimulationSetup.task_multiplier
        		,data_size*SimulationSetup.task_multiplier
        		,data_size*SimulationSetup.task_multiplier
        		);
        addComponent("TRAFFIC"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 1, 1)
        		,this.getUserId()
        		//,10.0 + ExponentialDistributionGenerator.getNext(traffic_mips)*1e-1
        		,10.0e3*SimulationSetup.task_multiplier
        		,data_size*SimulationSetup.task_multiplier
        		,20e3*SimulationSetup.task_multiplier
        		);
        addComponent("VOICE_SYNTH"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 1, 1)
        		,this.getUserId()
        		//,ExponentialDistributionGenerator.getNext(voice_synth_mips)*1e-1
        		,voice_synth_mips*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		,2e3*SimulationSetup.task_multiplier
        		,false
        		);
       addComponent("SPEED_TRAP"+"_"+getWorkloadId()+","+getUserId(),
        		new Hardware(1, 0.5, 1)
        		,this.getUserId()
        		//,2.0 + ExponentialDistributionGenerator.getNext(speed_mips)*1e-1
        		,2.0e3*SimulationSetup.task_multiplier
        		,10e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		,false
        		);
       addComponent("GUI"+"_"+getWorkloadId()+","+getUserId(),
       		new Hardware(1, 0.5, 1)
       		,this.getUserId()
       		//,2.0 + ExponentialDistributionGenerator.getNext(gui_mips)*1e-1
       		,2.0e3*SimulationSetup.task_multiplier
       		,10e3*SimulationSetup.task_multiplier
       		,1e3*SimulationSetup.task_multiplier
       		,false
       		);
	}

	@Override
	public void setupLinks() {
		addLink("CONF_PANEL"+"_"+getWorkloadId()+","+getUserId(), "GPS"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				Double.MIN_VALUE);
        addLink("CONF_PANEL"+"_"+getWorkloadId()+","+getUserId(), "CONTROL"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("GPS"+"_"+getWorkloadId()+","+getUserId(), "CONTROL"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("CONTROL"+"_"+getWorkloadId()+","+getUserId(), "MAPS"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("CONTROL"+"_"+getWorkloadId()+","+getUserId(), "PATH_CALC"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("CONTROL"+"_"+getWorkloadId()+","+getUserId(), "TRAFFIC"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("MAPS"+"_"+getWorkloadId()+","+getUserId(), "PATH_CALC"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("TRAFFIC"+"_"+getWorkloadId()+","+getUserId(), "PATH_CALC"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("PATH_CALC"+"_"+getWorkloadId()+","+getUserId(), "VOICE_SYNTH"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("PATH_CALC"+"_"+getWorkloadId()+","+getUserId(), "GUI"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("PATH_CALC"+"_"+getWorkloadId()+","+getUserId(), "SPEED_TRAP"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("VOICE_SYNTH"+"_"+getWorkloadId()+","+getUserId(), "GUI"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);
        addLink("SPEED_TRAP"+"_"+getWorkloadId()+","+getUserId(), "GUI"+"_"+getWorkloadId()+","+getUserId(),
        		sampleLatency(),
        		Double.MIN_VALUE);		
	}

}
