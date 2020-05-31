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

    if (cpList.contains(mappings.get(currTask))) {
      if (isValid(scheduling, currTask, bestNode)) {
        target = bestNode;
      }
    } else {
      double tMin = Double.MAX_VALUE;

      for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
        double est =
            CalcUtils.calcEST(
                currTask, scheduling, cn, this.currentApp, this.currentInfrastructure);
        double w = currTask.getRuntimeOnNode(cn, currentInfrastructure);
        double time = est + w;

        if (time < tMin && isValid(scheduling, currTask, cn)) {
          tMin = time;
          target = cn;
        }
      }

      ComputationalNode userNode =
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      double est =
          CalcUtils.calcEST(
              currTask, scheduling, userNode, this.currentApp, this.currentInfrastructure);
      if (est + currTask.getRuntimeOnNode(userNode, currentInfrastructure) < tMin
          && isValid(scheduling, currTask, userNode)) {
        target = userNode;
      }
    }

    return target;
  }
}
