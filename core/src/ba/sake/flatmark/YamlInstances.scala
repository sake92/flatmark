package ba.sake.flatmark

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.{Locale, TimeZone}
import scala.util.{Try, boundary}
import org.virtuslab.yaml.*
import org.virtuslab.yaml.Node.{MappingNode, ScalarNode}

import scala.collection.immutable.ListMap

object YamlInstances {

  given YamlDecoder[LocalDateTime] = YamlDecoder { case s @ ScalarNode(value, _) =>
    Try(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).toEither.left
      .map(ConstructError.from(_, "Timestamp", s))
  }

  given YamlEncoder[LocalDateTime] = dt => ScalarNode(dt.toString)

  given YamlDecoder[TimeZone] = YamlDecoder { case s @ ScalarNode(value, _) =>
    Try(TimeZone.getTimeZone(value)).toEither.left
      .map(ConstructError.from(_, "TimeZone", s))
  }

  given YamlEncoder[TimeZone] = dt => ScalarNode(dt.toString)

  given YamlDecoder[Locale] = YamlDecoder { case s @ ScalarNode(value, _) =>
    Try(Locale.forLanguageTag(value)).toEither.left
      .map(ConstructError.from(_, "Language", s))
  }

  given YamlEncoder[Locale] = dt => ScalarNode(dt.toString)
  
  given YamlEncoder[Node] = dt => dt

  given YamlDecoder[Node] = YamlDecoder { case n =>
    Right(n)
  }

  given [K, V](using
      keyDecoder: YamlDecoder[K],
      valueDecoder: YamlDecoder[V]
  ): YamlDecoder[ListMap[K, V]] = YamlDecoder { case MappingNode(mappings, _) =>
    val decoded: Seq[Either[ConstructError, (K, V)]] =
      mappings.toSeq
        .map { case (key, value) =>
          keyDecoder.construct(key) -> valueDecoder.construct(value)
        }
        .map { case (key, value) =>
          for {
            k <- key
            v <- value
          } yield (k -> v)
        }
    decoded.partitionMap(identity) match {
      case (lefts, _) if lefts.nonEmpty => Left(lefts.head)
      case (_, rights)                  => Right(ListMap.from(rights))
    }
  }

  given [K, V](using keyCodec: YamlEncoder[K], valueCodec: YamlEncoder[V]): YamlEncoder[ListMap[K, V]] = { (nodes) =>
    val mappings = nodes.map { case (key, value) =>
      keyCodec.asNode(key) -> valueCodec.asNode(value)
    }
    Node.MappingNode(mappings)
  }
}
