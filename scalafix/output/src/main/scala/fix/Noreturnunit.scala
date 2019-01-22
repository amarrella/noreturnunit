package fix

import cats.effect.IO
trait Noreturnunit {

  def foo: IO[Unit]

  val bar: IO[Unit]

  def baz = IO {
    println("I return unit")
  }

  def qux = IO {
    println("I return unit too")
  }

  def main(args: Array[String]): Unit = {
    println("Leave me alone")
  }
}
