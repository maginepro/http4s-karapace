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

final case class SchemaId(value: Int) {
  def next: SchemaId =
    SchemaId(value + 1)

  def prev: SchemaId =
    SchemaId(value - 1)
}

object SchemaId {
  val initial: SchemaId =
    SchemaId(1)

  implicit val schemaIdCodec: Codec[SchemaId] =
    Codec.from(
      Decoder[Int].map(apply),
      Encoder[Int].contramap(_.value)
    )

  implicit val schemaIdOrdering: Ordering[SchemaId] =
    Ordering.by(_.value)

  implicit val schemaIdSegmentEncoder: SegmentEncoder[SchemaId] =
    SegmentEncoder[Int].contramap(_.value)
}
