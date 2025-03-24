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

import cats.effect.IO
import io.circe.Json
import io.circe.syntax.*
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.http4s.Status
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllNoShrinkF

abstract class SchemaRegistryClientSuite extends CatsEffectSuite with ScalaCheckEffectSuite {
  def schemaRegistryClient: IO[SchemaRegistryClient[IO]]

  private val schema: Schema =
    Schema(
      Json.obj(
        "type" -> "record".asJson,
        "name" -> "UserEvent".asJson,
        "fields" -> Json.arr(
          Json.obj(
            "name" -> "userId".asJson,
            "type" -> "string".asJson
          )
        )
      )
    )

  private implicit val schemaIdArbitrary: Arbitrary[SchemaId] =
    Arbitrary(Gen.posNum[Int].map(SchemaId.apply))

  private implicit val subjectNameArbitrary: Arbitrary[SubjectName] =
    Arbitrary(
      for {
        length <- Gen.chooseNum(1, 30)
        chars <- Gen.listOfN(length, Gen.alphaNumChar)
      } yield SubjectName(chars.mkString)
    )

  test("getSchema") {
    forAllNoShrinkF { (id: SchemaId) =>
      schemaRegistryClient
        .flatMap(_.getSchema(id))
        .attempt
        .map {
          case Right(_) => true
          case Left(SchemaRegistryError(Status.NotFound, _, _)) => true
          case Left(_) => false
        }
        .assert
    }
  }

  test("registerSchema >> getSchema") {
    forAllNoShrinkF { (name: SubjectName) =>
      val obtained =
        for {
          client <- schemaRegistryClient
          id <- client.registerSchema(name, schema)
          schema <- client.getSchema(id)
        } yield schema

      obtained.assertEquals(schema)
    }
  }

  test("getSchemaOption") {
    forAllNoShrinkF { (id: SchemaId) =>
      schemaRegistryClient
        .flatMap(_.getSchemaOption(id))
        .attempt
        .map(_.isRight)
        .assert
    }
  }

  test("registerSchema >> getSchemaOption") {
    forAllNoShrinkF { (name: SubjectName) =>
      val obtained =
        for {
          client <- schemaRegistryClient
          id <- client.registerSchema(name, schema)
          schema <- client.getSchemaOption(id)
        } yield schema

      obtained.assertEquals(Some(schema))
    }
  }

  test("getSubject") {
    forAllNoShrinkF { (name: SubjectName) =>
      schemaRegistryClient
        .flatMap(_.getSubject(name))
        .attempt
        .map {
          case Right(_) => true
          case Left(SchemaRegistryError(Status.NotFound, _, _)) => true
          case Left(_) => false
        }
        .assert
    }
  }

  test("registerSchema >> getSubject") {
    forAllNoShrinkF { (name: SubjectName) =>
      val obtained =
        for {
          client <- schemaRegistryClient
          _ <- client.registerSchema(name, schema)
          subject <- client.getSubject(name)
        } yield subject.schema

      obtained.assertEquals(schema)
    }
  }

  test("getSubjectOption") {
    forAllNoShrinkF { (name: SubjectName) =>
      schemaRegistryClient
        .flatMap(_.getSubjectOption(name))
        .attempt
        .map(_.isRight)
        .assert
    }
  }

  test("registerSchema >> getSubjectOption") {
    forAllNoShrinkF { (name: SubjectName) =>
      val obtained =
        for {
          client <- schemaRegistryClient
          _ <- client.registerSchema(name, schema)
          subject <- client.getSubjectOption(name)
        } yield subject.map(_.schema)

      obtained.assertEquals(Some(schema))
    }
  }

  test("isCompatible") {
    forAllNoShrinkF { (name: SubjectName) =>
      schemaRegistryClient
        .flatMap(_.isCompatible(name, schema))
        .attempt
        .map {
          case Right(_) => true
          case Left(SchemaRegistryError(Status.NotFound, _, _)) => true
          case Left(_) => false
        }
        .assert
    }
  }

  test("registerSchema >> isCompatible") {
    forAllNoShrinkF { (name: SubjectName) =>
      val compatible =
        for {
          client <- schemaRegistryClient
          _ <- client.registerSchema(name, schema)
          compatible <- client.isCompatible(name, schema)
        } yield compatible

      compatible.assertEquals(true)
    }
  }

  test("isCompatibleOption") {
    forAllNoShrinkF { (name: SubjectName) =>
      schemaRegistryClient
        .flatMap(_.isCompatibleOption(name, schema))
        .attempt
        .map(_.isRight)
        .assert
    }
  }

  test("registerSchema >> isCompatibleOption") {
    forAllNoShrinkF { (name: SubjectName) =>
      val compatible =
        for {
          client <- schemaRegistryClient
          _ <- client.registerSchema(name, schema)
          compatible <- client.isCompatibleOption(name, schema)
        } yield compatible

      compatible.assertEquals(Some(true))
    }
  }
}
