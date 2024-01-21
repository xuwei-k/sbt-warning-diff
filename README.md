# sbt-warning-diff plugin

[![Latest version](https://index.scala-lang.org/xuwei-k/sbt-warning-diff/sbt-warning-diff/latest.svg)](https://index.scala-lang.org/xuwei-k/sbt-warning-diff/artifacts/sbt-warning-diff)

show added/removed warnings in CI

## Setup

### plugins.sbt

scala compiler warnings only

```scala
addSbtPlugin("com.github.xuwei-k" % "sbt-warning-diff" % "version")
```

scala compiler warnings and scalafix warnings

```scala
addSbtPlugin("com.github.xuwei-k" % "warning-diff-scalafix-plugin" % "version")
```

### GitHub Actions example

https://github.com/xuwei-k/sbt-warning-diff/blob/49693f3e270f87e76/.github/workflows/ci.yml#L29-L70
