package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
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
    double tMin = Double.MAX_VALUE;
    ComputationalNode target = null;

    if (!currTask.isOffloadable()) {
      if (isValid(
          scheduling,
          currTask,
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()))) {
        target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      }

    } else if (cpList.contains(mappings.get(currTask))) {
      if (isValid(scheduling, currTask, bestNode)) {
        target = bestNode;
      }
    } else {
      double maxP = Double.MIN_VALUE;
      for (MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask)) {
        if (cmp.getRunTime() > maxP) {
          maxP = cmp.getRunTime();
        }
      }

      for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
        if (maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure) < tMin
            && isValid(scheduling, currTask, cn)) {
          tMin = maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure);
          target = cn;
        }
      }

      if (maxP
                  + currTask.getRuntimeOnNode(
                      (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()),
                      currentInfrastructure)
              < tMin
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
