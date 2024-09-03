package warning_diff.rdf

case class Diagnostic(
  message: String,
  location: Location,
  severity: Option[Severity],
  source: Option[Source],
  code: Option[Code],
  suggestions: Seq[Suggestion],
  original_output: Option[String],
  related_locations: Seq[RelatedLocation]
)
