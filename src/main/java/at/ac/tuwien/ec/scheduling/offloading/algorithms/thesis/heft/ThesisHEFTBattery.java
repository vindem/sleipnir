package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.heft;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

public class ThesisHEFTBattery extends BaseThesisHEFT {
  public ThesisHEFTBattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public ThesisHEFTBattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;

    double minEnergy = Double.MAX_VALUE;

    for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(currTask.getUserId())) {
      if (isValid(scheduling, currTask, cn)) {
        double energy;
        if (currentInfrastructure.getMobileDevices().containsValue(cn)) {
          energy =
              cn.getCPUEnergyModel().computeCPUEnergy(currTask, cn, currentInfrastructure)
                  * currTask.getLocalRuntimeOnNode(cn, currentInfrastructure);
        } else {
          energy =
              currentInfrastructure
                      .getNodeById(currTask.getUserId())
                      .getNetEnergyModel()
                      .computeNETEnergy(currTask, cn, currentInfrastructure)
                  * currentInfrastructure.getTransmissionTime(
                      currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
        }

        if (energy < minEnergy) {
          minEnergy = energy;
          target = cn;
        }
      }
    }

    return target;
  }
}
