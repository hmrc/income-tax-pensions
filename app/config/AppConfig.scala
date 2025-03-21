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

package config

import com.google.inject.ImplementedBy
import models.common.{Nino, TaxYear}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.Logging

import javax.inject.Inject
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[BackendAppConfig])
trait AppConfig {

  def desBaseUrl: String

  def ifBaseUrl: String

  def hipBaseUrl: String

  def stateBenefitsBaseUrl: String
  def employmentBaseUrl: String

  /** It is used only to return URL for the Common Task List */
  def incomeTaxPensionsFrontendUrl: String

  def getEmploymentSourceUrl(nino: Nino, taxYear: TaxYear): String

  def getEmploymentUrl(nino: Nino, employmentId: String, source: String, taxYear: TaxYear): String
  def environment: String
  def authorisationToken: String
  def integrationFrameworkEnvironment: String
  def hipEnvironment: String

  def integrationFrameworkAuthorisationToken(api: String): String
  def hipAuthorisationToken(api: String): String

  def mongoTTL: Int
  def encryptionKey: String

  def useEncryption: Boolean
}

class BackendAppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends AppConfig with Logging {
  val desBaseUrl: String = servicesConfig.baseUrl("des")
  val ifBaseUrl: String  = servicesConfig.baseUrl("integration-framework")
  val hipBaseUrl: String = servicesConfig.baseUrl("hip")

  val stateBenefitsBaseUrl: String = servicesConfig.baseUrl("income-tax-state-benefits")
  val employmentBaseUrl: String    = s"${servicesConfig.baseUrl("income-tax-employment")}/income-tax-employment"

  /** It is used only to return URL for the Common Task List */
  val incomeTaxPensionsFrontendUrl: String =
    s"${servicesConfig.getString("microservice.services.income-tax-pensions-frontend.url")}/update-and-submit-income-tax-return"

  def getEmploymentSourceUrl(nino: Nino, taxYear: TaxYear) = s"$employmentBaseUrl/income-tax/nino/$nino/sources?taxYear=$taxYear"

  def getEmploymentUrl(nino: Nino, employmentId: String, source: String, taxYear: TaxYear) =
    s"$employmentBaseUrl/income-tax/nino/$nino/sources/$employmentId/$source?taxYear=$taxYear"

  val environment: String                     = config.get[String]("microservice.services.des.environment")
  val authorisationToken: String              = config.get[String]("microservice.services.des.authorisation-token")
  val integrationFrameworkEnvironment: String = config.get[String]("microservice.services.integration-framework.environment")
  val hipEnvironment: String                  = config.get[String]("microservice.services.hip.environment")

  def integrationFrameworkAuthorisationToken(api: String): String =
    config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")

  def hipAuthorisationToken(api: String): String =
    config.get[String](s"microservice.services.hip.authorisation-token.$api")

  val mongoTTL: Int         = Duration(servicesConfig.getString("mongodb.timeToLive")).toDays.toInt
  val encryptionKey: String = servicesConfig.getString("mongodb.encryption.key")

  val useEncryption: Boolean = {
    logger.warn("Mongo Backend encryption is turned off")
    servicesConfig.getBoolean("useEncryption")
  }
}
