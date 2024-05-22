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

package testdata.connector

import models.{GetPensionReliefsModel, PensionReliefs}

object getPensionReliefsModel {

  def getPensionReliefsModel = GetPensionReliefsModel(
    "2020-01-04T05:01:01Z",
    Some("2020-01-04T05:01:01Z"),
    pensionReliefs
  )

  def pensionReliefs = PensionReliefs(
    Some(1),
    Some(2),
    Some(3),
    Some(4),
    overseasPensionSchemeContributions = Some(2)
  )

}
