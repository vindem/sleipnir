package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import scala.Tuple2;

public class CPOPRuntime extends BaseCPOP {
  public CPOPRuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public CPOPRuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
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
      double tMin = Double.MAX_VALUE;

      for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(currTask.getUserId())) {
        if (isValid(scheduling, currTask, cn)) {
          double eft = CalcUtils.calcEFT(currTask, scheduling, cn, currentApp, currentInfrastructure);
          if (eft < tMin) {
            tMin = eft;
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
          double bestTime = Double.MAX_VALUE;
          ComputationalNode node = null;

          for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(userId)) {
            double result =
                value.stream()
                    .reduce(
                        0.0,
                        (time, msc) -> {
                          return time + msc.getRuntimeOnNode(cn, currentInfrastructure);
                        },
                        Double::sum);

            if (result < bestTime) {
              bestTime = result;
              node = cn;
            }
          }

          this.bestNode.put(userId, node);
        });
  }

}
