package at.ac.tuwien.ec.provisioning.workflow;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EntryPoint;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class WorkflowSchedulingTerminalsPlanner {
	
	protected static int MAP_M = FGCSSetup.MAP_M;
	protected static int MAP_N = FGCSSetup.MAP_N;
	
	static HardwareCapabilities defaultTerminalHardwareCapabilities 
	= FGCSSetup.defaultMobileDeviceHardwareCapabilities;
	
	public static void setupTerminals(MobileCloudInfrastructure inf)
	{
		Coordinates coords = new Coordinates(MAP_M/2,MAP_N/2);
		EntryPoint entryP = new EntryPoint("entry0", defaultTerminalHardwareCapabilities);
		entryP.setCoords(coords);
		inf.addTerminal(entryP);
		
	}

}
