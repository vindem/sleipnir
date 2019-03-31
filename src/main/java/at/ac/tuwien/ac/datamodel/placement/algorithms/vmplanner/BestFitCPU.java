package at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.FirstFitCPUDecreasing.VMCPUDecreasingComparator;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.FirstFitCPUIncreasing.MIPSRequirementComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;

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
	public ArrayList<VMInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<VMInstance> vmPlan = new ArrayList<VMInstance>();
		Collections.sort(dList,new MIPSRequirementDecreasingComparator());
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
		double minCPU = Double.MAX_VALUE;
		if(instancesForUid != null) 
		{
			for(VMInstance vm : instancesForUid)
				if(vm.getCapabilities().supports(d.getHardwareRequirements())
						&& vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore() < minCPU)
				{
					targetVM = vm;
					minCPU = vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore();
				}
		}
		return targetVM;
	}

	public VMInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,VMInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<VMInstance> repoInst = new ArrayList<VMInstance>(repo.values());
		VMInstance targetVM = null;
		double minCPU = Double.MAX_VALUE;
		for(VMInstance vm : repoInst) 
		{
			if(vm.getCapabilities().supports(d.getHardwareRequirements())
					&& vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore() < minCPU)
			{
				targetVM = vm.clone();
				minCPU = vm.getCapabilities().getAvailableCores() * vm.getCapabilities().getMipsPerCore();
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (VMInstance) targetVM.clone());
		return targetVM;
	} 
	
}
