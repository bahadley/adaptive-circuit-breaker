package scb.tpcdi

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension, NodeMetrics}
import akka.cluster.metrics.StandardMetrics.Cpu
     

class PerfMon extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val extension = ClusterMetricsExtension(context.system)

  val extResourceRole = "external-resource"
  var extResourceAddr = None: Option[Address]
     
  override def preStart(): Unit = {
    extension.subscribe(self)

    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent])
  }
     
  def receive = {
    case MemberUp(member) => {
      if(member.roles.contains(extResourceRole)) {
        extResourceAddr = Some(member.address)
        log.info("Member with role '{}' is Up: {}", extResourceRole, member.address)
      }
    }

    case ClusterMetricsChanged(clusterMetrics) =>
      extResourceAddr match {
        case Some(extResourceAddr) => {
          clusterMetrics.filter(_.address == extResourceAddr) foreach { nodeMetrics => 
            logCpu(nodeMetrics)
          }
        }
        case None => // No external resource node is up.
      }
  }
     
  override def postStop(): Unit = extension.unsubscribe(self)

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.info("Address: {} Load: {} ({} processors)", address, systemLoadAverage, processors)
    case _ => log.debug("No cpu info in NodeMetrics")
  }
}
