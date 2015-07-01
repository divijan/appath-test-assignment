import akka.actor._
import scala.collection.mutable
import scala.concurrent.duration._

object ActorWithTimer {
  case class Timer(name: String, msg: Any, repeat: Boolean)(context: ActorContext)
    extends NoSerializationVerificationNeeded {
    private var ref: Option[Cancellable] = _
    private val scheduler = context.system.scheduler
    private implicit val executionContext = context.dispatcher

    def schedule(actor: ActorRef, timeout: FiniteDuration): Unit =
      ref = Some(
        if (repeat) scheduler.schedule(timeout, timeout, actor, msg)
        else scheduler.scheduleOnce(timeout, actor, msg))

    def cancel(): Unit =
      if (ref.isDefined) {
        ref.get.cancel()
        ref = None
      }
  }
}

trait ActorWithTimer extends Actor with ActorLogging {
  import ActorWithTimer._
  
  private val timers = mutable.Map[String, Timer]()
  
  /**
   * Schedule named timer to deliver message after given delay, possibly repeating.
   * Any existing timer with the same name will automatically be canceled before
   * adding the new timer.
   * @param name identifier to be used with cancelTimer()
   * @param msg message to be delivered
   * @param timeout delay of first message delivery and between subsequent messages
   * @param repeat send once if false, scheduleAtFixedRate if true
   * @return current state descriptor
   */
  final def setTimer(name: String, msg: Any, timeout: FiniteDuration, repeat: Boolean = false): Unit = {
    log.debug("setting " + (if (repeat) "repeating " else "") + "timer '" + name + "'/" + timeout + ": " + msg)
    if (timers contains name) {
      timers(name).cancel
    }
    val timer = Timer(name, msg, repeat)(context)
    timer.schedule(self, timeout)
    timers(name) = timer
  }

  /**
   * Cancel named timer, ensuring that the message is not subsequently delivered (no race).
   * @param name of the timer to cancel
   */
  final def cancelTimer(name: String): Unit = {
    log.debug("canceling timer '" + name + "'")
    if (timers contains name) {
      timers(name).cancel
      timers -= name
    }
  }
  
  /**
   * Inquire whether the named timer is still active. Returns true unless the
   * timer does not exist, has previously been canceled or if it was a
   * single-shot timer whose message was already received.
   */
  final def isTimerActive(name: String): Boolean = timers contains name
  
  override def postStop(): Unit = {
    for (timer ← timers.values) timer.cancel()
      timers.clear()
    super.postStop()
  }

}
