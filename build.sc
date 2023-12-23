import mill._, scalalib._

object cpu_bound extends ScalaModule {
  def scalaVersion = "3.3.1"

  def ivyDeps = Agg(
    ivy"dev.zio::zio:2.0.20"
  )
}
