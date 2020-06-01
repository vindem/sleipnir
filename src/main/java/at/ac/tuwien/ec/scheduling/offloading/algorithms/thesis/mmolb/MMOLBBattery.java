package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import java.util.HashMap;
import java.util.List;
import scala.Tuple2;

public class MMOLBBattery extends BaseMMOLB {
  public MMOLBBattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public MMOLBBattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcAssignmentValue(OffloadScheduling scheduling, MobileSoftwareComponent msc, ComputationalNode node) {
    return CalcUtils.calcEFT(msc, scheduling, node, this.currentApp, this.currentInfrastructure);
  }

  @Override
  protected double calcNewAssignmentValue(
      ComputationalNode node,
      HashMap<ComputationalNode, List<Integer>> assignments,
      MobileSoftwareComponent[] tasks) {
    return assignments.get(node).stream()
        .reduce(0.0, (time, index) -> time + tasks[index].getRunTime(), Double::sum);
  }
}
