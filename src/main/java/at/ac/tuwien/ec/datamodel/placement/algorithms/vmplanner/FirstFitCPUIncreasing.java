package at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.FirstFitDecreasingSizeVMPlanner.DataSizeDecreasingComparator;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.VMPlanner.VMCPUComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;

public class FirstFitCPUIncreasing implements VMPlanner, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4444287679807134492L;

	class MIPSRequirementComparator implements Comparator<DataEntry>, Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2143189179495737463L;

		@Override
		public int compare(DataEntry o1, DataEntry o2) {
			return (int) (Double.compare(o1.getMillionsOfInstruction(), o2.getMillionsOfInstruction()));
		}
		
	}
	
	@Override
	public ArrayList<VMInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<VMInstance> vmPlan = new ArrayList<VMInstance>();
		Collections.sort(dList,new MIPSRequirementComparator());
		//System.out.println("FF");
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

	public VMInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,VMInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<VMInstance> repoInst = new ArrayList<VMInstance>(repo.values());
		Collections.sort(repoInst, new VMCPUComparator());
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
