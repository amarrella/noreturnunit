/*
rule = Noreturnunit
*/
package fix

trait Noreturnunit {

  def foo: Unit// assert: Noreturnunit.ReturnError

  val bar: Unit// assert: Noreturnunit.ReturnError

  def baz {// assert: Noreturnunit.ReturnError
    println("I return unit")
  }

  def qux = {// assert: Noreturnunit.ReturnError
    println("I return unit too")
  }

  def main(args: Array[String]): Unit = {
    println("Leave me alone")
  }
}
