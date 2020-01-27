package leaderboard.plugins

import distage.TagKK
import distage.plugins.PluginDef
import izumi.distage.model.definition.ModuleDef
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.roles.bundled.DistageRolesModule
import leaderboard.LeaderboardRole
import leaderboard.http.HttpApi
import leaderboard.repo.{Ladder, Profiles, Ranks}
import org.http4s.dsl.Http4sDsl
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])

  object modules {
    def roles[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      // The `leaderboard` app
      make[LeaderboardRole[F]]

      // Bundled roles: `help` & `configwriter`
      include(DistageRolesModule[F[Throwable, ?]](version = "1.0.0-SNAPSHOT"))
    }

    def api[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[HttpApi[F]].from[HttpApi.Impl[F]]
      make[Ranks[F]].from[Ranks.Impl[F]]

      make[Http4sDsl[F[Throwable, ?]]]
    }

    def repoDummy[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Dummy)

      make[Ladder[F]].fromResource[Ladder.Dummy[F]]
      make[Profiles[F]].fromResource[Profiles.Dummy[F]]
    }
  }
}
