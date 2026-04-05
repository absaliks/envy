package absaliks.envy.agent

import java.lang.instrument.Instrumentation
import java.util.Objects
import java.util.Properties

object EnvyAgent {

  fun premain(agentArgs: String?, inst: Instrumentation?) {
    System.getProperties().forEach { (k: Any?, v: Any?) ->
      val env = agentArgs ?: throw IllegalArgumentException("Missing required environment argument")

      val key = Objects.toString(k)
      if (key.startsWith($$"${envy.") && key.endsWith("}")) {
        println("$k=$v")
      }
    }
  }

  private fun loadProperties(env: String): Properties {
    val resourcePath = "/envs/$env.properties"
    val stream = EnvyAgent::class.java.getResourceAsStream(resourcePath)
      ?: throw IllegalArgumentException("No properties file found for environment '$env': $resourcePath")
    return stream.use { Properties().apply { load(it) } }
  }
}
