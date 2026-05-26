package absaliks.envy.data.properties

import absaliks.envy.data.Config
import absaliks.envy.data.env.Env
import absaliks.envy.data.env.EnvsIndex
import com.charleskorn.kaml.*

/**
 * Converts YAML properties into flat env-specific key/value pairs
 */
class PropertiesFlattener(
  private val config: Config,
  private val env: Env,
  private val envsIndex: EnvsIndex
) {

  private val result = mutableMapOf<String, String>()

  fun flatten(): Map<String, String> {
    flatten("", config.data, setOf())
    addProperty("__env.k8s-context", env.k8sContext)
    addProperty("__env.k8s-namespace", config.defaults?.k8sNamespace)
    return result
  }

  private fun addProperty(key: String, value: String?) {
    if (value != null) {
      result[key] = value
    }
  }

  private fun flatten(path: String, node: YamlNode, envsFilter: Set<String>) {
    when (node) {
      is YamlScalar -> if (envsFilter.isEmpty() || envsFilter.contains(env.name)) result[path] = node.content
      is YamlMap -> flatten(path, node, envsFilter)
      is YamlList -> flatten(path, node, envsFilter)
      is YamlTaggedNode -> throw UnsupportedOperationException("tagged nodes are not supported (${path})")
      is YamlNull -> {}
    }
  }

  private fun flatten(path: String, yamlMap: YamlMap, envsFilter: Set<String>) {
    val envsFilter = getEnvsFilter(yamlMap, envsFilter)
    val containsEnvironmentAwareValues = yamlMap.entries.keys.any { envsIndex.contains(it.content) }
    if (containsEnvironmentAwareValues) {
      env.resolutionPath.firstNotNullOfOrNull { yamlMap[it] }
        ?.let { value -> flatten(path, value, envsFilter) }
    }

    yamlMap.entries.forEach { (key, value) ->
      if (!envsIndex.contains(key.content)) {
        val newPath = if (path.isEmpty()) key.content else path + "." + key.content
        flatten(newPath, value, envsFilter)
      }
    }
  }

  // A property can declare __envs filter, so that they were added only in specified envs
  // (useful for non region specific properties)
  private fun getEnvsFilter(yamlMap: YamlMap, envsFilter: Set<String>): Set<String> =
    yamlMap.get<YamlList>("__envs")?.items?.map { (it as YamlScalar).content }?.toSet()
      ?: envsFilter

  private fun flatten(path: String, list: YamlList, envsFilter: Set<String>) =
    list.items.forEachIndexed { i, node -> flatten("$path[$i]", node, envsFilter) }
}
