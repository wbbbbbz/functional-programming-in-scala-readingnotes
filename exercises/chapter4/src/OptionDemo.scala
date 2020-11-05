object OptionDemo {

  sealed trait Option[+A]{
    def map[B](f: A => B): Option[B] = this match {
      case None => None
      case Some(a) => Some(f(a))
    }

    def getOrElse[B>:A](default: => B): B = this match {
      case None => default
      case Some(a) => a
    }

    def flatMap[B](f: A => Option[B]): Option[B] = map(f) getOrElse None

    def flatMap_1[B](f: A => Option[B]): Option[B] = this match {
      case None => None
      case Some(a) => f(a)
    }

    def orElse[B>:A](ob: => Option[B]): Option[B] = this map (Some(_)) getOrElse ob

    def orElse_1[B>:A](ob: => Option[B]): Option[B] = this match {
      case None => ob
      case _ => this
    }

    def filter_1(f: A => Boolean): Option[A] = flatMap(a => if (f(a)) Some(a) else None)

    def filter(f: A => Boolean): Option[A] = this match {
      case Some(a) if f(a) => this
      case _ => None
    }

    def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

  }
  case class Some[+A](get: A) extends Option[A]
  case object None extends Option[Nothing]

}
