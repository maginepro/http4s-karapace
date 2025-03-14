/*
 * Copyright 2025 Magine Pro
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

package com.magine.http4s.karapace

import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import org.http4s.Uri.Path.SegmentEncoder

final case class SubjectVersion(value: Int) {
  def next: SubjectVersion =
    SubjectVersion(value + 1)

  def prev: SubjectVersion =
    SubjectVersion(value - 1)
}

object SubjectVersion {
  val initial: SubjectVersion =
    SubjectVersion(1)

  implicit val subjectVersionCodec: Codec[SubjectVersion] =
    Codec.from(
      Decoder[Int].map(apply),
      Encoder[Int].contramap(_.value)
    )

  implicit val subjectVersionOrdering: Ordering[SubjectVersion] =
    Ordering.by(_.value)

  implicit val subjectVersionSegmentEncoder: SegmentEncoder[SubjectVersion] =
    SegmentEncoder[Int].contramap(_.value)
}
