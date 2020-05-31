package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseKDLA extends ThesisOffloadScheduler {
  public BaseKDLA(MobileApplication A, MobileCloudInfrastructure I) {
    setMobileApplication(A);
    setInfrastructure(I);
    setRanks();
  }

  public BaseKDLA(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  private void setRanks() {
    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      bLevel(msc, currentApp.getTaskDependencies(), currentInfrastructure);
    }
  }

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
          double c_communication_cost =
              CalcUtils.calcAverageCommunicationCost(n_succ, I.getNodeById(msc.getUserId()), I);
          double b_level = bLevel(n_succ, dag, I);

          maxSRank = Math.max(b_level + c_communication_cost, maxSRank);
        }

        msc.setRank(w_computational_cost + maxSRank);
      }
    }
    return msc.getRank();
  }
}
