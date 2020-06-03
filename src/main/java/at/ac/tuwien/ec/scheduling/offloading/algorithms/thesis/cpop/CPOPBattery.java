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

    if (criticalPath.get(currTask.getUserId()).contains(currTask)) {
      ComputationalNode node = bestNode.get(currTask.getUserId());
      if (isValid(scheduling, currTask, node)) {
        target = node;
      }
    } else {
      double minEnergy = Double.MAX_VALUE;

      for (ComputationalNode cn :
          currentInfrastructure.getAllNodesWithMobile(currTask.getUserId())) {
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
    }

    return target;
  }

  @Override
  protected void initBestNodes() {
    this.criticalPath.forEach(
        (userId, value) -> {
          double bestEnergy = Double.MAX_VALUE;
          ComputationalNode node = null;

          for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(userId)) {
            double result =
                value.stream()
                    .reduce(
                        0.0,
                        (acc, currTask) -> {
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

                          return acc + energy;
                        },
                        Double::sum);

            if (result < bestEnergy) {
              bestEnergy = result;
              node = cn;
            }
          }

          this.bestNode.put(userId, node);
        });
  }
}
