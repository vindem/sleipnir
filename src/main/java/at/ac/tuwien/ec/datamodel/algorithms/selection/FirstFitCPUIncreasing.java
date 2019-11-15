package at.ac.tuwien.ec.datamodel.algorithms.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.FirstFitDecreasingSizeContainerPlanner.DataSizeDecreasingComparator;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner.VMCPUComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;

public class FirstFitCPUIncreasing implements ContainerPlanner, Serializable{

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
	public ArrayList<ContainerInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<ContainerInstance> vmPlan = new ArrayList<ContainerInstance>();
		Collections.sort(dList,new MIPSRequirementComparator());
		//System.out.println("FF");
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

	public ContainerInstance instantiateNewVM(DataEntry d, MobileDevice mDev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		HashMap<String,ContainerInstance> repo = currentInfrastructure.getVMRepository();
		ArrayList<ContainerInstance> repoInst = new ArrayList<ContainerInstance>(repo.values());
		Collections.sort(repoInst, new VMCPUComparator());
		ContainerInstance targetVM = null;
		for(ContainerInstance vm : repoInst) 
		{
			if(vm.isCompatible(d)) 
			{
				targetVM = vm.clone();
				break;
			}
		}
		currentInfrastructure.instantiateContainerForUser(mDev.getId(), (ContainerInstance) targetVM.clone());
		return targetVM;
	} 
	
}
