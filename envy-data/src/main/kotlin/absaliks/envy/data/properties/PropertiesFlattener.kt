package absaliks.envy.data.properties

import absaliks.envy.data.env.Env
import absaliks.envy.data.env.EnvsIndex
import com.charleskorn.kaml.*

// helper class to avoid passing too many params
class PropertiesFlattener(
  private val data: YamlNode,
  private val env: Env,
  private val envsIndex: EnvsIndex
) {

  private val result = mutableMapOf<String, String>()

  fun flatten(): Map<String, String> {
    flatten("", data)
    return result
  }

  private fun flatten(path: String, node: YamlNode) {
    when (node) {
      is YamlScalar -> result[path] = node.content
      is YamlMap -> flatten(path, node)
      is YamlList -> flatten(path, node)
      is YamlTaggedNode -> throw UnsupportedOperationException("tagged nodes are not supported (${path})")
      is YamlNull -> {}
    }
  }

  private fun flatten(path: String, yamlMap: YamlMap) {
    val containsEnvironmentAwareValues = yamlMap.entries.keys.any { envsIndex.contains(it.content) }
    if (containsEnvironmentAwareValues) {
      env.resolutionPath.firstNotNullOfOrNull { yamlMap[it] }
        ?.let { value -> flatten(path, value) }
    }

    yamlMap.entries.forEach { (key, value) ->
      if (!envsIndex.contains(key.content)) {
        val newPath = if (path.isEmpty()) key.content else path + "." + key.content
        flatten(newPath, value)
      }
    }
  }

  private fun flatten(path: String, list: YamlList) =
    list.items.forEachIndexed { i, node -> flatten("$path[$i]", node) }
}