# 5 严格求值和惰性求值

- map, filter, fold, zip都有输入参数，并且输出一个新构造的列表
- 都会产生中间列表，之后立即被丢弃
- 希望融合成单个函数避免产生临时数据结构
- 使用非严格求值(non-strictness)

## 5.1 严格和非严格函数
> 非严格求值是函数的一种属性，称一个函数是非严格求值的意思是这个函数可以选择不对它的一个或多个参数求值。相反，一个严格求值的函数总是对它的参数求值。严格求值函数在大部分编程语言中是一种常态。并且大部分编程语言也只支持严格求值。在Scala中除非明确声明，否则任何函数都是严格求值的（到目前为止我们所定义过的函数都是严格的）。

例如
```scala
def square(x: Double): Double = x * x
square(41.0 + 1.0) // 接收42.0
square(sys.error("failure")) // 先得到异常，因为sys.error先求值
```

- &&和||就属于非严格求值
- if对条件函数严格求值，对分支参数非严格，只求满足条件的那一个参数。即使if true的时候抛出异常，也不代表一定会抛出异常。
- 表达式未求值形式称为thunk
- 可以通过接收某个未求值的参数（通过函数实现）来写非严格求值函数
例子
```scala
def if2[A](cond: Boolean, onTrue: () => A, onFalse: () => A): A = 
  if (cond) onTrue() else onFalse()
if (a < 22, () => println("a"), () => println("b"))
```
- () => A表示接收0个参数返回A类型。
- scala语法糖
```scala
def if2[A](cond: Boolean, onTrue: => A, onFalse: => A): A = 
  if (cond) onTrue() else onFalse()
if (false, sys.error("fail"), 3) // 输出Int = 3
```
- 未求值参数有一个箭头=>在前，这样在方法内部不需要做任何事情，就可以包装为thunk
- 在方法体中引用的地方会被求值一次，默认不会缓存一个参数的求值结果。也就是引用多少次就会计算多少次。比如：
```scala
// 代码块被计算了两次
def maybeTwice(b: Boolean, i: => int) = if(b) i+i else 0
val x = maybeTwice(true, {println("hi"); 1+41})
// hi
// hi
// 84
```
- 如果希望只求一次，需要通过lazy关键字显示缓存，比如：
```scala
def maybeTwice(b: Boolean, i: => int) = {
  // 通过显示缓存，只执行一次
  lazy val j = i
  if(b) j+j else 0
}
val x = maybeTwice(true, {println("hi"); 1+41})
// hi
// 84
```
> 对一个val声明的变量添加lazy修饰符，将导致Scala延迟对这个变量求值，直到它第一次被引用的时候。它也会缓存结果，在后续引用的地方不会触发重复求值。

> Scala中非严格求值的函数接收的参数是传名参数（by name）而非传值参数（by value）


## 5.3 把函数的描述与求值分离
> 函数式编程的主题之一是关注分离（separation of concerns）。我们希望将计算的描述与实际运行分开。

> 注意这些实现是增量的（incremental）——它们不会生成完全的答案，直到某些其他计算看到由实际计算中生成的作为结果的Stream中的元素之后，才可以生成想要的元素。因为这种增量性质，我们可以一个接一个地调用这些函数而不用对中间结果实例化（fully instantiating）。

> Stream转换的增量性质对内存使用也有重要的影响。因为不再生成中间Stream，转换只需要处理当前元素所够用的内存。


## 5.4 无限流与共递归
- 具有增量性质的函数也适用于无限流
- 但是必须小心，很容易写出永不结束或者线程栈不安全的表达式