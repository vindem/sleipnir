package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

// TODO: Doesnt work like expected the algorithm would fail for it!
public class MMOLBBattery extends BaseMMOLB {
  public MMOLBBattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public MMOLBBattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcAssignmentValue(
      OffloadScheduling scheduling, MobileSoftwareComponent currTask, ComputationalNode cn) {
    if (this.currentInfrastructure.getMobileDevices().containsValue(cn)) {
      ComputationalNode userNode =
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      return userNode
              .getCPUEnergyModel()
              .computeCPUEnergy(currTask, userNode, currentInfrastructure)
          * currTask.getLocalRuntimeOnNode(userNode, currentInfrastructure);

    } else {
      return currentInfrastructure
              .getNodeById(currTask.getUserId())
              .getNetEnergyModel()
              .computeNETEnergy(currTask, cn, currentInfrastructure)
          * currentInfrastructure.getTransmissionTime(
              currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
    }
  }
}
