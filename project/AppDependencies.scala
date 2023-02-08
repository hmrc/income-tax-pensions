import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapPlay28Versions = "7.13.0"

  val compile = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-28"  % bootstrapPlay28Versions,
    "com.fasterxml.jackson.module"  %%  "jackson-module-scala"      % "2.14.2"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapPlay28Versions             % Test,
    "com.typesafe.play"       %% "play-test"                % current                             % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.15"                             % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.0"                            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"                             % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.35.0"                            % "test, it",
    "org.scalamock"           %% "scalamock"                % "5.2.0"                             % Test)
}
