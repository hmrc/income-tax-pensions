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

package models

package object frontend {

  def hasAnswer(boolField: Boolean, field: Option[_]): Boolean =
    (boolField && field.isDefined) || !boolField

  def hasAnswer(prerequisiteQuestion: Boolean, boolField: Option[Boolean], field: Option[_]): Boolean =
    (prerequisiteQuestion && hasAnswer(boolField, field)) || !prerequisiteQuestion

  def hasAnswer(boolField: Option[Boolean], field: Option[_]): Boolean =
    boolField match {
      case Some(bool) =>
        if (bool) field.isDefined else true
      case None =>
        // if we don't know Yes/No, but we have the value, we know it was Yes.
        // However, we  cannot say it was No because it could be None or No
        field.isDefined
    }
}
