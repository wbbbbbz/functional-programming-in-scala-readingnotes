object EitherDemo {

  sealed trait Either[+E, +A]{
    def map[B](f: A => B): Either[E, B] = this match {
      case Left(_) => _
      case Right(value) => Right(f(value))
    }

    def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] = this match {
      case Left(_) => _
      case Right(value) => f(value)
    }

    def orElse[EE >: E, AA >: A](b: => Either[EE, AA]): Either[EE, AA] = this match {
      case Left(_) => b
      case _ => _
    }

    def map2[EE >: E, B, C](b: Either[EE, B])(f: (A, B) => C):Either[EE, C] =
//      (this, b) match {
//      case (Left(e), _) => Left(e)
//      case (_, Left(e)) => Left(e)
//      case (Right(v1), Right(v2)) => Right(f(v1, v2))
//    }
//    for {
//      aa <- this
//      bb <- b
//    } yield f(aa, bb)
    {
      this flatMap (aa => b map (bb => f(aa, bb)))
    }

  }
  case class Left[+E](value: E) extends Either[E, Nothing]
  case class Right[+A](value: A) extends Either[Nothing, A]

}
