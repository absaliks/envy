package absaliks.envy.env

import absaliks.envy.data.env.Env
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

class Envs {

  private val index: Map<String, Env>

  constructor(text: String) {
    val envs = Yaml.default.decodeFromString<List<Env>>(text)
    val index = indexEnvs(mutableMapOf(), envs)
    validateResolutionPaths(index)
    this.index = index
  }

  private fun indexEnvs(map: MutableMap<String, Env>, envs: List<Env>): MutableMap<String, Env> {
    envs.forEach { env ->
      assert(map.put(env.name, env) == null) { "Environment '${env.name}' defined multiple times" }
      indexEnvs(map, env.children)
    }
    return map
  }

  private fun validateResolutionPaths(map: Map<String, Env>) {
    map.values.forEach { env ->
      env.resolutionPath.forEach {
        assert(map.contains(it)) { "Environment '${env.name}' refers undefined resolution path element '$it'" }
      }
    }
  }

  fun values(): Collection<Env> = index.values

  fun get(name: String) = index[name]

  fun contains(name: String) = index.containsKey(name)
}