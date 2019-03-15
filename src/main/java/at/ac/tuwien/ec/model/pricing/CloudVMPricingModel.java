package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class CloudVMPricingModel implements PricingModel{

	@Override
	public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
		DataEntry de = (DataEntry) sc;
		VMInstance vm = de.getVMInstance();
		return vm.getPricePerSecond() * de.getMillionsOfInstruction() / cn.getMipsPerCore();
	}
	

}
