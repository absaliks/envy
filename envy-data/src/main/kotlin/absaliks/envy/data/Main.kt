package absaliks.envy.data

import absaliks.envy.data.env.EnvsIndex
import absaliks.envy.data.properties.PropertiesFlattener
import absaliks.envy.data.properties.PropertiesWriter
import absaliks.envy.data.util.FileUtil
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

fun main() {
  val start = System.currentTimeMillis()
  val configText = FileUtil.readFile("config.yaml")
  val config = Yaml.default.decodeFromString<Config>(configText)

  val envsIndex = EnvsIndex(config.envs)

  envsIndex.values()
    .filter { it.isLeaf() }
    .associateWith { env -> PropertiesFlattener(config, env, envsIndex).flatten() }
    .forEach { (env, properties) ->
      PropertiesWriter.write(env.name, properties)
    }

  println("Property files created in ${System.currentTimeMillis() - start}ms")
}
