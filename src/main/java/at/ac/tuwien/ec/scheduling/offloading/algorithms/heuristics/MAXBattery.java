package at.ac.tuwien.ec.scheduling.algorithms.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import scala.Tuple2;



public class MAXBattery extends HeuristicResearch {
	
	public MAXBattery(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
	}
	
	public MAXBattery(Tuple2<MobileApplication,MobileCloudInfrastructure> t)
	{
		super();
		setMobileApplication(t._1);
		setInfrastructure(t._2);
	}

	public ComputationalNode findTarget(OffloadScheduling deployment, MobileSoftwareComponent msc) {
		ComputationalNode target = null;
		if(!msc.isOffloadable())
		{
			if(isValid(deployment,msc,currentInfrastructure.getMobileDevices().get(msc.getUserId())))
				return currentInfrastructure.getMobileDevices().get(msc.getUserId());
			else
				return null;
		}
		else
		{
			double currBattery = Double.MIN_VALUE, tmpBattery;
			for(ComputationalNode cn : currentInfrastructure.getAllNodes())
			{
				if(!isValid(deployment,msc,cn))
					continue;
				else
				{
					
					if(currentInfrastructure.getMobileDevices().containsValue(cn))
						tmpBattery = cn.getCPUEnergyModel().computeCPUEnergy(msc, cn, currentInfrastructure);
					else
						tmpBattery = currentInfrastructure.getMobileDevices().get(msc.getUserId()).
							getNetEnergyModel().computeNETEnergy(msc, cn, currentInfrastructure);
					if(tmpBattery > currBattery)
					{
						target = cn;
						currBattery = tmpBattery;
					}
				}
			}

			return target;						
		}

	}


	
}
