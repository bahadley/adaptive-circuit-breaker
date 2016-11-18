package scb.tpcdi

import akka.actor.{Actor, ActorLogging, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


class TaxRateProducer extends Actor with ActorLogging {
  import context.dispatcher

  implicit val timeout = Timeout(2 seconds)

  var da = None : Option[ActorSelection] // DataAccess actor with circuit breaker
  val rnd = new java.util.Random()

  case class Tick()
  val ticker =
    context.system.scheduler.schedule(5 seconds, 500 millis, self, Tick())

  override def postStop() = ticker.cancel()

  def receive = {
    case Tick() => 
      da match {
        case None => 
          da = Some(context.actorSelection("/user/dataaccess/singleton")) 
        case Some(da) => 
          ask(da, nextTaxRate()) onFailure {
            case e => log.error("Error: {}", e)
          } 
      }
  }

  def nextTaxRate(): TaxRate = {
    TaxRate("US1", 
      "U.S. Income Tax Bracket for the poor", 
      rnd.nextDouble())
      //(math floor rnd.nextDouble() * 1E5) / 1E2)
  } 
}
