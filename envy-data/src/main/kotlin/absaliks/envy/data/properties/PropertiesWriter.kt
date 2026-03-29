package absaliks.envy.data.properties

import java.io.File
import java.io.FileWriter
import java.util.*

object PropertiesWriter {

  // Resolves to "envy-data/target/classes"
  private val buildPath = File(this.javaClass.protectionDomain.codeSource.location.path).absolutePath

  fun write(name: String, data: Map<String, Any>) {
    val props = Properties()
    props.putAll(data)
    FileWriter("${buildPath}/$name.properties").use {
      props.store(it, name)
    }
  }
}