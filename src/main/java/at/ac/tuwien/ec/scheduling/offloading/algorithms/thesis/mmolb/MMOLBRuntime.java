package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import scala.Tuple2;

public class MMOLBRuntime extends BaseMMOLB {
  public MMOLBRuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public MMOLBRuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcAssignmentValue(OffloadScheduling scheduling, MobileSoftwareComponent currTask, ComputationalNode cn) {
    return CalcUtils.calcEFT(currTask, scheduling, cn, this.currentApp, this.currentInfrastructure);
  }
}
