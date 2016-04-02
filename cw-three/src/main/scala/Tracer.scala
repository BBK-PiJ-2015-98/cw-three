import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.actor.{PoisonPill, ActorRef, Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import com.mildlyskilled.Coordinator.{InitializeFinished, Initialize}
import com.mildlyskilled._

object Tracer extends App {

  val system = ActorSystem("TracerSystem")
  val supervisor = system.actorOf(Props[CoordinatorSupervisor], name = "CoordinatorSupervisorActor")

  val (infile, outfile) = ("src/main/resources/input.dat", "output.png")
  val scene = Scene.fromFile(infile)
  val t = new Trace
  render(scene, outfile, t.Width, t.Height)

  println("rays cast " + t.rayCount)
  println("rays hit " + t.hitCount)
  println("light " + t.lightCount)
  println("dark " + t.darkCount)

  def render(scene: Scene, outfile: String, width: Int, height: Int) = {

    implicit val timeout = Timeout(5 seconds)
    implicit val executionContext = system.dispatcher

    val image = new Image(width, height)

    val createdCoordinator = (supervisor ? Props[Coordinator]).mapTo[ActorRef]
    createdCoordinator.onComplete {
      case Success(coordinatorActor) => {

        implicit val ca = coordinatorActor

        val initialization = (coordinatorActor ? Initialize(image, outfile)).mapTo[Coordinator.CoordinatorCommand]

        initialization.onComplete {
          case Success(result) => result match {
            case InitializeFinished => {
              scene.traceImage(width, height, () => {
                system.terminate().onComplete {
                  case Success(_) => println(s"Finished, see $outfile for the result")
                  case Failure(e) => println(s"Failed with: $e")
                }
              })
            }
            case _ => println("Could not initialize Coordinator")
          }
          case Failure(e) => println(s"Failed with: $e")
        }
      }
      case Failure(e) => println(s"Failed with: $e")
    }
  }
}
