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
import org.http4s.QueryParamCodec
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamEncoder
import org.http4s.Uri.Path.SegmentEncoder

final case class SubjectName(value: String)

object SubjectName {
  implicit val subjectNameCodec: Codec[SubjectName] =
    Codec.from(
      Decoder[String].map(apply),
      Encoder[String].contramap(_.value)
    )

  implicit val subjectNameQueryParamCodec: QueryParamCodec[SubjectName] =
    QueryParamCodec.from(
      QueryParamDecoder[String].map(apply),
      QueryParamEncoder[String].contramap(_.value)
    )

  implicit val subjectNameSegmentEncoder: SegmentEncoder[SubjectName] =
    SegmentEncoder[String].contramap(_.value)
}
