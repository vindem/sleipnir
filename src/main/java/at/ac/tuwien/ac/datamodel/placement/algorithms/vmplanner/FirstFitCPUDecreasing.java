package at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.FirstFitCPUIncreasing.MIPSRequirementComparator;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.VMPlanner.VMCPUComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;

public class FirstFitCPUDecreasing implements VMPlanner, Serializable{

	class MIPSRequirementDecreasingComparator implements Comparator<DataEntry>, Serializable
	{

		@Override
		public int compare(DataEntry o1, DataEntry o2) {
			return (int) (Double.compare(o2.getMillionsOfInstruction(), o1.getMillionsOfInstruction()));
		}
		
	}
	
	class VMCPUDecreasingComparator implements Comparator<VMInstance>
	{

		@Override
		public int compare(VMInstance o1, VMInstance o2) {
			return (int) Double.compare(o2.getCapabilities().getMipsPerCore()*o2.getCapabilities().getAvailableCores()
					,o1.getCapabilities().getMipsPerCore()*o1.getCapabilities().getAvailableCores());
		}
		
	}
	
	@Override
	public ArrayList<VMInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<VMInstance> vmPlan = new ArrayList<VMInstance>();
		Collections.sort(dList,new MIPSRequirementDecreasingComparator());
		//System.out.println("FFD");
		for(DataEntry d : dList)
		{
			VMInstance vm = findExistingVMInstance(d,mDev,inf);
			if(vm == null)
			{
				vm = instantiateNewVM(d, mDev, inf);
				vmPlan.add(vm);
			}
			d.setVMInstance(vm);
						
		}
		return vmPlan;
	}
	
	public VMInstance findExistingVMInstance(DataEntry d, MobileDevice mDev, MobileDataDistributionInfrastructure inf) {
		VMInstance targetVM = null;
		ArrayList<VMInstance> instancesForUid = inf.getVMAssignment(mDev.getId());
		if(instancesForUid != null) 
		{
			Collections.sort(instancesForUid, new VMCPUDecreasingComparator());

			for(VMInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements()))
				{
					targetVM = vm;
					break;
				}
		}
		return targetVM;
	}

	public VMInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,VMInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<VMInstance> repoInst = new ArrayList<VMInstance>(repo.values());
		Collections.sort(repoInst, new VMCPUDecreasingComparator());
		VMInstance targetVM = null;
		for(VMInstance vm : repoInst) 
		{
			if(vm.isCompatible(d)) 
			{
				targetVM = vm.clone();
				break;
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (VMInstance) targetVM.clone());
		return targetVM;
	} 

}
