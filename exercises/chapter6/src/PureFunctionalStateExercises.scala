object PureFunctionalStateExercises {

  trait RNG {
    def nextInt: (Int, RNG)
    // Should generate a random `Int`. We'll later define other functions in terms of `nextInt`.
  }

  object RNG {
    // NB - this was called SimpleRNG in the book text

    case class Simple(seed: Long) extends RNG {
      def nextInt: (Int, RNG) = {
        // 用当前种子生成新种子
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
        // 下一个状态，用新种子创建RNG实例
        val nextRNG = Simple(newSeed)
        // 右位移产生新的伪随机整数
        val n = (newSeed >>> 16).toInt
        // 返回二元元组，包含随机整数和下一个RNG状态
        (n, nextRNG)
      }
    }

    // 泛指任何一种函数会从RNG生成(a, RNG)
    type Rand[+A] = RNG => (A, RNG)

    val int: Rand[Int] = _.nextInt

    // 这个行为值传递RNG状态，总是返回一个常量值而非随机数
    def unit[A](a: A): Rand[A] =
      rng => (a, rng)

    // 用于转换状态行为的输出而不修改状态自身
    def map[A,B](s: Rand[A])(f: A => B): Rand[B] =
      rng => {
        val (a, rng2) = s(rng)
        (f(a), rng2)
      }

    // -MinValue比MaxValue大1，所以map的时候需要小心一点。
    def nonNegativeInt(rng: RNG): (Int, RNG) =
      {
        val (i, r) = rng.nextInt
        (if (i < 0) -(i + 1) else i, r)
      }

    def nonNegativeEven : Rand[Int] = map(nonNegativeInt)(i => i - i % 2)


    // 因为不包含1，所以分母要+1，但是又不能溢出，所以要先toDouble
    def double(rng: RNG): (Double, RNG) =
    {
      val (i, r) = nonNegativeInt(rng)
      (i / (Int.MaxValue.toDouble + 1), r)
    }

    def doubleViaMap(rng: RNG): Rand[Double] =
      map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))

    def intDouble(rng: RNG): ((Int,Double), RNG) =
      {
        val (i, r1) = rng.nextInt
        val (d, r2) = double(r1)
        ((i, d), r2)
      }

    def doubleInt(rng: RNG): ((Double,Int), RNG) =
      {
        val ((i, d), r) = intDouble(rng)
        ((d, i), r)
      }

    def double3(rng: RNG): ((Double,Double,Double), RNG) =
      {
        val (d1, r1) = double(rng)
        val (d2, r2) = double(r1)
        val (d3, r3) = double(r2)
        ((d1, d2, d3), r3)
      }

    def ints(count: Int)(rng: RNG): (List[Int], RNG) = {
      @annotation.tailrec
      def go(count: Int, r: RNG, xs: List[Int]): (List[Int], RNG) = count match {
        case n if n < 0 => throw sys.error("less than 0")
        case 0 => (xs, r)
        case n => {
          val (i, nextr) = r.nextInt
          go(n - 1, nextr, i :: xs)
        }
      }
      go(count, rng, List())
//      val buf = scala.collection.mutable.ListBuffer.empty[Int]
//      var tempr = rng
//      for(i <- (1 to count)) {
//        val (x, r) = tempr.nextInt
//        buf += x
//        tempr = r
//      }
//      (buf.toList, tempr)
    }

    def map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
      rng => {
        val (a, rng1) = ra(rng)
        val (b, rng2) = rb(rng1)
        (f(a, b), rng2)
    }

    // 将生成A和生成B类型值的函数结合，生成(A, B)
    def both[A, B](ra: Rand[A], rb: Rand[B]):Rand[(A, B)] = map2(ra, rb)((_, _))

    def randIntDouble: Rand[(Int, Double)] = both(int, double)

    val randDoubleint: Rand[(Double, Int)] = both(double, int)

    // foldRight方向正确。foldLeft需要加reverse
    def sequence[A](fs: List[Rand[A]]): Rand[List[A]] =
      fs.foldRight(unit(List[A]()))((f, acc) => map2(f, acc)(_ :: _))

    def _ints(count: Int): Rand[List[Int]] =
      sequence(List.fill(count)(int))

    def flatMap[A,B](f: Rand[A])(g: A => Rand[B]): Rand[B] = rng => {
      val (a, r1) = f(rng)
      g(a)(r1) // We pass the new state along
    }

    def nonNegativeLessThan(n: Int): Rand[Int] = {
      flatMap(nonNegativeInt) { i =>
        val mod = i % n
        if (i + (n-1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
      }
    }

    def _map[A,B](s: Rand[A])(f: A => B): Rand[B] =
      flatMap(s)(a => unit(f(a)))

    def _map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
      flatMap(ra)(a => map(rb)(b => f(a, b)))
  }

  case class State[S, +A](run: S => (A, S)) {
    def map[B](f: A => B): State[S, B] =
      flatMap(a => State.unit(f(a)))
    def map2[B,C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      flatMap(a => sb.map(b => f(a, b)))
    def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
      val (a, s1) = run(s)
      f(a).run(s1)
    })
  }

  sealed trait Input
  case object Coin extends Input
  case object Turn extends Input

  case class Machine(locked: Boolean, candies: Int, coins: Int)

  object State {

    def unit[S, A](a: A): State[S, A] =
      State(s => (a, s))

    type Rand[A] = State[RNG, A]
    def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] = ???
  }
}
