package com.mildlyskilled

import akka.actor.{Props, Actor}

class CoordinatorSupervisor extends Actor {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Escalate
    }

  def receive = {
    case props: Props => sender ! context.actorOf(props)
  }
}
