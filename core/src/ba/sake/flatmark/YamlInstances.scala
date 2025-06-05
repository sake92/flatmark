package ba.sake.flatmark

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.{Locale, TimeZone}
import scala.util.{Try, boundary}
import org.virtuslab.yaml.*
import org.virtuslab.yaml.Node.ScalarNode

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

}
