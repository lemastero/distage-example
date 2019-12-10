package sample

import cats.effect.{ConcurrentEffect, Timer}
import distage.DIResource
import distage.DIResource.DIResourceBase
import izumi.distage.framework.model.PluginSource
import izumi.distage.model.definition.Activation
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.plugins.load.PluginLoader.PluginConfig
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.distage.roles.{RoleAppLauncher, RoleAppMain}
import izumi.fundamentals.platform.cli.model.raw.{RawEntrypointParams, RawRoleParams}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sample.http.HttpApi

/** Example session:
  *
  * {{{
  * curl -X POST http://localhost:8080/ladder/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4/100
  * curl -X POST http://localhost:8080/profile/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4 -d '{"name": "Kai", "description": "S C A L A"}'
  * curl -X GET http://localhost:8080/profile/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4
  * curl -X GET http://localhost:8080/ladder
  * }}}
  */
final class SampleRole[F[+_, +_]](
  httpApi: HttpApi[F],
)(implicit
  concurrentEffect: ConcurrentEffect[F[Throwable, ?]],
  timer: Timer[F[Throwable, ?]],
) extends RoleService[F[Throwable, ?]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): DIResourceBase[F[Throwable, ?], Unit] = {
    DIResource.fromCats {
      BlazeServerBuilder[F[Throwable, ?]]
        .withHttpApp(httpApi.http.orNotFound)
        .bindLocal(8080)
        .resource
    }.void
  }
}
object SampleRole extends RoleDescriptor {
  val id = "sample"
}

object Main
  extends RoleAppMain.Default(
    launcher = new RoleAppLauncher.LauncherBIO[zio.IO] {
      override val pluginSource = PluginSource(
        PluginConfig(
          debug            = false,
          packagesEnabled  = Seq("sample.plugins"),
          packagesDisabled = Nil,
        )
      )
      override val requiredActivations = Activation(
        Repo -> Repo.Dummy
      )
    }
  ) {
  override val requiredRoles = Vector(
    RawRoleParams.empty(SampleRole.id)
  )
}