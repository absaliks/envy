package absaliks.envy.data

import absaliks.envy.env.Envs
import absaliks.envy.data.properties.PropertiesWriter
import absaliks.envy.data.properties.flattenProperties
import absaliks.envy.data.util.FileUtil
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlNode

fun main() {
  val start = System.currentTimeMillis()
  val envsText = FileUtil.readFile("config/envs.yaml")
  val envs = Envs(envsText)

  val dataText = FileUtil.readFile("config/data.yaml")
  val data: YamlNode = Yaml.default.parseToYamlNode(dataText)

  envs.values()
    .filter { it.isLeaf() }
    .associateWith { env -> flattenProperties(data, env, envs) }
    .forEach { (env, properties) ->
      PropertiesWriter.write(env.name, properties)
    }

  println("Property files created in ${System.currentTimeMillis() - start}ms")
}
