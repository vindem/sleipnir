package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class CloudContainerPricingModel implements PricingModel{

	@Override
	public double computeCost(SoftwareComponent sc, ComputationalNode src, ComputationalNode trg, MobileCloudInfrastructure i) {
		DataEntry de = (DataEntry) sc;
		ContainerInstance vm = de.getContainerInstance();
		return vm.getPricePerSecond() * de.getMillionsOfInstruction() / trg.getMipsPerCore();
	}
	
	public double computeCost(SoftwareComponent sc, ComputationalNode trg, MobileCloudInfrastructure i) {
		DataEntry de = (DataEntry) sc;
		ContainerInstance vm = de.getContainerInstance();
		return vm.getPricePerSecond() * de.getMillionsOfInstruction() / trg.getMipsPerCore();
	}
	

}
