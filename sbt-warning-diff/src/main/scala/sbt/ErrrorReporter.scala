package sbt

import xsbti.Severity

final class ErrrorReporter extends xsbti.Reporter {
  private val lock = new Object

  private val buffer = collection.mutable.ArrayBuffer.empty[xsbti.Problem]

  def reset(): Unit = lock.synchronized {
    buffer.clear()
  }

  def hasErrors: Boolean = lock.synchronized {
    buffer.exists(_.severity == Severity.Error)
  }

  def hasWarnings: Boolean = lock.synchronized {
    buffer.exists(_.severity == Severity.Warn)
  }

  def printSummary(): Unit = ()

  def problems: Array[xsbti.Problem] = lock.synchronized {
    buffer.toArray
  }

  def log(problem: xsbti.Problem): Unit = lock.synchronized {
    buffer.append(problem)
  }

  def comment(pos: xsbti.Position, msg: String): Unit = ()
}
