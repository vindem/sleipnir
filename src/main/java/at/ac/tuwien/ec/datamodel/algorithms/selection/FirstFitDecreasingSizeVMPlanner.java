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

public class FirstFitDecreasingSizeVMPlanner implements VMPlanner,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4748429963179361944L;

	class DataSizeDecreasingComparator implements Comparator<DataEntry>, Serializable
	{

		@Override
		public int compare(DataEntry o1, DataEntry o2) {
			return (int) ((o2.getInData() + o2.getOutData()) - (o1.getInData() + o1.getOutData()));
		}
		
	}
	
	@Override
	public ArrayList<ContainerInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<ContainerInstance> vmPlan = new ArrayList<ContainerInstance>();
		Collections.sort(dList,new DataSizeDecreasingComparator());
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
		double minCost = Double.MAX_VALUE;
		ContainerInstance targetVM = null;
		for(ContainerInstance vm : repo.values()) 
		{
			double tmp = vm.getPricePerSecond();
			if(tmp < minCost && vm.getCapabilities().supports(d.getHardwareRequirements())) 
			{
				minCost = tmp;
				targetVM = vm.clone();
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (ContainerInstance) targetVM.clone());
		return targetVM;
	} 

}
