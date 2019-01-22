/*
rule = Noreturnunit
*/
package fix

trait Noreturnunit {

  def foo: Unit

  val bar: Unit

  def baz {
    println("I return unit")
  }

  def qux = {
    println("I return unit too")
  }

  def main(args: Array[String]): Unit = {
    println("Leave me alone")
  }
}
