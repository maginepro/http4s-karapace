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
import org.http4s.Uri
import org.http4s.client.Client

final class SchemaRegistryClientBuilder[F[_]: Concurrent] private (
  val auth: Auth,
  val cache: Boolean,
  val client: Client[F],
  val uri: Uri
) {
  def build: F[SchemaRegistryClient[F]] =
    SchemaRegistryClient.fromBuilder(this)

  private def copy(
    auth: Auth = auth,
    cache: Boolean = cache,
    client: Client[F] = client,
    uri: Uri = uri
  ): SchemaRegistryClientBuilder[F] =
    new SchemaRegistryClientBuilder[F](
      auth = auth,
      cache = cache,
      client = client,
      uri = uri
    )

  def withAuth(auth: Auth): SchemaRegistryClientBuilder[F] =
    copy(auth = auth)

  def withBasicAuth(username: String, password: String): SchemaRegistryClientBuilder[F] =
    copy(auth = Auth.Basic(username, password))

  def withoutAuth: SchemaRegistryClientBuilder[F] =
    copy(auth = Auth.None)

  def withCache: SchemaRegistryClientBuilder[F] =
    copy(cache = true)

  def withoutCache: SchemaRegistryClientBuilder[F] =
    copy(cache = false)

  def withClient(client: Client[F]): SchemaRegistryClientBuilder[F] =
    copy(client = client)

  def withUri(uri: Uri): SchemaRegistryClientBuilder[F] =
    copy(uri = uri)
}

object SchemaRegistryClientBuilder {
  def default[F[_]: Concurrent](client: Client[F], uri: Uri): SchemaRegistryClientBuilder[F] =
    new SchemaRegistryClientBuilder(
      auth = Auth.None,
      cache = true,
      client = client,
      uri = uri
    )
}
