
# 6 纯函数式状态

## 6.1 以副作用方式生成随机数
- 每一次随机数都会发生变化，那么内部可能存在状态。这方法就不是引用透明的。难以测试，组合。即使传入seed，seed本身也是一个状态。

## 6.2 纯函数式随机数生成器
```scala
trait RNG{
  def nextInt: (Int, RNG)
}
```
- 每一次返回新的状态，但是老的状态不变。这样即可维持引用透明
- 调用相同的RNG状态的nextInt总会返回相同的值

## 6.3 用纯函数式实现带状态的API
> 将带状态的API改造为纯函数式风格遇到的问题和解决办法（让API计算下一个状态而不改变任何值）不是随机数生成独有的，它是一个频繁发生的问题，我们可以用相同的方式来处理。不论何时使用这一模式，让调用者负责传递计算后的下一状态给剩余的程序。

## 6.4 状态行为的更好的API
> 通用模式：我们的每一个函数都有一个形式为RNG=＞(A, RNG)的类型（存在某种A类型）。这种类型的函数被称为状态行为（state action）或状态转移。因为它们把RNG从一个状态转换到另一个状态。状态行为可以通过组合子来组合，组合子是一个高阶函数（会在本章定义）。既然要一直乏味地重复传递状态参数，何不用组合子帮我们在行为之间自动传递状态。

```scala
// 可以给RNG状态行为定义一个类型别名
type Rand[+A] = RNG => (A, RNG)
val int: Rand[Int] = _.nextInt
```

> 我们想写一个组合子让我们能组合Rand行为，避免显式地传递RNG状态。我们将以某种特定领域语言（DSL）来帮我们实现自动传递。比如，一个简单的RNG状态转换是unit行为，这个行为只传递RNG状态，它总是返回一个常量值（unit）而非随机数。

## 6.5 更通用的状态行为数据类型
> 我们写过的函数——unit、map、map2、flatMap和sequence都不是专门为随机数使用的。它们都是处理状态行为的通用函数，不关心状态类型。注意这一点，比如map不关心它处理的RNG状态行为，我们可以给它一个更通用的签名
```scala
def map[S, A, B](a: S => (A, S))(f: A => B): S => (B, S)
type State[S, A] = S => (A, S)
```
>这里State是对“携带状态的计算”或“状态行为”“状态转换”，甚至是“指令”（statement）的缩写

## 6.6 纯函数式命令编程
> 函数式编程是没有副作用的程序，而命令式编程是关于使用指令（state-ment）修改程序状态的程序，如我们所见，无副作用的维护状态也是完全合理的。函数式编程对写命令式程序有很好的支持，并带来额外的好处。例如程序可以被等式推理，因为它们是引用透明的。