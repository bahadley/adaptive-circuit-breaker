package scb.tpcdi

import akka.actor.{Actor, Address, FSM}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension, NodeMetrics}
import akka.cluster.metrics.StandardMetrics.Cpu


object Resource {
  sealed trait State
  case object Healthy extends State
  case object Unhealthy extends State

  case class CPU(d : Double)
} 

class AdmissionControl extends Actor with FSM[Resource.State, Resource.CPU] {
  import Resource._

  val cluster = Cluster(context.system)
  val extension = ClusterMetricsExtension(context.system)

  val extResourceRole = "external-resource"
  var extResourceAddr = None: Option[Address]
     
  startWith(Healthy, CPU(0.0))
  when(Healthy) {
    case Event(MemberUp(member), data: CPU) => 
      if(member.roles.contains(extResourceRole)) {
        extResourceAddr = Some(member.address)
        log.info("Member with role '{}' is Up: {}", extResourceRole, member.address)
      }
      stay
    case Event(ClusterMetricsChanged(clusterMetrics), data: CPU) => 
      extResourceAddr match {
        case Some(extResourceAddr) => {
          clusterMetrics.filter(_.address == extResourceAddr) foreach { nodeMetrics => 
            logCpu(nodeMetrics)
          }
        }
        case None => // No external resource node is up.
      }
      if (data.d > 0.0) {
        goto(Unhealthy)
      }
      else {
        stay
      }
  }
  when(Unhealthy) { 
    case Event(ClusterMetricsChanged(clusterMetrics), data: CPU) => 
      stay
  }
  initialize()

  override def preStart(): Unit = {
    extension.subscribe(self)

    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent])
  }

  override def postStop(): Unit = extension.unsubscribe(self)

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.info("Address: {} Load: {} ({} processors)", address, systemLoadAverage, processors)
    case _ => log.debug("No cpu info in NodeMetrics")
  }
}
