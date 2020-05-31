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
    ComputationalNode target = null;

    if (cpList.contains(mappings.get(currTask))) {
      if (isValid(scheduling, currTask, bestNode)) {
        target = bestNode;
      }
    } else {
      double minEnergy = Double.MAX_VALUE;

      for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
        double offloadEnergy =
            currentInfrastructure
                    .getNodeById(currTask.getUserId())
                    .getNetEnergyModel()
                    .computeNETEnergy(currTask, cn, currentInfrastructure)
                * currentInfrastructure.getTransmissionTime(
                    currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
        if (offloadEnergy < minEnergy && isValid(scheduling, currTask, cn)) {
          minEnergy = offloadEnergy;
          target = cn;
        }
      }

      ComputationalNode userNode =
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      double mobileEnergy =
          userNode.getCPUEnergyModel().computeCPUEnergy(currTask, userNode, currentInfrastructure)
              * currTask.getLocalRuntimeOnNode(userNode, currentInfrastructure);

      if (mobileEnergy < minEnergy
          && isValid(
              scheduling,
              currTask,
              (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()))) {
        target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      }
    }

    return target;
  }
}
