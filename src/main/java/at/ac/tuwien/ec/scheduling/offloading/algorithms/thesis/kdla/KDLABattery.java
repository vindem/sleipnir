package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import at.ac.tuwien.ec.sleipnir.thesis.HeuristicSettings;
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
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;

    double best_sest = Double.MAX_VALUE;
    for (ComputationalNode cn : this.currentInfrastructure.getAllNodes()) {
      double est =
          CalcUtils.calcEST(currTask, scheduling, cn, this.currentApp, this.currentInfrastructure);
      double ebl = calcEbl(currTask, cn, HeuristicSettings.kdla_k);
      double temp_sest = est + ebl;

      if (temp_sest < best_sest && isValid(scheduling, currTask, cn)) {
        target = cn;
        best_sest = temp_sest;
      }
    }

    ComputationalNode userNode =
        (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
    double est =
        CalcUtils.calcEST(
            currTask, scheduling, userNode, this.currentApp, this.currentInfrastructure);
    double ebl = calcEbl(currTask, userNode, HeuristicSettings.kdla_k);
    double temp_sest = est + ebl;

    if (temp_sest < best_sest) {
      target = userNode;
    }

    return target;
  }

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
