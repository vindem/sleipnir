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

public class KDLABattery extends BaseKDLA {

  public KDLABattery(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public KDLABattery(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected double calcEbl(MobileSoftwareComponent currTask, ComputationalNode node, int k) {
    double b_energy = 0;

    if (node == currentInfrastructure.getNodeById(currTask.getUserId())) {
      ComputationalNode userNode =
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      b_energy =
          userNode.getCPUEnergyModel().computeCPUEnergy(currTask, userNode, currentInfrastructure)
              * currTask.getLocalRuntimeOnNode(userNode, currentInfrastructure);
    } else {
      b_energy =
          currentInfrastructure
                  .getNodeById(currTask.getUserId())
                  .getNetEnergyModel()
                  .computeNETEnergy(currTask, node, currentInfrastructure)
              * currentInfrastructure.getTransmissionTime(
                  currTask, currentInfrastructure.getNodeById(currTask.getUserId()), node);
    }

    double c_avg_communication_cost =
        CalcUtils.calcAverageCommunicationCost(currTask, this.currentInfrastructure);

    ArrayList<ComponentLink> task_successors = this.currentApp.getOutgoingEdgesFrom(currTask);
    Set<NetworkConnection> node_neighbours = this.currentInfrastructure.getOutgoingLinksFrom(node);

    double result =
        b_energy
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
