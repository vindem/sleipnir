package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.heft;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import scala.Tuple2;

public class ThesisHEFTRuntime extends BaseThesisHEFT {
  public ThesisHEFTRuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public ThesisHEFTRuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;

    double tMin = Double.MAX_VALUE;

    for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
      double eft = CalcUtils.calcEFT(currTask, scheduling, cn, currentApp, currentInfrastructure);
      if (eft < tMin && isValid(scheduling, currTask, cn)) {
        tMin = eft;
        target = cn;
      }
    }

    ComputationalNode userNode =
        (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
    double eft = CalcUtils.calcEFT(currTask, scheduling, userNode, currentApp, currentInfrastructure);

    if (eft < tMin
        && isValid(scheduling, currTask, userNode)) {
      target = userNode;
    }

    return target;
  }
}
