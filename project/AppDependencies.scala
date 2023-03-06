import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapPlay28Versions = "7.13.0"
  private val flexmarkVersion = "0.50.30" //Only upgrade after upgrading JDK to Java 11+  "0.36.8" "0.62.2"

  val compile = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-28"  % bootstrapPlay28Versions,
    "com.fasterxml.jackson.module"  %%  "jackson-module-scala"      % "2.14.2",
    "org.typelevel"                 %% "cats-core"                  % "2.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapPlay28Versions             % Test,
    "com.typesafe.play"       %% "play-test"                % current                             % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.15"                            % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % flexmarkVersion                     % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"                             % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.35.0"                            % "test, it",
    "org.scalamock"           %% "scalamock"                % "5.2.0"                             % Test)
}
