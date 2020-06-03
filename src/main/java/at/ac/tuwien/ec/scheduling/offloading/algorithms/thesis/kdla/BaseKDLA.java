package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import at.ac.tuwien.ec.sleipnir.thesis.HeuristicSettings;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseKDLA extends ThesisOffloadScheduler {
  public BaseKDLA(MobileApplication A, MobileCloudInfrastructure I) {
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseKDLA(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {
    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      bLevel(msc, currentApp.getTaskDependencies(), currentInfrastructure);
    }
  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;
    double best_sest = Double.MAX_VALUE;

    for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(currTask.getUserId())) {
      if (isValid(scheduling, currTask, cn)) {
        double est =
            CalcUtils.calcEST(
                currTask, scheduling, cn, this.currentApp, this.currentInfrastructure);
        double ebl = calcEbl(currTask, cn, HeuristicSettings.kdla_k);
        double temp_sest = est + ebl;

        if (temp_sest < best_sest && isValid(scheduling, currTask, cn)) {
          target = cn;
          best_sest = temp_sest;
        }
      }
    }

    return target;
  }

  protected abstract double calcEbl(
      MobileSoftwareComponent currTask, ComputationalNode node, int k);

  private double bLevel(
      MobileSoftwareComponent msc,
      DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
      MobileCloudInfrastructure I) {
    if (!msc.isVisited()) {
      msc.setVisited(true);
      double w_computational_cost = CalcUtils.calcAverageComputationalCost(msc, I);

      if (dag.outgoingEdgesOf(msc).isEmpty()) {
        msc.setRank(w_computational_cost);
      } else {
        double maxSRank = 0;

        for (ComponentLink neigh : dag.outgoingEdgesOf(msc)) {
          MobileSoftwareComponent n_succ = neigh.getTarget();
          double c_communication_cost = CalcUtils.calcAverageCommunicationCost(n_succ, I);
          double b_level = bLevel(n_succ, dag, I);

          maxSRank = Math.max(b_level + c_communication_cost, maxSRank);
        }

        msc.setRank(w_computational_cost + maxSRank);
      }
    }
    return msc.getRank();
  }
}
