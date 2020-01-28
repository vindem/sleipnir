package at.ac.tuwien.ec.model.infrastructure.provisioning.workflow;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Timezone;
import at.ac.tuwien.ec.model.availability.AvailabilityModel;
import at.ac.tuwien.ec.model.availability.ConstantAvailabilityModel;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.energy.AMDCPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.CloudFixedPricingModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class WorkflowSchedulingCloudPlanner {

	static HardwareCapabilities defaultCloudNodesCapabilities = FGCSSetup.defaultCloudCapabilities.clone();
	
	static PricingModel defaultCloudPricindModel = new CloudFixedPricingModel();
	static Timezone[] defaultTimezones = 
		{
				Timezone.DETROIT, Timezone.INDIANAPOLIS, Timezone.DUBLIN, Timezone.STGHISLAIN, Timezone.SINGAPORE, Timezone.KOREA				
		};
	static CPUEnergyModel defaultCPUEnergyModel = new AMDCPUEnergyModel();
	static double[] cloudAvailability = {0.999999, 0.999999, 0.99675, 0.996119, 0.99999, 0.99899};	
	public static void setupCloudNodes(MobileCloudInfrastructure inf, int cloudNum) 
	{
		for(int i = 0; i < cloudNum; i++)
		{
			CloudDataCenter cdc = new CloudDataCenter("cloud_"+i, 
					defaultCloudNodesCapabilities.clone(), 
					defaultCloudPricindModel);
			cdc.setCoords(defaultTimezones[i%defaultTimezones.length]);
			cdc.setCPUEnergyModel(defaultCPUEnergyModel);
			cdc.setBandwidth(1000.0);
			cdc.setLatency(300.0);
			AvailabilityModel model = new ConstantAvailabilityModel(cloudAvailability[i%6]);
			cdc.setAvailabilityModel(model);
			inf.addCloudDataCenter(cdc);
		}
			
	}

}
