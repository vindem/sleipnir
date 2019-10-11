package at.ac.tuwien.ec.datamodel.algorithms.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;

public interface VMPlanner {
	
	class VMCPUComparator implements Comparator<ContainerInstance>, Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3058420470358519683L;

		@Override
		public int compare(ContainerInstance o1, ContainerInstance o2) {
			return (int) Double.compare(o1.getCapabilities().getMipsPerCore()*o1.getCapabilities().getAvailableCores()
					,o2.getCapabilities().getMipsPerCore()*o2.getCapabilities().getAvailableCores());
		}
		
	}
	
	abstract ArrayList<ContainerInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev, MobileDataDistributionInfrastructure inf);
	
	
	default ContainerInstance findExistingVMInstance(DataEntry d, MobileDevice mDev, MobileDataDistributionInfrastructure inf) {
		System.out.println("VMP");
		ContainerInstance targetVM = null;
		ArrayList<ContainerInstance> instancesForUid = inf.getVMAssignment(mDev.getId());
		if(instancesForUid != null) 
		{
			Collections.sort(instancesForUid, new VMCPUComparator());

			for(ContainerInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements()))
				{
					targetVM = vm;
					break;
				}
		}
		return targetVM;
	}

	default ContainerInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,ContainerInstance> repo = currentInfrastructure.getVMRepository();
		double minCost = Double.MAX_VALUE;
		ContainerInstance targetVM = null;
		for(ContainerInstance vm : repo.values()) 
		{
			double tmp = (d.getMillionsOfInstruction() / vm.getMipsPerCore()) * vm.getPricePerSecond();
			if(tmp < minCost) 
			{
				minCost = tmp;
				targetVM = vm;
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (ContainerInstance) targetVM.clone());
		return targetVM;
	} 

}
