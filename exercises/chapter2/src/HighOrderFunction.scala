object HighOrderFunction {

  def compose[A, B, C](f: B=>C, g:A=>B):A=>C = a => f(g(a))

  def main(args: Array[String]): Unit = {
    def f = (x:Int) => (2 * x)
    def g = (x:Int) => (5 * x)

    println(compose(f, g)(10))
  }

}
