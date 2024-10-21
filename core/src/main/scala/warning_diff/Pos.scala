package warning_diff

import xsbti.Position

case class Pos(
  line: Option[Int],
  lineContent: String,
  offset: Option[Int],
  pointer: Option[Int],
  pointerSpace: Option[String],
  sourcePath: Option[String],
  startOffset: Option[Int],
  endOffset: Option[Int],
  startLine: Option[Int],
  startColumn: Option[Int],
  endLine: Option[Int],
  endColumn: Option[Int]
) extends PosCompat

object Pos {

  private[this] def j2s[A](j: java.util.Optional[A]): Option[A] =
    if (j.isPresent) Option(j.get) else None

  private[this] def j2sInt(j: java.util.Optional[Integer]): Option[Int] =
    if (j.isPresent) Option(j.get.asInstanceOf[Int]) else None

  def fromSbt(p: Position): Pos = Pos(
    line = j2sInt(p.line()),
    lineContent = p.lineContent(),
    offset = j2sInt(p.offset()),
    pointer = j2sInt(p.pointer()),
    pointerSpace = j2s(p.pointerSpace()),
    sourcePath = j2s(p.sourcePath()),
    startOffset = j2sInt(p.startOffset()),
    endOffset = j2sInt(p.endOffset()),
    startLine = j2sInt(p.startLine()),
    startColumn = j2sInt(p.startColumn()),
    endLine = j2sInt(p.endLine()),
    endColumn = j2sInt(p.endColumn())
  )
}
