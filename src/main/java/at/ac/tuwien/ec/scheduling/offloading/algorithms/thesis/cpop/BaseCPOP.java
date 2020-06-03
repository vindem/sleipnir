package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils.CalcUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseCPOP extends ThesisOffloadScheduler {
  protected final Map<MobileSoftwareComponent, CPOPSoftwareComponentAdapter> mappings =
      new HashMap<>();
  protected final Map<String, List<MobileSoftwareComponent>> criticalPath = new HashMap<>();
  protected final Map<String, ComputationalNode> bestNode = new HashMap<>();

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
                  mobileSoftwareComponent,
                  new CPOPSoftwareComponentAdapter(mobileSoftwareComponent));
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
    CPOPSoftwareComponentAdapter first = this.mappings.get(currentApp.readyTasks().get(0));
    System.out.println(first.getMsc().getId());
    mappings
        .values()
        .forEach(
            mscp -> {
              System.out.println(
                  mscp.getMsc().getId()
                      + ":"
                      + mscp.getRankUp()
                      + " + "
                      + mscp.getRankDown()
                      + " = "
                      + mscp.getPriority()
                      + "[DIFFERENCE: "
                      + (first.getPriority() - mscp.getPriority())
                      + " ]");
            });
    */

    initCriticalPaths();
    initBestNodes();
  }

  private void initCriticalPaths() {
    currentApp
        .readyTasks()
        .forEach(
            msc -> {
              String userId = msc.getUserId();
              CPOPSoftwareComponentAdapter entryTask = this.mappings.get(msc);

              if (!criticalPath.containsKey(userId)
                  || this.mappings.get(this.criticalPath.get(userId).get(0)).getPriority()
                      < entryTask.getPriority()) {
                List<MobileSoftwareComponent> path = new ArrayList<>();

                CPOPSoftwareComponentAdapter currentTask = entryTask;
                while (currentTask != null) {
                  Optional<ComponentLink> nextLink =
                      this.currentApp.getOutgoingEdgesFrom(currentTask.getMsc()).stream()
                          .filter(
                              next -> {
                                return Math.abs(
                                        entryTask.getPriority()
                                            - this.mappings.get(next.getTarget()).getPriority())
                                    < Math.pow(0.1, 10);
                              })
                          .findFirst();

                  if (nextLink.isPresent()) {
                    MobileSoftwareComponent node = nextLink.get().getTarget();
                    path.add(node);
                    currentTask = this.mappings.get(node);
                  } else {
                    break;
                  }
                }

                criticalPath.put(userId, path);
              }
            });
  }

  protected abstract void initBestNodes();

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
          double c_communication_cost = CalcUtils.calcAverageCommunicationCost(n_succ, I);
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
          double c_communication_cost = CalcUtils.calcAverageCommunicationCost(msc, I);
          double n_rank_down = downRank(n_pred, dag, I);

          maxSRank = Math.max(n_rank_down + w_computational_cost + c_communication_cost, maxSRank);
        }

        mappings.get(msc).setRankDown(maxSRank);
      }
    }
    return mappings.get(msc).getRankDown();
  }
}
