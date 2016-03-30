# Yawn
_A YAML parser for Scala_

## Overview
Yawn is a YAML parser for Scala, built on [SnakeYAML](http://www.snakeyaml.org),
heavily influenced by [jawn](http://github.com/non/jawn).

Yawn provides only a parser and plugin interface for constructing an AST. The
AST is pluggable, and at present we provide modules for several popular Scala
JSON AST implementations:

* [argonaut](http://argonaut.io)
* [circe](http://circe.io)

## Dependencies
Yawn supports Scala 2.10 and 2.11, add the `core` dependency to your project:

### SBT
```scala
val yawnVersion = "1.0.0"
libraryDependencies += "com.datasift.yawn" %% "yawn-core" % yawnVersion
```

### Maven
```xml
<dependency>
    <groupId>com.datasift.yawn</groupId>
    <artifactId>yawn-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

To use Yawn with your favourite JSON AST, also add a dependency to its support
module:

### Argonaut
```scala
libraryDependencies += "com.datasift.yawn" %% "yawn-argonaut" % yawnVersion
```

### Circe
```scala
libraryDependencies += "com.datasift.yawn" %% "yawn-circe" % yawnVersion
```

## Usage
To parse a YAML document, you'll first need to choose an AST; see above.

### Argonaut
Parsing YAML with Argonaut works similarly to parsing JSON. Define your codec
(or derive it automatically with `argonaut-shapeless`):

```scala
import argonaut._, Argonaut._

case class Person(name: String, age: Int, things: List[String])

implicit def PersonCodec = casecodec3(Person.apply, Person.unapply)("name", "age", "things")
```

Then use `YamlParse` to parse your YAML from a String:

```scala
import com.datasift.yawn.argonaut.YamlParse

val yaml = Source.fromFile("person.yaml").mkString
YamlParse.decodeOption[Person](yaml)
```

Note: Currently, yawn doesn't provide methods directly on `String` to decode
YAML as Argonaut does for JSON. This is because these methods would conflict,
and Argonaut wouldn't know whether to parse the String as JSON or YAML.

Instead, you must explicitly use `YamlParse` to work with YAML. Hopefully this
issue will be solved in the future.

### Circe

Parsing YAML with Circe works similarly to parsing JSON. Define your codec (or
derive it automatically with `circe-generic`):

```scala
import io.circe._, io.circe.generic.auto._

case class Person(name: String, age: Int, things: List[String])
```

Then use yawn to parse your YAML from a String:

```scala
import com.datasift.yawn.circe.yawn._

val yaml = Source.fromFile("person.yaml").mkString
decode[Person](yaml)
```

## License and Copyright
All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php.

Copyright DataSift Inc. 2016
