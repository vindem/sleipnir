package at.ac.tuwien.ec.datamodel.algorithms.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUIncreasing.MIPSRequirementComparator;
import at.ac.tuwien.ec.datamodel.algorithms.selection.VMPlanner.VMCPUComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;

public class FirstFitCPUDecreasing implements VMPlanner, Serializable{

	class MIPSRequirementDecreasingComparator implements Comparator<DataEntry>, Serializable
	{

		@Override
		public int compare(DataEntry o1, DataEntry o2) {
			return (int) (Double.compare(o2.getMillionsOfInstruction(), o1.getMillionsOfInstruction()));
		}
		
	}
	
	class VMCPUDecreasingComparator implements Comparator<ContainerInstance>
	{

		@Override
		public int compare(ContainerInstance o1, ContainerInstance o2) {
			return (int) Double.compare(o2.getCapabilities().getMipsPerCore()*o2.getCapabilities().getAvailableCores()
					,o1.getCapabilities().getMipsPerCore()*o1.getCapabilities().getAvailableCores());
		}
		
	}
	
	@Override
	public ArrayList<ContainerInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<ContainerInstance> vmPlan = new ArrayList<ContainerInstance>();
		Collections.sort(dList,new MIPSRequirementDecreasingComparator());
		//System.out.println("FFD");
		for(DataEntry d : dList)
		{
			ContainerInstance vm = findExistingVMInstance(d,mDev,inf);
			if(vm == null)
			{
				vm = instantiateNewVM(d, mDev, inf);
				vmPlan.add(vm);
			}
			d.setVMInstance(vm);
						
		}
		return vmPlan;
	}
	
	public ContainerInstance findExistingVMInstance(DataEntry d, MobileDevice mDev, MobileDataDistributionInfrastructure inf) {
		ContainerInstance targetVM = null;
		ArrayList<ContainerInstance> instancesForUid = inf.getVMAssignment(mDev.getId());
		if(instancesForUid != null) 
		{
			Collections.sort(instancesForUid, new VMCPUDecreasingComparator());

			for(ContainerInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements()))
				{
					targetVM = vm;
					break;
				}
		}
		return targetVM;
	}

	public ContainerInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,ContainerInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<ContainerInstance> repoInst = new ArrayList<ContainerInstance>(repo.values());
		Collections.sort(repoInst, new VMCPUDecreasingComparator());
		ContainerInstance targetVM = null;
		for(ContainerInstance vm : repoInst) 
		{
			if(vm.isCompatible(d)) 
			{
				targetVM = vm.clone();
				break;
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (ContainerInstance) targetVM.clone());
		return targetVM;
	} 

}
