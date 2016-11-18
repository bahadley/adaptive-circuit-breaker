package scb.tpcdi

import akka.actor.{Actor, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


class TaxRateProducer extends Actor with ActorLogging {
  import context.dispatcher

  implicit val timeout = Timeout(2 seconds)

  val rnd = new java.util.Random()

  val tick =
    context.system.scheduler.schedule(5 seconds, 500 millis, self, "tick")

  override def postStop() = tick.cancel()

  def receive = {
    case "tick" => 
      ask(context.actorSelection("/user/dataaccess/singleton"), nextTaxRate()) onFailure {
        case e => log.error("Error: {}", e)
      }
  }

   def nextTaxRate(): TaxRate = {
    TaxRate("US1", 
      "U.S. Income Tax Bracket for the poor", 
      rnd.nextDouble())
      //(math floor rnd.nextDouble() * 1E5) / 1E2)
  } 
}
