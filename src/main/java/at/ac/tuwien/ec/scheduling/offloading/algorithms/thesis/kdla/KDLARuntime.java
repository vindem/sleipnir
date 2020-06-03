package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import java.util.ArrayList;
import java.util.Set;
import scala.Tuple2;

public class KDLARuntime extends BaseKDLA {

  public KDLARuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public KDLARuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcEbl(MobileSoftwareComponent msc, ComputationalNode node, int k) {
    double w_computation_cost = msc.getRuntimeOnNode(node, this.currentInfrastructure);
    double c_avg_communication_cost =
        CalcUtils.calcAverageCommunicationCost(msc, this.currentInfrastructure);

    ArrayList<ComponentLink> task_successors = this.currentApp.getOutgoingEdgesFrom(msc);
    Set<NetworkConnection> node_neighbours = this.currentInfrastructure.getOutgoingLinksFrom(node);

    double result =
        w_computation_cost
            + c_avg_communication_cost
                * Math.ceil(task_successors.size() / (node_neighbours.size() * 1.0));
    if (k > 1 && task_successors.size() > 0) {
      double max =
          task_successors.stream()
              .map(n -> calcEbl(n.getTarget(), node, k - 1))
              .max(Double::compareTo)
              .get();
      result += max;
    }

    return result;
  }
}
