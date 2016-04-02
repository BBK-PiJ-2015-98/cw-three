package com.mildlyskilled

import akka.actor.{ActorLogging, Actor}

class Coordinator extends Actor with ActorLogging {

  import Coordinator._

  private def init(im: Image, of: String) = {
    image = im
    outfile = of
    waiting = im.width * im.height
  }

  // Number of pixels we're waiting for to be set.
  private var waiting = 0
  private var outfile: String = null
  private var image: Image = null

  private def set(x: Int, y: Int, c: Colour) = {
    image(x, y) = c
    waiting -= 1
  }

  private def print = {
    assert(waiting == 0)
    image.print(outfile)
  }

  override def receive: Receive = {
    case config: Initialize => {
      log.debug("Initializing Coordinator...")
      init(config.im, config.of)
      sender ! InitializeFinished
    }
    case pixel: Pixel => {
      log.debug(s"Writing $pixel")
      set(pixel.x, pixel.y, pixel.colour)
    }
    case Print => {
      log.debug("Saving generated image...")
      print
      sender ! PrintFinished
    }
  }
}

object Coordinator {
  trait CoordinatorCommand
  case class Initialize(im: Image, of: String) extends CoordinatorCommand
  case object InitializeFinished extends CoordinatorCommand
  case object Print extends CoordinatorCommand
  case object PrintFinished extends CoordinatorCommand
}
