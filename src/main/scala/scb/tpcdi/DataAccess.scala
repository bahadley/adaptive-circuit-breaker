package scb.tpcdi 

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.language.postfixOps
import slick.driver.H2Driver.api._


class DataAccess extends Actor with ActorLogging {
  import context.dispatcher

  var db = None : Option[Database]

  val cb =
    new CircuitBreaker(
      context.system.scheduler,
      maxFailures = 5,
      callTimeout = 2.second,
      resetTimeout = 1.minute)
        .onOpen(notifyOpen())
        .onHalfOpen(notifyHalfOpen())
        .onClose(notifyClose())

  override def preStart(): Unit = {
    db = Some(Database.forConfig("tpcdi", ConfigFactory.load()))
    super.preStart()
  }

  override def postStop(): Unit = {
    db match {
      case None => throw new NullPointerException(ERR_MSG_DB)
      case Some(db) => db.close 
    }
    super.postStop()
  }

  def receive = {
    case msg: TaxRate =>
      db match {
        case None => 
          throw new NullPointerException(ERR_MSG_DB) 
        case Some(db) => 
          cb.guard(db.run(insertTx(msg))) pipeTo sender()
      }
    case _ =>
  }

  def insertTx(tx: TaxRate): DBIO[Int] =
    sqlu"""insert into taxrate (tx_id, tx_name, tx_rate)
      values (${tx.tx_id}, ${tx.tx_name}, ${tx.tx_rate})"""

  def notifyOpen(): Unit =
    log.warning("CircuitBreaker is now open")

  def notifyHalfOpen(): Unit =
    log.warning("CircuitBreaker is now half open")

  def notifyClose(): Unit =
    log.warning("CircuitBreaker is now closed")

  val ERR_MSG_DB = "Connection pool is uninitialized" 
}
