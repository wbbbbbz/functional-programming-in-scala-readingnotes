object Fibonacci {

  // 普通递归
  // 计算48时需要几秒，速度较慢
  def fibonacciRecur(n : Int): BigInt =
  n match {
    case 1 => 0
    case 2 => 1
    case _ => fibonacciRecur(n - 1) + fibonacciRecur(n - 2)
  }


  // 尾递归
  def fibonacciRecur2(n : Int): BigInt ={
    @annotation.tailrec
    def go(n: Int, f0: BigInt, f1: BigInt): BigInt =
    n match {
      case 1 => f0
      case _ => go(n - 1, f1, f1 + f0)
    }
    go(n, 0, 1)
  }

  // 直接计算
  def fibonacciCalc(n : Int) : BigInt = {
    var f0 : BigInt = 0
    var f1 : BigInt = 1
    for(i <- (1 to n)){
      f1 += f0
      f0 = f1 - f0
    }
    f0
  }

  def main(args: Array[String]): Unit = {
    val list = List(5, 23, 48)
    list.foreach(n => println(s"n = ${n}, fib = ${fibonacciCalc(n)}"))
  }

}
