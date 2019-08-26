package at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;

public interface VMPlanner {
	
	class VMCPUComparator implements Comparator<VMInstance>, Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3058420470358519683L;

		@Override
		public int compare(VMInstance o1, VMInstance o2) {
			return (int) Double.compare(o1.getCapabilities().getMipsPerCore()*o1.getCapabilities().getAvailableCores()
					,o2.getCapabilities().getMipsPerCore()*o2.getCapabilities().getAvailableCores());
		}
		
	}
	
	abstract ArrayList<VMInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev, MobileDataDistributionInfrastructure inf);
	
	
	default VMInstance findExistingVMInstance(DataEntry d, MobileDevice mDev, MobileDataDistributionInfrastructure inf) {
		System.out.println("VMP");
		VMInstance targetVM = null;
		ArrayList<VMInstance> instancesForUid = inf.getVMAssignment(mDev.getId());
		if(instancesForUid != null) 
		{
			Collections.sort(instancesForUid, new VMCPUComparator());

			for(VMInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements()))
				{
					targetVM = vm;
					break;
				}
		}
		return targetVM;
	}

	default VMInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,VMInstance> repo = currentInfrastructure.getVMRepository();
		double minCost = Double.MAX_VALUE;
		VMInstance targetVM = null;
		for(VMInstance vm : repo.values()) 
		{
			double tmp = (d.getMillionsOfInstruction() / vm.getMipsPerCore()) * vm.getPricePerSecond();
			if(tmp < minCost) 
			{
				minCost = tmp;
				targetVM = vm;
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (VMInstance) targetVM.clone());
		return targetVM;
	} 

}
