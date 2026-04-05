package absaliks.envy.agent

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


object Utils {

  fun envsList(): List<String> {
    val uri = Path.of(Utils::class.java.getResource("/envs/")!!.toURI())
    return Files.list(uri)
      .map { filenameWithoutExtension(it) }
      .toList()
  }

  private fun filenameWithoutExtension(path: Path): String {
    val filename = path.fileName?.toString() ?: throw java.lang.IllegalArgumentException("Bad path: $path")
    val i = filename.lastIndexOf('.')
    return if (i == -1) filename else filename.substring(0, i)
  }

  fun loadProperties(env: String): Map<String, String> {
    val path = "/envs/$env.properties"
    val stream = getResourceAsStream(path)
    val result = HashMap<String, String>()
    try {
      val props = Properties()
      props.load(stream)
      val iter = props.entries.iterator()
      while (iter.hasNext()) {
        val entry = iter.next()
        result[entry.key.toString()] = entry.value.toString()
      }
    } finally {
      stream.close()
    }
    return result
  }

  private fun getResourceAsStream(path: String): InputStream =
    Utils::class.java.getResourceAsStream(path)
      ?: throw IllegalArgumentException("Resource not found: $path")
}