package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import scala.Tuple2;

public abstract class BaseMMOLB extends ThesisOffloadScheduler {
  public BaseMMOLB(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseMMOLB(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {}

  @Override
  protected void processReadyTasks(
      double currentRuntime,
      PriorityQueue<MobileSoftwareComponent> readyTasks,
      OffloadScheduling scheduling,
      PriorityQueue<MobileSoftwareComponent> scheduledNodes) {

    if (processNonOffloadableTasks(currentRuntime, readyTasks, scheduling, scheduledNodes)) {

      MobileSoftwareComponent[] tasks =
          readyTasks.stream()
              .filter(MobileSoftwareComponent::isOffloadable)
              .toArray(MobileSoftwareComponent[]::new);

      if (tasks.length > 0) {
        List<MMOLBComputationalNode> allNodes =
            currentInfrastructure.getAllNodes().stream()
                .map(
                    cn -> {
                      return new MMOLBComputationalNode(
                          cn, scheduling, currentApp, currentInfrastructure, this);
                    })
                .collect(Collectors.toList());
        allNodes.addAll(
            currentInfrastructure.getMobileDevices().values().stream()
                .map(
                    cn ->
                        new MMOLBComputationalNode(
                            cn, scheduling, currentApp, currentInfrastructure, this))
                .collect(Collectors.toList()));

        Set<MobileSoftwareComponent> alreadyAssigned = new HashSet<>();

        for (int i = 0; i < tasks.length; i++) {
          MobileSoftwareComponent msc = findMaxTask(tasks, allNodes, alreadyAssigned);
          MMOLBComputationalNode node = findMinNode(msc, allNodes, scheduling);
          if (node == null) {
            return;
          }

          node.add(msc);
          alreadyAssigned.add(msc);
        }

        Set<MMOLBComputationalNode> alreadyChecked = new HashSet<>();
        MMOLBComputationalNode highestCn = findMaxCT(allNodes, alreadyChecked);
        double makespan = highestCn.getMakeSpan();

        while (alreadyChecked.size() != allNodes.size()) {
          List<MobileSoftwareComponent> assignments = highestCn.getAssignments();
          int assigmentSize = assignments.size();
          if (assigmentSize > 0) {
            List<MobileSoftwareComponent> notRemoveAble = new ArrayList<>();

            while (notRemoveAble.size() < assigmentSize) {
              MobileSoftwareComponent msc = highestCn.getMinMsc(notRemoveAble);
              MMOLBComputationalNode maxNode = findMaxNode(msc, allNodes, scheduling);

              if (maxNode.getValue(msc) < makespan) {
                highestCn.remove(msc);
                maxNode.add(msc);
                break;
              } else {
                notRemoveAble.add(msc);
              }
            }
          } else {
            break;
          }

          alreadyChecked.add(highestCn);
          highestCn = findMaxCT(allNodes, alreadyChecked);
          if (highestCn == null) {
            break;
          }
        }

        for (MMOLBComputationalNode node : allNodes) {
          List<MobileSoftwareComponent> assignments = node.getAssignments();
          if (assignments.size() > 0) {
            for (MobileSoftwareComponent msc : assignments) {
              ComputationalNode cn = node.getNode();
              if (isValid(scheduling, msc, cn)) {
                // System.out.println(cn.getId() + "->" + msc.getId());
                deploy(currentRuntime, scheduling, msc, cn);
                scheduledNodes.add(msc);
              }
            }
          }
        }
      }
    }
  }

  protected abstract double calcAssignmentValue(
      OffloadScheduling scheduling, MobileSoftwareComponent currTask, MMOLBComputationalNode mmolbcn);

  private boolean processNonOffloadableTasks(
      double currentRuntime,
      PriorityQueue<MobileSoftwareComponent> readyTasks,
      OffloadScheduling scheduling,
      PriorityQueue<MobileSoftwareComponent> scheduledNodes) {

    MobileSoftwareComponent[] tasks =
        readyTasks.stream().filter(t -> !t.isOffloadable()).toArray(MobileSoftwareComponent[]::new);

    for (MobileSoftwareComponent currTask : tasks) {
      ComputationalNode userNode =
          (ComputationalNode) this.currentInfrastructure.getNodeById(currTask.getUserId());

      if (isValid(scheduling, currTask, userNode)) {
        deploy(currentRuntime, scheduling, currTask, userNode);
        scheduledNodes.add(currTask);
      } else {
        return false;
      }
    }

    return true;
  }

  private MMOLBComputationalNode findMaxCT(
      List<MMOLBComputationalNode> allNodes, Set<MMOLBComputationalNode> alreadyChecked) {
    double makespan = Double.MIN_VALUE;
    MMOLBComputationalNode maxNode = null;

    for (MMOLBComputationalNode node :
        allNodes.stream().filter(n -> !alreadyChecked.contains(n)).collect(Collectors.toList())) {
      double temp = node.getMakeSpan();

      if (makespan < temp) {
        makespan = temp;
        maxNode = node;
      }
    }

    return maxNode;
  }

  private MobileSoftwareComponent findMaxTask(
      MobileSoftwareComponent[] tasks,
      List<MMOLBComputationalNode> allNodes,
      Set<MobileSoftwareComponent> alreadyAssigned) {
    MobileSoftwareComponent msc = null;
    double max = Double.MIN_VALUE;

    for (MobileSoftwareComponent task :
        Arrays.stream(tasks)
            .filter(t -> !alreadyAssigned.contains(t))
            .collect(Collectors.toList())) {
      List<Double> times =
          allNodes.stream()
              .map(n -> n.getValue(task))
              .filter(Double::isFinite)
              .collect(Collectors.toList());

      double temp = Collections.max(times);

      if (max < temp) {
        msc = task;
        max = temp;
      }
    }

    return msc;
  }

  private MMOLBComputationalNode findMinNode(
      MobileSoftwareComponent task,
      List<MMOLBComputationalNode> allNodes,
      OffloadScheduling scheduling) {
    MMOLBComputationalNode cn = null;
    double min = Double.MAX_VALUE;

    for (MMOLBComputationalNode node : allNodes) {
      if (isValid(scheduling, task, node.getNode())) {
        double temp = node.getValue(task);

        if (min > temp) {
          cn = node;
          min = temp;
        }
      }
    }

    return cn;
  }

  private MMOLBComputationalNode findMaxNode(
      MobileSoftwareComponent task,
      List<MMOLBComputationalNode> allNodes,
      OffloadScheduling scheduling) {
    MMOLBComputationalNode cn = null;
    double max = Double.MIN_VALUE;

    for (MMOLBComputationalNode node : allNodes) {
      if (isValid(scheduling, task, node.getNode())) {
        double temp = node.getValue(task);

        if (max < temp) {
          cn = node;
          max = temp;
        }
      }
    }

    return cn;
  }
}
