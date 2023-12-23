package org.diddley

import zio.*

object Utils:
  extension [R, E, A](effect: ZIO[R, E, A])
    def time(name: String): ZIO[R, E, A] =
      for {
        tuple <- effect.timed
        (time, result) = tuple
        _ <- ZIO.debug(s"Time for $name: ${time.toMillis}ms")
      } yield result
