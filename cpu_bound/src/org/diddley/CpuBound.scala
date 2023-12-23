package org.diddley

import zio.*
import Utils.*

object CpuBound extends ZIOAppDefault {
  def run = app
    .catchAll(e => ZIO.debug(s"Error: $e"))
    .catchAllDefect(e => ZIO.debug(s"Unrecoverable Error: $e"))
    .time("app")

  private val app = for {
    lines                                            <- readFile
    numCores                                          = 4
    chunks: List[Seq[String]]                         = lines.grouped(lines.size / numCores).toList
    fibers: List[Fiber.Runtime[_, Map[String, Int]]] <- ZIO.foreach(chunks)(chunk => count(chunk).fork)
    results: List[Map[String, Int]]                  <- ZIO.foreach(fibers)(_.join)
    wordCount                                         = results.fold(Map[String, Int]())(merge)
    _                                                <- ZIO.debug(wordCount.toList.sortBy(_._2).reverse.take(10).mkString("\n"))
  } yield ()

  private val nonParallelApp = for {
    lines     <- readFile
    wordCount <- count(lines)
    _         <- ZIO.debug(wordCount.toList.sortBy(_._2).reverse.take(10).mkString("\n"))
  } yield ()

  private def readFile: Task[List[String]] = ZIO.attempt {
    val resource = scala.io.Source.fromResource("100MB.txt")
    resource.getLines.toList
  }.time("readFile")

  private def count(lines: Seq[String]): Task[Map[String, Int]] = ZIO.attemptBlocking {
    lines.foldLeft(Map[String, Int]()) { (map, line) =>
      map ++ line
        .split(" ")
        .filter(_.nonEmpty)
        .foldLeft(Map[String, Int]()) { (count, word) =>
          val oldCount = count.getOrElse(word, map.getOrElse(word, 0))
          count + (word -> (oldCount + 1))
        }
    }
  }

  private def merge(one: Map[String, Int], two: Map[String, Int]): Map[String, Int] =
    one ++ two.map { case (k, v) => k -> (v + one.getOrElse(k, 0)) }
}
