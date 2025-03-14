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

import cats.syntax.all.*
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.parser.parse

final case class Schema(value: Json) {
  override def toString: String =
    s"Schema(${value.noSpaces})"
}

object Schema {
  implicit val schemaCodec: Codec[Schema] =
    Codec.from(
      Decoder[String].emap(parse(_).bimap(_.message, apply)),
      Encoder[String].contramap(_.value.noSpaces)
    )
}
