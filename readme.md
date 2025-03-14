# :turtle: http4s-karapace

Provides an [http4s](https://http4s.org) client for the [Karapace](https://www.karapace.io) schema registry.

## Usage

You can add the following line to `build.sbt` to use the library.

```scala
libraryDependencies += "com.magine" %% "http4s-karapace" % http4sKarapaceVersion
```

Make sure to replace `http4sKarapaceVersion` with a [release version](https://github.com/maginepro/http4s-karapace/releases).<br>
Replace `%%` with `%%%` if you are using [Scala.js](https://www.scala-js.org) or [Scala Native](https://scala-native.org).

## Quick Example

Create a `SchemaRegistryClient` and use it to interact with a schema registry.

```scala
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import com.magine.http4s.karapace.SchemaRegistryClient
import com.magine.http4s.karapace.SubjectName
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all._

object Main extends IOApp.Simple {
  override def run: IO[Unit] =
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        schemaRegistryClient <- SchemaRegistryClient
          .builder(client, uri"http://localhost:8081")
          .withBasicAuth("username", "password")
          .build
        name = SubjectName("topic-value")
        subject <- schemaRegistryClient.getSubject(name)
        _ <- IO.println(subject)
      } yield ()
    }
}
```
