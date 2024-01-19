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

package config

import com.google.inject.ImplementedBy
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject


class BackendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {
  val authBaseUrl: String = servicesConfig.baseUrl("auth")
  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

  val desBaseUrl: String = servicesConfig.baseUrl("des")
  val stateBenefitsBaseUrl: String = servicesConfig.baseUrl("income-tax-state-benefits")
  val ifBaseUrl: String = servicesConfig.baseUrl("integration-framework")

  val environment: String = config.get[String]("microservice.services.des.environment")
  val authorisationToken: String = config.get[String]("microservice.services.des.authorisation-token")
  val integrationFrameworkEnvironment: String = config.get[String]("microservice.services.integration-framework.environment")

  val submissionBaseUrl: String = s"${servicesConfig.baseUrl(serviceName = "income-tax-submission")}/income-tax-submission-service"

  def integrationFrameworkAuthorisationToken(api:String): String = config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")

}

@ImplementedBy(classOf[BackendAppConfig])
trait AppConfig  {
  val authBaseUrl: String
  val auditingEnabled: Boolean
  val graphiteHost: String

  val desBaseUrl: String
  val ifBaseUrl: String
  val stateBenefitsBaseUrl: String

  val environment: String
  val authorisationToken: String
  val integrationFrameworkEnvironment: String
  val submissionBaseUrl: String

  def integrationFrameworkAuthorisationToken(api: String): String
}
