package scb.tpcdi 

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.Cluster
import akka.cluster.metrics.ClusterMetricsExtension
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val system = ActorSystem("tpcdi", ConfigFactory.load())

  val roles = system.settings.config.getStringList("akka.cluster.roles")

  if(roles.contains("da")) {
    Cluster(system).registerOnMemberUp {
      system.actorOf(
        ClusterSingletonManager.props(
          singletonProps = Props(classOf[MetricsListener]),
          terminationMessage = PoisonPill,
          settings = ClusterSingletonManagerSettings(system).withRole("da")),
        name = "metrics-listener")
    }
  }
}

