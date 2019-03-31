package at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;

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
	public ArrayList<VMInstance> performVMAllocation(ArrayList<DataEntry> dList, MobileDevice mDev,
			MobileDataDistributionInfrastructure inf) {
		ArrayList<VMInstance> vmPlan = new ArrayList<VMInstance>();
		Collections.sort(dList,new DataSizeDecreasingComparator());
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
		double minCost = Double.MAX_VALUE;
		VMInstance targetVM = null;
		for(VMInstance vm : repo.values()) 
		{
			double tmp = vm.getPricePerSecond();
			if(tmp < minCost && vm.getCapabilities().supports(d.getHardwareRequirements())) 
			{
				minCost = tmp;
				targetVM = vm.clone();
			}
		}
		currentInfrastructure.instantiateVMForUser(mDev.getId(), (VMInstance) targetVM.clone());
		return targetVM;
	} 

}
