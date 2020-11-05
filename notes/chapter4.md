# 4 不是用异常来处理错误

- 抛出异常会产生副作用
> 我们可以用普通的值表现失败和异常，可以通过高阶函数抽象出错误处理和恢复的常用模式。在函数式解决方案中，以值的方式返回错误更安全，符合引用透明，并且可以通过高阶函数保存异常的优点——统一错误处理逻辑。

## 4.1 异常的优点与劣势
- 引用透明性需要看表达式是否依赖上下文。比如例子:
  ```scala
  val y : Int = throw new Exception("fail")
  try{
    x + y
    }
  catch {case e : Exception => 43}
  ```
- 不满足引用透明。因为y不可以用throw new Exception代替，如果代替的话就会被catch。
- 上述可得异常破坏了引用透明并引入了上下文依赖
- 异常不是类型安全。不知道可能发生什么杨的异常
- 高阶函数不可能感知参数引起的特定异常
- 但是还是需要整合集中的错误处理逻辑
- 引入一种新的泛型类型来描述可能存在定义的值
  - 完全类型安全
  - 得到类型检测的帮助

## 4.2 异常的其他选择
- 传统的伪造异常值的缺点
  - 允许错误“无声”传播
  - 导致代码量放大
  - 不适用于多态（泛型）
  - 很难传递给高阶函数
- 让调用者提供参数（类似default）来指导处理
  - 没有中止运算或者分支的选择

## 4.3 Option数据类型
> 解决方案是在返回值类型时明确表示该函数并不总是有答案。可以认为这是用于推迟调用者的错误处理策略。我们引入一种新的类型Option。

- Option是一个最多只包含一个元素的List

<img src="https://madusudanan.com/images/scala-option.png">

引用自：
[Scala Tutorials Part #16 - The Option type | Madusudanan](https://madusudanan.com/blog/scala-tutorials-part-16-the-option-type/)

> 如果成功的结果不匹配给定的预判，可以用filter将成功的结果转换成失败的结果。一个通用的模式是调用map、flatMap和（或）filter来转换Option，在最后用getOrElse来处理错误

> orElse与getOrElse相似，不同之处在于如果第一个Option没有定义将返回另一个。当我们需要将可能的失败串联起来时，如果第一个失败尝试第二个，这会很有用。

> 一个常见的用法是o.getOrElse(throw new Exception(＂FAIL＂))。将结果是None的Option转回一个异常。一般的经验法则是在没有合理的方案能捕获异常时将其抛出；如果异常是一种可恢复的错误，使用Option（或Either）会更加灵活。

- 使用Option并不意味着将所有函数底层重写
  - Option的map函数是将A=>B函数应用在Option[A]，然后返回Option[B]，也就是说map是将一个A=>B的函数转化成Option[A]=>Option[B]，所以可以定义一个lift = _ map f，用来将普通函数转换成Option函数
  - 对于函数f来说，None就映射为None，Some就应用f，所以不需要感知Option

