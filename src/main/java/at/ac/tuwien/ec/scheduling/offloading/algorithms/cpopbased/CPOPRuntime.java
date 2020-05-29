package at.ac.tuwien.ec.scheduling.offloading.algorithms.cpopbased;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Array;
import scala.Tuple2;

public class CPOPRuntime extends OffloadScheduler {
  private final Map<MobileSoftwareComponent, CPOPSoftwareComponentProxy> mappings = new HashMap<>();

  public CPOPRuntime(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
    setRank(this.currentApp, this.currentInfrastructure);
  }

  public CPOPRuntime(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super();
    setMobileApplication(t._1());
    setInfrastructure(t._2());
    setRank(this.currentApp, this.currentInfrastructure);
  }

  @Override
  public ArrayList<OffloadScheduling> findScheduling() {
    OffloadScheduling scheduling = new OffloadScheduling();
    List<CPOPSoftwareComponentProxy> cpList = getCriticalPath();
    ComputationalNode bestNode = getBestNode(cpList);

    PriorityQueue<MobileSoftwareComponent> scheduledNodes =
        new PriorityQueue<>(new RuntimeComparator());

    int totalTaskNum = currentApp.getComponentNum();

    while (scheduling.size() < totalTaskNum) {
      if (!scheduledNodes.isEmpty()) {
        MobileSoftwareComponent finishedTask = scheduledNodes.remove();
        currentApp.removeEdgesFrom(finishedTask);
        currentApp.removeTask(finishedTask);
        ((ComputationalNode) scheduling.get(finishedTask)).undeploy(finishedTask);
      }

      MobileSoftwareComponent currTask;
      PriorityQueue<MobileSoftwareComponent> readyTasks =
          new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator()) {
            {
              addAll(currentApp.readyTasks().stream().filter(rt -> !scheduledNodes.contains(rt)).collect(
                  Collectors.toSet()));
            }
          };

      if (readyTasks.isEmpty()) {
        if (scheduledNodes.isEmpty()) {
          scheduling = null;
          break;
        } else {
          continue;
        }
      }

      while ((currTask = readyTasks.poll()) != null) {
        ComputationalNode target = findTarget(currTask, scheduling, cpList, bestNode);

        if (target == null) {
          break;
        };
        System.out.println(target.getId() + "->" + currTask.getId());
        deploy(scheduling, currTask, target);
        scheduledNodes.add(currTask);
      }
    }

    ArrayList<OffloadScheduling> result = new ArrayList<>();
    result.add(scheduling);
    return result;
  }

  private ComputationalNode findTarget(
      MobileSoftwareComponent currTask,
      OffloadScheduling scheduling,
      List<CPOPSoftwareComponentProxy> cpList,
      ComputationalNode bestNode) {
    double tMin = Double.MAX_VALUE;
    ComputationalNode target = null;

    if (!currTask.isOffloadable()) {
      if (isValid(
          scheduling,
          currTask,
          (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()))) {
        target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      }

    } else if (cpList.contains(mappings.get(currTask))) {
      if (isValid(scheduling, currTask, bestNode)) {
        target = bestNode;
      }
    } else {
      double maxP = Double.MIN_VALUE;
      for (MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask)) {
        if (cmp.getRunTime() > maxP) {
          maxP = cmp.getRunTime();
        }
      }

      for (ComputationalNode cn : currentInfrastructure.getAllNodes()) {
        if (maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure) < tMin
            && isValid(scheduling, currTask, cn)) {
          tMin = maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure);
          target = cn;
        }
      }

      if (maxP
                  + currTask.getRuntimeOnNode(
                      (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()),
                      currentInfrastructure)
              < tMin
          && isValid(
              scheduling,
              currTask,
              (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()))) {
        target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
      }
    }

    return target;
  }

  private List<CPOPSoftwareComponentProxy> getCriticalPath() {
    CPOPSoftwareComponentProxy entryNode = this.mappings.get(currentApp.readyTasks().get(0));

    return this.mappings.values().stream()
        .filter(
            mscp -> {
              return Math.abs(entryNode.getPriority() - mscp.getPriority()) < Math.pow(0.1, 10);
            })
        .collect(Collectors.toList());
  }

  private ComputationalNode getBestNode(List<CPOPSoftwareComponentProxy> cpList) {
    ComputationalNode bestNode = null;
    double best = Double.MAX_VALUE;
    for (ComputationalNode node : currentInfrastructure.getAllNodes()) {
      double result =
          cpList.stream()
              .reduce(
                  0.0,
                  (time, mscp) -> {
                    return time + mscp.getMsc().getRuntimeOnNode(node, currentInfrastructure);
                  },
                  Double::sum);

      if (result < best) {
        best = result;
        bestNode = node;
      }
    }

    return bestNode;
  }

  private void setRank(MobileApplication A, MobileCloudInfrastructure I) {
    A.getTaskDependencies()
        .vertexSet()
        .forEach(
            mobileSoftwareComponent -> {
              mappings.put(
                  mobileSoftwareComponent, new CPOPSoftwareComponentProxy(mobileSoftwareComponent));
            });

    for (MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet()) {
      upRank(msc, A.getTaskDependencies(), I);
    }

    for (MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet()) {
      msc.setVisited(false);
    }

    for (MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet()) {
      downRank(msc, A.getTaskDependencies(), I);
    }

    mappings
        .values()
        .forEach(
            mscp -> {
              mscp.setPriority(mscp.getRankUp() + mscp.getRankDown());
            });

    CPOPSoftwareComponentProxy first = this.mappings.get(currentApp.readyTasks().get(0));
    System.out.println(first.getMsc().getId());
    mappings
        .values()
        .forEach(
            mscp -> {
              System.out.println(mscp.getMsc().getId() + ":" + mscp.getPriority() + "[DIFFERENCE: "+ (first.getPriority() - mscp.getPriority()) +" ]");
            });
  }

  private double upRank(
      MobileSoftwareComponent msc,
      DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
      MobileCloudInfrastructure I) {
    if (!msc.isVisited()) {
      msc.setVisited(true);
      double w_computational_cost = calcAverageComputationalCost(msc, I);

      if (dag.outgoingEdgesOf(msc).isEmpty()) {
        mappings.get(msc).setRankUp(w_computational_cost);
      } else {
        double maxSRank = 0;

        for (ComponentLink neigh : dag.outgoingEdgesOf(msc)) {
          MobileSoftwareComponent n_succ = neigh.getTarget();
          double c_communication_cost =
              calcAverageCommunicationCost(n_succ, I.getNodeById(msc.getUserId()), I);
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

          double w_computational_cost = calcAverageComputationalCost(n_pred, I);
          double c_communication_cost =
              calcAverageCommunicationCost(msc, I.getNodeById(n_pred.getUserId()), I);
          double n_rank_down = downRank(n_pred, dag, I);

          maxSRank = Math.max(n_rank_down + w_computational_cost + c_communication_cost, maxSRank);
        }

        mappings.get(msc).setRankDown(maxSRank);
      }
    }
    return mappings.get(msc).getRankDown();
  }

  private double calcAverageComputationalCost(
      MobileSoftwareComponent msc, MobileCloudInfrastructure I) {
    double w_computational_cost = 0;
    int numberOfNodes = I.getAllNodes().size() + 1;
    for (ComputationalNode cn : I.getAllNodes()) {
      w_computational_cost += msc.getLocalRuntimeOnNode(cn, I);
    }
    w_computational_cost +=
        msc.getLocalRuntimeOnNode((ComputationalNode) I.getNodeById(msc.getUserId()), I);
    w_computational_cost = w_computational_cost / numberOfNodes;

    return w_computational_cost;
  }

  private double calcAverageCommunicationCost(
      MobileSoftwareComponent msc, NetworkedNode from, MobileCloudInfrastructure I) {
    if (msc.isOffloadable()) {
      double c_communication_cost = 0;
      for (ComputationalNode cn : I.getAllNodes()) {
        c_communication_cost += I.getTransmissionTime(msc, from, cn);
      }
      c_communication_cost = c_communication_cost / (I.getAllNodes().size());

      return c_communication_cost;
    }

    return 0;
  }
}
