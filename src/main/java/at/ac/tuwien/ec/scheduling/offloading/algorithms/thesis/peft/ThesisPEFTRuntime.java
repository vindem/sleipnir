package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.peft;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import scala.Tuple2;

public class ThesisPEFTRuntime extends BaseThesisPEFT {

  public ThesisPEFTRuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public ThesisPEFTRuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  public ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;
    double OeftMin = Double.MAX_VALUE, tMin = Double.MAX_VALUE;

    for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
      double eft =
          CalcUtils.calcEFT(currTask, scheduling, cn, this.currentApp, this.currentInfrastructure);
      double o_eft =
          eft
              + OCT[currentApp.getTaskIndex(currTask)][
                  currentInfrastructure.getAllNodes().indexOf(cn)];

      if (o_eft < OeftMin && isValid(scheduling, currTask, cn)) {
        tMin = eft;
        OeftMin = o_eft;
        target = cn;
      }
    }

    ComputationalNode userNode =
        (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
    double eft =
        CalcUtils.calcEFT(currTask, scheduling, userNode, currentApp, currentInfrastructure);

    if (eft < tMin && isValid(scheduling, currTask, userNode)) {
      target = userNode;
    }

    return target;
  }
}
