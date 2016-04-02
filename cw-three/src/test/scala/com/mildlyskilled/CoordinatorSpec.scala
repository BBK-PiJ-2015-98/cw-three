package com.mildlyskilled

import akka.actor._
import akka.testkit.{TestKit, ImplicitSender}

import com.mildlyskilled.Coordinator._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CoordinatorSpec extends TestKit(ActorSystem("CoordinatorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Coordinator actor" must {
    "handle successful invocations" when {
      "received an Initialize message" in new CoordinatorActorContext {
        underTest ! Initialize(imageMock, OutFile)
        expectMsg(InitializeFinishedMessage)
      }

      "received a Pixel message" in new CoordinatorActorContext {
        ignoreInitializationResponse()

        underTest ! Initialize(imageMock, OutFile)
        underTest ! Pixel(0, 0, colourMock)

        expectNoMsg()
        verify(imageMock).update(0, 0, colourMock)
      }

      "received a Print message" in new CoordinatorActorContext {
        ignoreInitializationResponse()

        underTest ! Initialize(imageMock, OutFile)
        underTest ! Pixel(0, 0, colourMock)
        underTest ! Pixel(0, 1, colourMock)
        underTest ! Print

        expectMsg(PrintFinished)
        verify(imageMock).print(OutFile)
      }
    }
  }

  trait CoordinatorActorContext extends MockitoSugar {
    val colourMock = mock[Colour]
    val imageMock = mock[Image]

    when(imageMock.width) thenReturn 1
    when(imageMock.height) thenReturn 2

    val InitializeFinishedMessage = InitializeFinished
    val OutFile = ""

    def ignoreInitializationResponse() = {
      ignoreMsg {
        case message => message == InitializeFinishedMessage
      }
    }

    val underTest = system.actorOf(Props[Coordinator])
  }
}
