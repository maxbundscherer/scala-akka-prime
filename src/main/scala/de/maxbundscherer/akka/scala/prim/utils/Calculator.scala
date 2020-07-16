package de.maxbundscherer.akka.scala.prim.utils

object Calculator {

  /**
   * Calc * about vector
   * @param values e.g. 1,2,3
   * @return e.g. 1 * 2 * 3
   */
  @Deprecated
  def calcTo(values: Vector[Int]): Int = {
    var result = 1
    values.foreach(value => result = result * value)
    result
  }

  /**
   * Simple check if value isPrime
   * @param n value
   * @return isPrime
   */
  def isPrime(n: Int): Boolean = ! ((2 until n-1) exists (n % _ == 0))

}