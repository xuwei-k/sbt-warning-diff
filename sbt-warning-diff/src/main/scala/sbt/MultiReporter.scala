package sbt

import xsbti.Position
import xsbti.Problem
import xsbti.Reporter

final class MultiReporter(mainReporter: Reporter, consumeOnly: Reporter) extends Reporter {

  override def reset(): Unit = {
    mainReporter.reset()
    consumeOnly.reset()
  }

  override def hasErrors: Boolean = mainReporter.hasErrors

  override def hasWarnings: Boolean = mainReporter.hasWarnings

  override def printSummary(): Unit = mainReporter.printSummary()

  override def problems(): Array[Problem] = mainReporter.problems()

  override def log(problem: Problem): Unit = {
    mainReporter.log(problem)
    consumeOnly.log(problem)
  }

  override def comment(pos: Position, msg: String): Unit = {
    mainReporter.comment(pos, msg)
    consumeOnly.comment(pos, msg)
  }

}
