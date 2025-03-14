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

import org.http4s.BasicCredentials
import org.http4s.Headers
import org.http4s.headers.Authorization

sealed abstract class Auth {
  private[karapace] def headers: Headers =
    this match {
      case Auth.BasicAuth(username, password) =>
        Headers(Authorization(BasicCredentials(username, password)))
      case Auth.NoAuth =>
        Headers()
    }
}

object Auth {
  private final case class BasicAuth(username: String, password: String) extends Auth {
    override def toString: String = s"Basic($username, ***)"
  }

  private case object NoAuth extends Auth {
    override def toString: String = "None"
  }

  def Basic(username: String, password: String): Auth =
    BasicAuth(username, password)

  def None: Auth =
    NoAuth
}
