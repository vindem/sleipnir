package at.ac.tuwien.ec.scheduling.offloading.algorithms.cpopbased;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class CPOPSoftwareComponentProxy {
  private double rankUp = 0;
  private double rankDown = 0;
  private final MobileSoftwareComponent msc;

  public CPOPSoftwareComponentProxy(
      MobileSoftwareComponent msc) {
    this.msc = msc;
  }

  public double getRankDown() {
    return rankDown;
  }

  public void setRankDown(double rankDown) {
    this.rankDown = rankDown;
  }

  public double getRankUp() {
    return rankUp;
  }

  public void setRankUp(double rankUp) {
    this.rankUp = rankUp;
  }

  public MobileSoftwareComponent getMsc() {
    return this.msc;
  }

  public void setPriority(double priority) {
    this.msc.setRank(priority);
  }

  public double getPriority() {
    return this.msc.getRank();
  }

  @Override
  public String toString() {
    return "CPOPSoftwareComponentProxy{" + this.msc.getId() + "}";
  }
}
