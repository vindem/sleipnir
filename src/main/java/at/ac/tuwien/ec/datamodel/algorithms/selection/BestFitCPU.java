package at.ac.tuwien.ec.datamodel.algorithms.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUDecreasing.VMCPUDecreasingComparator;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitCPUIncreasing.MIPSRequirementComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;

public class BestFitCPU implements VMPlanner, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3877922078211276933L;

	class MIPSRequirementDecreasingComparator implements Comparator<DataEntry>, Serializable
	{

		@Override
		public int compare(DataEntry o1, DataEntry o2) {
			return (int) (Double.compare(o2.getMillionsOfInstruction(), o1.getMillionsOfInstruction()));
		}
		
	}
	
	@Override
	public ArrayList<ContainerInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<ContainerInstance> vmPlan = new ArrayList<ContainerInstance>();
		Collections.sort(dList,new MIPSRequirementDecreasingComparator());
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
		double minCPU = Double.MAX_VALUE;
		if(instancesForUid != null) 
		{
			for(ContainerInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements())
						&& vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore() < minCPU)
				{
					targetVM = vm;
					minCPU = vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore();
				}
		}
		return targetVM;
	}

	public ContainerInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,ContainerInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<ContainerInstance> repoInst = new ArrayList<ContainerInstance>(repo.values());
		ContainerInstance targetVM = null;
		double minCPU = Double.MAX_VALUE;
		for(ContainerInstance vm : repoInst) 
		{
			if(vm.getCapabilities().supports(d.getHardwareRequirements())
					&& vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore() < minCPU)
			{
				targetVM = vm.clone();
				minCPU = vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore();
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (ContainerInstance) targetVM.clone());
		return targetVM;
	} 
	
}
