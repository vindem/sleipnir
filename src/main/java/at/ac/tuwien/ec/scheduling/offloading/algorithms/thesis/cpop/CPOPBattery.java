package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

public class CPOPBattery extends BaseCPOP {
  public CPOPBattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public CPOPBattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    double maxB = Double.MAX_VALUE;
    ComputationalNode target = null;

    if(!currTask.isOffloadable())
    {
      if(isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
      {
        target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      }
    }
    else if (cpList.contains(mappings.get(currTask))) {
      if (isValid(scheduling, currTask, bestNode)) {
        target = bestNode;
      }
    }
    else
    {
      if(currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
          currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
              getCPUEnergyModel().computeCPUEnergy(currTask,
              currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure) > maxB
          && isValid(scheduling,currTask,currentInfrastructure.getMobileDevices().get(currTask.getUserId())))
      {
        maxB = currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
            currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
                getCPUEnergyModel().computeCPUEnergy(currTask,
                currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure);
        target = currentInfrastructure.getMobileDevices().get(currTask.getUserId());
      }


      for(ComputationalNode cn : currentInfrastructure.getAllNodes())
        if( currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
            currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
                getCPUEnergyModel().computeCPUEnergy(currTask,
                currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure) > maxB
            &&	isValid(scheduling,currTask,cn))
        {
          maxB = currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
              currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
                  getCPUEnergyModel().computeCPUEnergy(currTask,
                  currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure);
          target = currentInfrastructure.getMobileDevices().get(currTask.getUserId());
        }
    }

    return target;
  }
}
