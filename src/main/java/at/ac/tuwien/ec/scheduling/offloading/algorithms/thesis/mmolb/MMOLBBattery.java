package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

public class MMOLBBattery extends BaseMMOLB {
  public MMOLBBattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public MMOLBBattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcAssignmentValue(
      OffloadScheduling scheduling, MobileSoftwareComponent currTask, MMOLBComputationalNode mmolbcn) {
    ComputationalNode cn = mmolbcn.getNode();
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
