package at.ac.tuwien.ec.provisioning;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Timezone;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.energy.AMDCPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.CloudFixedPricingModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class DefaultCloudPlanner {
	
	static HardwareCapabilities defaultCloudNodesCapabilities = OffloadingSetup.defaultCloudCapabilities;
			
	static PricingModel defaultCloudPricindModel = new CloudFixedPricingModel();
	static Timezone[] defaultTimezones = 
		{
				Timezone.DETROIT, Timezone.INDIANAPOLIS, Timezone.DUBLIN, Timezone.STGHISLAIN, Timezone.SINGAPORE, Timezone.KOREA				
		};
	static CPUEnergyModel defaultCPUEnergyModel = new AMDCPUEnergyModel();
	
	public static void setupCloudNodes(MobileCloudInfrastructure inf, int cloudNum) 
	{
		for(int i = 0; i < cloudNum; i++)
		{
			CloudDataCenter cdc = new CloudDataCenter("cloud_"+i, 
					defaultCloudNodesCapabilities, 
					defaultCloudPricindModel);
			cdc.setCoords(defaultTimezones[i%defaultTimezones.length]);
			cdc.setCPUEnergyModel(defaultCPUEnergyModel);
			inf.addCloudDataCenter(cdc);
		}
			
	}

}
