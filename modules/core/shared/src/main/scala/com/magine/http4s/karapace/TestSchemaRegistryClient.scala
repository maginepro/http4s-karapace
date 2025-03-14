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
import org.http4s.Status

trait TestSchemaRegistryClient[F[_]] extends SchemaRegistryClient[F] {

  /**
    * Returns a list of subjects that have been registered.
    */
  def subjects: F[List[Subject]]
}

object TestSchemaRegistryClient {

  /**
    * Returns a client for an empty registry where all schemas
    * are compatible with each other, and schema registrations
    * always succeed as a consequence.
    */
  def empty[F[_]: Concurrent]: F[TestSchemaRegistryClient[F]] =
    Ref[F].of(List.empty[Subject]).map { ref =>
      new TestSchemaRegistryClient[F] {
        override def getSchema(id: SchemaId): F[Schema] =
          getSchemaOption(id).map(_.toRight(schemaNotFound)).rethrow

        override def getSchemaOption(id: SchemaId): F[Option[Schema]] =
          ref.get.map(_.find(_.id == id).map(_.schema))

        override def getSubject(name: SubjectName): F[Subject] =
          getSubjectOption(name).map(_.toRight(subjectNotFound(name))).rethrow

        override def getSubjectOption(name: SubjectName): F[Option[Subject]] =
          ref.get.map(_.find(_.name == name))

        override def isCompatible(name: SubjectName, schema: Schema): F[Boolean] =
          isCompatibleOption(name, schema).map(_.toRight(latestVersionNotFound)).rethrow

        override def isCompatibleOption(name: SubjectName, schema: Schema): F[Option[Boolean]] =
          getSubjectOption(name).map(_.as(true))

        override def registerSchema(name: SubjectName, schema: Schema): F[SchemaId] =
          ref.modify { subjects =>
            val id =
              subjects.headOption match {
                case Some(last) => last.id.next
                case None => SchemaId.initial
              }

            val version =
              subjects.find(_.name == name) match {
                case Some(last) => last.version.next
                case None => SubjectVersion.initial
              }

            val subject =
              Subject(id, schema, name, version)

            (subject :: subjects, id)
          }

        override def subjects: F[List[Subject]] =
          ref.get

        private def schemaNotFound: SchemaRegistryError =
          SchemaRegistryError(Status.NotFound, 40403, "Schema not found")

        private def subjectNotFound(name: SubjectName): SchemaRegistryError =
          SchemaRegistryError(Status.NotFound, 40401, s"Subject '${name.value}' not found.")

        private def latestVersionNotFound: SchemaRegistryError =
          SchemaRegistryError(Status.NotFound, 40402, "Version latest not found.")
      }
    }
}
