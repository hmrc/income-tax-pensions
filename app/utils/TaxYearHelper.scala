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

package utils

import connectors.PensionChargesConnector.PensionChargesBaseApi
import connectors.PensionIncomeConnector.PensionIncomeBaseApi
import connectors.PensionReliefsConnector.PensionReliefsBaseApi

/** Represents a tax year for DES or IF Calls
  *
  * Calculating the tax year string (where 2018 represents 2017-18)
  */
object TaxYearHelper {

  def twoDigitYear(year: Int): String = year.toString takeRight 2
  def ifTysTaxYearConverter(taxYear: Int): String =
    s"${twoDigitYear(taxYear - 1)}-${twoDigitYear(taxYear)}"

  private val tysApiOrderedMap: Map[(String, Int), String] = Map(
    (PensionIncomeBaseApi.Update, 2024)  -> "1884",
    (PensionIncomeBaseApi.Get, 2024)     -> "1909",
    (PensionIncomeBaseApi.Delete, 2024)  -> "1910",
    (PensionReliefsBaseApi.Update, 2024) -> "1797",
    (PensionReliefsBaseApi.Get, 2024)    -> "1798",
    (PensionReliefsBaseApi.Delete, 2024) -> "1799",
    (PensionChargesBaseApi.Update, 2024) -> "1868",
    (PensionChargesBaseApi.Get, 2024)    -> "1869",
    (PensionChargesBaseApi.Delete, 2024) -> "1883"
  ).toSeq.sortWith(_._1._2 > _._1._2).toMap // order map by year descending

  def desIfTaxYearConverter(taxYear: Int): String =
    s"${taxYear - 1}-${twoDigitYear(taxYear)}"

  def isTysApi(taxYear: Int, apiNum: String, tysMap: Map[(String, Int), String] = tysApiOrderedMap): Boolean =
    findLatestTys(taxYear, apiNum, tysMap).nonEmpty

  def apiPath(nino: String, taxYear: Int, apiNum: String): String =
    if (isTysApi(taxYear, apiNum)) s"${ifTysTaxYearConverter(taxYear)}/$nino" else s"$nino/${desIfTaxYearConverter(taxYear)}"

  def apiVersion(taxYear: Int, apiNum: String, tysMap: Map[(String, Int), String] = tysApiOrderedMap): String =
    estTys(taxYear, apiNum, tysMap).map(_._2).getOrElse(apiNum)

  private def findLatestTys(taxYear: Int, apiNum: String, tysMap: Map[(String, Int), String]): Option[((String, Int), String)] =
    tysMap.find { case ((api, year), _) => api == apiNum && year <= taxYear }
}
