/*
 * Copyright 2024 HM Revenue & Customs
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

package testdata

import models.OverseasPensionContribution
import models.database.PaymentsIntoOverseasPensionsStorageAnswers
import models.frontend.OverseasPensionScheme.{MigrantMemberRelief, TransitionalCorrespondingRelief}
import models.frontend.{OverseasPensionScheme, PaymentsIntoOverseasPensionsAnswers}

object paymentsIntoOverseasPensions {

  val paymentsIntoOverseasPensionsAnswers: PaymentsIntoOverseasPensionsAnswers =
    PaymentsIntoOverseasPensionsAnswers(Some(true), Some(2.0), Some(true), Some(false), List(mmrOverseasPensionScheme, tcrOverseasPensionScheme))

  val piopStorageAnswers: PaymentsIntoOverseasPensionsStorageAnswers = PaymentsIntoOverseasPensionsStorageAnswers(Some(true), Some(true), Some(false))

  def mmrOverseasPensionScheme: OverseasPensionScheme = OverseasPensionScheme(
    customerReference = Some("reference"),
    employerPaymentsAmount = Some(100),
    reliefType = Some(MigrantMemberRelief),
    alphaTwoCountryCode = Some("GB"),
    alphaThreeCountryCode = Some("GBR"),
    doubleTaxationArticle = None,
    doubleTaxationTreaty = None,
    doubleTaxationReliefAmount = None,
    qopsReference = Some("Q123456"),
    sf74Reference = None
  )
  def tcrOverseasPensionScheme: OverseasPensionScheme = OverseasPensionScheme(
    customerReference = Some("reference"),
    employerPaymentsAmount = Some(100),
    reliefType = Some(TransitionalCorrespondingRelief),
    alphaTwoCountryCode = Some("GB"),
    alphaThreeCountryCode = Some("GBR"),
    doubleTaxationArticle = None,
    doubleTaxationTreaty = None,
    doubleTaxationReliefAmount = None,
    qopsReference = None,
    sf74Reference = Some("sf74Reference")
  )
  def mmrOverseasPensionContribution: OverseasPensionContribution = mmrOverseasPensionScheme.toOverseasPensionsContributions
  def tcrOverseasPensionContribution: OverseasPensionContribution = tcrOverseasPensionScheme.toOverseasPensionsContributions

}
