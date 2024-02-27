/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion  = "8.4.0"
  private val flexmarkVersion    = "0.64.6"

  val compile = Seq(
    "uk.gov.hmrc"                               %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"           % "2.14.2",
    "org.typelevel"                              %% "cats-core"                                % "2.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                     %% "bootstrap-test-play-30"   % bootstrapVersion % Test,
    "org.playframework"          %% "play-test"                           % "3.0.1"                   % Test,
    "org.scalatest"                    %% "scalatest"                           % "3.2.15"                 % Test,
    "com.vladsch.flexmark"     % "flexmark-all"                          % flexmarkVersion  % "test, it",
    "org.scalatestplus.play"     %% "scalatestplus-play"            % "5.1.0"                  % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8-standalone" % "2.35.1"                % "test, it",
    "org.scalamock"                 %% "scalamock"                         % "5.2.0"                  % Test
  )
}
