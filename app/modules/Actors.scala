package modules

import javax.inject._
import actors.StatisticsProvider
import akka.actor.ActorSystem
import com.google.inject.AbstractModule

/**
  * Created by david on 17/02/17.
  */
class Actors @Inject()(system: ActorSystem) extends ApplicationActors {
  system.actorOf(
    props = StatisticsProvider.props,
    name = "statisticsProvider"
  )
}

trait ApplicationActors

class ActorsModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationActors])
      .to(classOf[Actors]).asEagerSingleton
  }
}
