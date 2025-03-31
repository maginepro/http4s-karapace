val catsEffectVersion = "3.6.0"
val circeVersion = "0.14.8"
val fs2Version = "3.11.0"
val http4sVersion = "0.23.30"
val munitCatsEffectVersion = "2.0.0"
val scala213Version = "2.13.16"
val scala3Version = "3.3.5"
val scalaCheckEffectVersion = "2.0.0-M2"
val testcontainersVersion = "1.20.6"

inThisBuild(
  Seq(
    crossScalaVersions := Seq(scala213Version, scala3Version),
    developers := List(tlGitHubDev("vlovgr", "Viktor Rudebeck")),
    githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17")),
    githubWorkflowTargetBranches := Seq("**"),
    licenses := Seq(License.Apache2),
    organization := "com.magine",
    organizationName := "Magine Pro",
    scalaVersion := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    startYear := Some(2025),
    tlBaseVersion := "2.1",
    tlCiHeaderCheck := true,
    tlCiScalafixCheck := true,
    tlCiScalafmtCheck := true,
    tlFatalWarnings := true,
    tlJdkRelease := Some(8),
    tlUntaggedAreSnapshots := false,
    versionScheme := Some("early-semver")
  )
)

lazy val root = tlCrossRootProject
  .aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/core"))
  .settings(
    name := "http4s-karapace",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % fs2Version,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "org.http4s" %%% "http4s-circe" % http4sVersion,
      "org.http4s" %%% "http4s-client" % http4sVersion,
      "org.http4s" %%% "http4s-core" % http4sVersion,
      "org.typelevel" %%% "cats-effect-kernel" % catsEffectVersion,
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "org.typelevel" %%% "munit-cats-effect" % munitCatsEffectVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % scalaCheckEffectVersion % Test
    )
  )
  .jvmSettings(
    Test / fork := true,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
      "org.testcontainers" % "testcontainers" % testcontainersVersion % Test
    )
  )
