package absaliks.envy.data.util

import java.io.File

object FileUtil {
  fun readResource(filename: String): String {
    val resource = this::class.java.getResource(filename) ?: throw RuntimeException("Resource not found: $filename")
    return resource.readText(Charsets.UTF_8)
  }

  fun readFile(filename: String): String {
    return File(filename).readText(Charsets.UTF_8)
  }
}