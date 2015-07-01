import scala.concurrent.Future
import akka.actor._
import scala.collection.immutable.{ Queue ⇒ Q }
import scala.concurrent.duration._
import scala.util.{Success, Failure}
 
// A simple actor that prints whatever it receives
class PrintActor extends Actor {
  def receive = {
    case x ⇒ println(x)
  }
}


object LeakyBucketThrottler {
  case object Tick
  
  // Messages, as we queue them to be sent later
  final case class Message(message: Any, sender: ActorRef)
  
  final case class SetTarget(optTarget: Option[ActorRef])
}

/**
  * A throttling actor implementing the Leaky Bucket algorithm
  * @param restorePeriod the rate at which request quota increases over time, up to the maximum request quota 
  * @param burstRate maximum request quota, how many requests are allowed at once
  */
class LeakyBucketThrottler(var restorePeriod: FiniteDuration, var burstRate: Int) extends ActorWithTimer {
  import LeakyBucketThrottler._
  
  var requestQuota = burstRate //keeps track of current request quota
  var optTarget: Option[ActorRef] = None
  var messageQueue: Q[Message] = Q()
  private val restoreTimer = "restoreTimer"
  
  def receive: Receive = {
	case _: Tick.type => 
	  if (!messageQueue.isEmpty && optTarget.isDefined) {
		val (msg, newQ) = messageQueue.dequeue
		optTarget.get.tell(msg.message, msg.sender)
		messageQueue = newQ
	  } else if (requestQuota < burstRate) {
		requestQuota += 1
	  } else {
		log.debug("Quota fully restored!")
		cancelTimer(restoreTimer)
	  }
	case SetTarget(newOptTarget) => optTarget = newOptTarget
	case msg if optTarget.isDefined =>
	  if (requestQuota > 0) {
		optTarget.get.tell(msg, context.sender)
		requestQuota -= 1
		if (requestQuota == 0) setTimer(restoreTimer, Tick, restorePeriod, true)
	  } else {
		messageQueue = messageQueue enqueue Message(msg, context.sender)
	  }
	case msg => messageQueue = messageQueue enqueue Message(msg, context.sender)
  }
}
 
    

object Throttler extends App {
  import LeakyBucketThrottler._
  
  val system = ActorSystem()
  //implicit val execCtx = system.dispatcher
  
  val printer = system.actorOf(Props[PrintActor])
  // The throttler for this example, setting the rate
  val throttler = system.actorOf(Props(classOf[LeakyBucketThrottler], 3.seconds, 3))
  // Set the target
  throttler ! SetTarget(Some(printer))
  // These three messages will be sent to the target immediately
  throttler ! "1"
  throttler ! "2"
  println("waiting for 3 seconds to avoid throttling")
  Thread.sleep(3000)
  throttler ! "3"
  throttler ! "4"
  // These two will be throttled
  throttler ! "5"
  throttler ! "6"
}
