object Currying {

  // 接受一个f(a, b) = c形式的函数
  // 返回一个f(a) = g(b) => c的函数，也就是柯里化
  def curry[A,B,C](f:(A, B) => C): A => (B => C) = {
    (a : A) => (b : B) => f(a, b)
  }

  // 接受一个f(a) = g(b) => c形式的函数
  // 返回一个f(a, b) = c的函数，也就是反柯里化
  def uncurry[A, B, C](f: A => B => C): (A, B) => C ={
    (a : A, b : B) => f(a)(b)
  }

  def main(args: Array[String]): Unit = {
    def f1 = (a : Int, b : Int) => a % b
    def f2 = (a:Int) => (b:Int) => Math.pow(a, b)

    // 输出的是一个函数
    println(curry[Int, Int, Int](f1)(20))
    println(curry[Int, Int, Int](f1)(20)(3))
    println(uncurry[Int, Int, Double](f2)(13, 3))
  }

}
