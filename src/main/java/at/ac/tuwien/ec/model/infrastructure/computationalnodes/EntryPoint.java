package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class EntryPoint extends ComputationalNode {

	public EntryPoint(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void sampleNode() {
		// TODO Auto-generated method stub

	}
	
	public double computeCost(SoftwareComponent sc, MobileCloudInfrastructure i)
	{
		return 0.0;		
	}

}
