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

import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.Json
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.Status.Successful
import org.http4s.Uri
import org.http4s.Uri.Path.Root
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.headers.`Content-Type`

trait SchemaRegistryClient[F[_]] {

  /**
    * Returns the schema with the specified `id` if it exists;
    * otherwise raises a [[SchemaRegistryError]] exception.
    */
  def getSchema(id: SchemaId): F[Schema]

  /**
    * Returns the schema with the specified `id` if it
    * exists; `None` otherwise.
    */
  def getSchemaOption(id: SchemaId): F[Option[Schema]]

  /**
    * Returns a description of the subject with the
    * specified `name` if it exists; otherwise raises
    * a [[SchemaRegistryError]] exception.
    */
  def getSubject(name: SubjectName): F[Subject]

  /**
    * Returns a description of the subject with the
    * specified `name` if it exists; `None` otherwise.
    */
  def getSubjectOption(name: SubjectName): F[Option[Subject]]

  /**
    * Returns `true` if the specified `schema` is compatible
    * with the specified subject; `false` otherwise. If the
    * subject does not exist, a [[SchemaRegistryError]]
    * exception is raised.
    */
  def isCompatible(name: SubjectName, schema: Schema): F[Boolean]

  /**
    * Returns `true` if the specified `schema` is compatible
    * with the specified subject; `false` otherwise. If the
    * subject does not exist, returns `None`.
    */
  def isCompatibleOption(name: SubjectName, schema: Schema): F[Option[Boolean]]

  /**
    * Registers the specified `schema` for the subject and
    * returns its [[SchemaId]].
    */
  def registerSchema(name: SubjectName, schema: Schema): F[SchemaId]
}

object SchemaRegistryClient {
  def builder[F[_]: Concurrent](client: Client[F], uri: Uri): SchemaRegistryClientBuilder[F] =
    SchemaRegistryClientBuilder.default(client, uri)

  def fromBuilder[F[_]: Concurrent](builder: SchemaRegistryClientBuilder[F]): F[SchemaRegistryClient[F]] =
    Ref[F].of(Map.empty[SchemaId, Schema]).map { ref =>
      new SchemaRegistryClient[F] {
        import builder.uri

        private val mediaType: MediaType =
          new MediaType(
            mainType = "application",
            subType = "vnd.schemaregistry.v1+json",
            compressible = true,
            binary = true
          )

        private val headers: Headers =
          builder.auth.headers.put(Accept(mediaType))

        private implicit def jsonEntityDecoder[A: Decoder]: EntityDecoder[F, A] =
          jsonOfWithMedia(mediaType)

        private implicit val jsonEntityEncoder: EntityEncoder[F, Json] =
          jsonEncoder[F].withContentType(`Content-Type`(mediaType))

        private def run[A: Decoder](request: Request[F]): F[A] =
          builder.client.run(request.putHeaders(headers)).use {
            case Successful(response) =>
              response.as[A]

            case response =>
              implicit val errorDecoder: Decoder[SchemaRegistryError] =
                SchemaRegistryError.codec(response.status)
              response.as[SchemaRegistryError].flatMap(_.raiseError)
          }

        private def option[A](fa: F[A]): F[Option[A]] =
          fa.map(_.some).recover { case SchemaRegistryError(Status.NotFound, _, _) => None }

        private def cached(id: SchemaId)(schema: F[Schema]): F[Schema] =
          if (builder.cache)
            ref.get.flatMap { schemas =>
              schemas.get(id) match {
                case Some(schema) => schema.pure[F]
                case None => schema.flatTap(schema => ref.update(_.updated(id, schema)))
              }
            }
          else schema

        override def getSchema(id: SchemaId): F[Schema] = {
          implicit val decoder: Decoder[Schema] = Schema.schemaCodec.at("schema")
          cached(id)(run(Request(Method.GET, uri.withPath(Root / "schemas" / "ids" / id))))
        }

        override def getSchemaOption(id: SchemaId): F[Option[Schema]] =
          option(getSchema(id))

        override def getSubject(name: SubjectName): F[Subject] =
          run(Request(Method.GET, uri.withPath(Root / "subjects" / name / "versions" / "latest")))

        override def getSubjectOption(name: SubjectName): F[Option[Subject]] =
          option(getSubject(name))

        override def isCompatible(name: SubjectName, schema: Schema): F[Boolean] = {
          implicit val decoder: Decoder[Boolean] =
            Decoder.decodeBoolean.at("is_compatible")

          run(
            Request(
              Method.POST,
              uri.withPath(Root / "compatibility" / "subjects" / name / "versions" / "latest")
            ).withEntity(Json.obj("schema" -> schema.asJson))
          )
        }

        override def isCompatibleOption(name: SubjectName, schema: Schema): F[Option[Boolean]] =
          option(isCompatible(name, schema))

        override def registerSchema(name: SubjectName, schema: Schema): F[SchemaId] = {
          implicit val decoder: Decoder[SchemaId] = SchemaId.schemaIdCodec.at("id")
          run(
            Request(Method.POST, uri.withPath(Root / "subjects" / name / "versions"))
              .withEntity(Json.obj("schema" -> schema.asJson))
          )
        }
      }
    }

  def default[F[_]: Concurrent](client: Client[F], uri: Uri): F[SchemaRegistryClient[F]] =
    SchemaRegistryClient.builder(client, uri).build
}
