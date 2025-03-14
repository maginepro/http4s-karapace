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
import cats.effect.Resource
import cats.syntax.all.*
import java.io.File
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait

final class ContainerSchemaRegistryClientSuite extends SchemaRegistryClientSuite {
  val schemaRegistryClientFixture: Fixture[SchemaRegistryClient[IO]] = {
    val schemaRegistryContainer: Resource[IO, DockerComposeContainer[?]] =
      Resource.make {
        IO.blocking {
          val container: DockerComposeContainer[?] =
            new DockerComposeContainer(new File("src/test/resources/compose.yml"))
          container.withExposedService("karapace-registry", 8081, Wait.forHttp("/"))
          container.start()
          container
        }
      }(container => IO.blocking(container.stop()))

    val schemaRegistryClient: DockerComposeContainer[?] => Resource[IO, SchemaRegistryClient[IO]] =
      container =>
        EmberClientBuilder.default[IO].build.evalMap { client =>
          val host = container.getServiceHost("karapace-registry", 8081)
          val port = container.getServicePort("karapace-registry", 8081)
          val address = Uri.fromString(s"http://$host:$port").liftTo[IO]
          address.flatMap(SchemaRegistryClient.default(client, _))
        }

    ResourceSuiteLocalFixture(
      "schemaRegistryClient",
      schemaRegistryContainer.flatMap(schemaRegistryClient)
    )
  }

  override def munitFixtures: Seq[Fixture[?]] =
    schemaRegistryClientFixture +: super.munitFixtures

  override def schemaRegistryClient: IO[SchemaRegistryClient[IO]] =
    IO(schemaRegistryClientFixture())
}
