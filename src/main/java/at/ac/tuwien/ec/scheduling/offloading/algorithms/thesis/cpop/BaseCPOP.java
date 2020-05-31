package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.FinishTimeComparator;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedAcyclicGraph;
import scala.Tuple2;

public abstract class BaseCPOP extends ThesisOffloadScheduler {
  protected final Map<MobileSoftwareComponent, CPOPSoftwareComponentProxy> mappings =
      new HashMap<>();
  protected List<CPOPSoftwareComponentProxy> cpList;
  protected ComputationalNode bestNode;

  public BaseCPOP(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);

    setRank(this.currentApp, this.currentInfrastructure);
    cpList = getCriticalPath();
    bestNode = getBestNode(cpList);
  }

  public BaseCPOP(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  public ArrayList<OffloadScheduling> findScheduling() {
    double start = System.nanoTime();

    OffloadScheduling scheduling = new OffloadScheduling();
    PriorityQueue<MobileSoftwareComponent> scheduledNodes =
        new PriorityQueue<>(new FinishTimeComparator());

    int totalTaskNum = currentApp.getComponentNum();
    int totalFinishedTasks = 0;
    double currentRuntime = 0;

    while (totalFinishedTasks < totalTaskNum) {
      if (!scheduledNodes.isEmpty()) {
        MobileSoftwareComponent finishedTask = scheduledNodes.remove();
        currentApp.removeEdgesFrom(finishedTask);
        currentApp.removeTask(finishedTask);

        ((ComputationalNode) scheduling.get(finishedTask)).undeploy(finishedTask);
        currentRuntime += finishedTask.getRunTime();
        totalFinishedTasks++;
      }

      MobileSoftwareComponent currTask;
      PriorityQueue<MobileSoftwareComponent> readyTasks =
          new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator()) {
            {
              addAll(
                  currentApp.readyTasks().stream()
                      .filter(rt -> !scheduledNodes.contains(rt))
                      .collect(Collectors.toSet()));
            }
          };

      if (readyTasks.isEmpty() && !scheduledNodes.isEmpty()) {
        continue;
      }

      while ((currTask = readyTasks.poll()) != null) {
        ComputationalNode target = findTarget(currTask, scheduling);

        if (target == null) {
          break;
        }

        deploy(currentRuntime, scheduling, currTask, target);
        scheduledNodes.add(currTask);
      }
    }

    double end = System.nanoTime();
    scheduling.setExecutionTime(end - start);
    ArrayList<OffloadScheduling> result = new ArrayList<>();
    result.add(scheduling);
    return result;
  }

  protected abstract ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling);

  protected double calcEST(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling, ComputationalNode cn) {
    double readyTime = Double.MIN_VALUE;

    for (MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask)) {
      double otherReadyTime =
          cmp.getFinishTime()
              + currentInfrastructure.getTransmissionTime(cmp, scheduling.get(cmp), cn);
      if (otherReadyTime > readyTime) {
        readyTime = cmp.getRunTime();
      }
    }

    double avail = 0;
    SoftwareComponent sc = cn.getDeployedComponent();
    if (sc != null) {
      avail = ((MobileSoftwareComponent) sc).getFinishTime();
    }

    return Math.max(avail, readyTime);
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
