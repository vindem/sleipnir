package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseCPOP extends ThesisOffloadScheduler {
  protected final Map<MobileSoftwareComponent, CPOPSoftwareComponentAdapter> mappings =
      new HashMap<>();
  protected List<CPOPSoftwareComponentAdapter> cpList;
  protected ComputationalNode bestNode;

  public BaseCPOP(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseCPOP(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {
    currentApp
        .getTaskDependencies()
        .vertexSet()
        .forEach(
            mobileSoftwareComponent -> {
              mappings.put(
                  mobileSoftwareComponent, new CPOPSoftwareComponentAdapter(mobileSoftwareComponent));
            });

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      upRank(msc, currentApp.getTaskDependencies(), currentInfrastructure);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : currentApp.getTaskDependencies().vertexSet()) {
      downRank(msc, currentApp.getTaskDependencies(), currentInfrastructure);
    }

    mappings
        .values()
        .forEach(
            mscp -> {
              mscp.setPriority(mscp.getRankUp() + mscp.getRankDown());
            });

    /*
    CPOPSoftwareComponentProxy first = this.mappings.get(currentApp.readyTasks().get(0));
    System.out.println(first.getMsc().getId());
    mappings
        .values()
        .forEach(
            mscp -> {
              System.out.println(
                  mscp.getMsc().getId()
                      + ":"
                      + mscp.getPriority()
                      + "[DIFFERENCE: "
                      + (first.getPriority() - mscp.getPriority())
                      + " ]");
            });

     */

    cpList = getCriticalPath();
    bestNode = getBestNode(cpList);
  }

  private List<CPOPSoftwareComponentAdapter> getCriticalPath() {
    CPOPSoftwareComponentAdapter entryNode = this.mappings.get(currentApp.readyTasks().get(0));

    return this.mappings.values().stream()
        .filter(
            mscp -> {
              return Math.abs(entryNode.getPriority() - mscp.getPriority()) < Math.pow(0.1, 10);
            })
        .collect(Collectors.toList());
  }

  private ComputationalNode getBestNode(List<CPOPSoftwareComponentAdapter> cpList) {
    ComputationalNode bestNode = null;
    double bestTime = Double.MAX_VALUE;
    for (ComputationalNode node : currentInfrastructure.getAllNodes()) {
      double result =
          cpList.stream()
              .reduce(
                  0.0,
                  (time, mscp) -> {
                    return time + mscp.getMsc().getRuntimeOnNode(node, currentInfrastructure);
                  },
                  Double::sum);

      if (result < bestTime) {
        bestTime = result;
        bestNode = node;
      }
    }

    return bestNode;
  }

  private double upRank(
      MobileSoftwareComponent msc,
      DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
      MobileCloudInfrastructure I) {
    if (!msc.isVisited()) {
      msc.setVisited(true);
      double w_computational_cost = CalcUtils.calcAverageComputationalCost(msc, I);

      if (dag.outgoingEdgesOf(msc).isEmpty()) {
        mappings.get(msc).setRankUp(w_computational_cost);
      } else {
        double maxSRank = 0;

        for (ComponentLink neigh : dag.outgoingEdgesOf(msc)) {
          MobileSoftwareComponent n_succ = neigh.getTarget();
          double c_communication_cost =
              CalcUtils.calcAverageCommunicationCost(n_succ, I.getNodeById(msc.getUserId()), I);
          double n_rank_up = upRank(n_succ, dag, I);

          maxSRank = Math.max(n_rank_up + c_communication_cost, maxSRank);
        }

        mappings.get(msc).setRankUp(w_computational_cost + maxSRank);
      }
    }
    return mappings.get(msc).getRankUp();
  }

  private double downRank(
      MobileSoftwareComponent msc,
      DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
      MobileCloudInfrastructure I) {
    if (!msc.isVisited()) {
      msc.setVisited(true);

      if (dag.incomingEdgesOf(msc).isEmpty()) {
        mappings.get(msc).setRankDown(0);
      } else {
        double maxSRank = 0;

        for (ComponentLink neigh : dag.incomingEdgesOf(msc)) {
          MobileSoftwareComponent n_pred = neigh.getSource();

          double w_computational_cost = CalcUtils.calcAverageComputationalCost(n_pred, I);
          double c_communication_cost =
              CalcUtils.calcAverageCommunicationCost(msc, I.getNodeById(n_pred.getUserId()), I);
          double n_rank_down = downRank(n_pred, dag, I);

          maxSRank = Math.max(n_rank_down + w_computational_cost + c_communication_cost, maxSRank);
        }

        mappings.get(msc).setRankDown(maxSRank);
      }
    }
    return mappings.get(msc).getRankDown();
  }
}
