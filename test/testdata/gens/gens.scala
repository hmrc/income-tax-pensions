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

import org.scalacheck.Gen

import scala.util.{Success, Try}

package object gens {
  val booleanGen: Gen[Boolean] = Gen.oneOf(true, false)

  val bigDecimalGen: Gen[BigDecimal] = Gen
    .chooseNum[BigDecimal](0, 10000)
    .map(n => n.setScale(2, BigDecimal.RoundingMode.HALF_UP))

  val intGen: Gen[Int] = Gen.chooseNum[Int](0, 10000)

  val stringGen: Gen[String] = for {
    length <- Gen.chooseNum(1, 10)
    chars  <- Gen.listOfN(length, Gen.alphaNumChar)
  } yield chars.mkString

  def stringSeqGen(minLength: Int = 1, maxLength: Int = 2): Gen[Seq[String]] = for {
    length  <- Gen.chooseNum(minLength, maxLength)
    strings <- Gen.listOfN(length, stringGen)
  } yield strings

  /** gen.sample.get - can fail as generator may return None. Use this method for safely generate one instance of A
    */
  def genOne[A](gen: Gen[A]): A =
    LazyList.continually(Try(gen.sample)).collect { case Success(Some(value)) => value }.head
}
