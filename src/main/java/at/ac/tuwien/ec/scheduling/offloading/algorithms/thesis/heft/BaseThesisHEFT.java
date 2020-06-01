package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.heft;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseThesisHEFT extends ThesisOffloadScheduler {
  public BaseThesisHEFT(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseThesisHEFT(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {
    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      upRank(msc, currentApp.getTaskDependencies(), currentInfrastructure);
    }
  }

  private double upRank(
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
              CalcUtils.calcAverageCommunicationCost(n_succ, I);
          double n_rank_up = upRank(n_succ, dag, I);

          maxSRank = Math.max(n_rank_up + c_communication_cost, maxSRank);
        }

        msc.setRank(w_computational_cost + maxSRank);
      }
    }
    return msc.getRank();
  }
}
