package com.mildlyskilled

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}

import com.mildlyskilled.Coordinator.Print
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike, FunSuite}
import org.scalatest.mockito.MockitoSugar

class CoordinatorSupervisorSpec extends TestKit(ActorSystem("CoordinatorSupervisorSpec")) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A CoordinatorSupervisor" must {
    "handle errors" when {
      "received a Pixel message but not initialized" in new CoordinatorWithSupervisorActorContext {
        val pixelMock = mock[Pixel]
        underTest ! pixelMock
        expectMsgPF() {
          case Terminated(`underTest`) => ()
        }
      }

      "received a Print message but not initialized" in new CoordinatorWithSupervisorActorContext {
        underTest ! Print
        expectMsgPF() {
          case Terminated(`underTest`) => ()
        }
      }
    }

    "be able to shut down Coordinator" in new CoordinatorWithSupervisorActorContext {
      underTest ! PoisonPill
      expectMsgPF() {
        case Terminated(`underTest`) => ()
      }
    }
  }

  trait CoordinatorWithSupervisorActorContext extends MockitoSugar {
    val supervisor = system.actorOf(Props[CoordinatorSupervisor])
    supervisor ! Props[Coordinator]
    val underTest = expectMsgType[ActorRef]
    watch(underTest)
  }
}
