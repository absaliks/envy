package absaliks.envy.agent

import java.lang.instrument.Instrumentation
import java.util.*

object EnvyAgent {

  @JvmStatic
  fun premain(agentArgs: String?, inst: Instrumentation?) {
    val env = getEnv(agentArgs)
    System.getProperties().forEach { (k: Any?, v: Any?) ->

      val key = Objects.toString(k)
      if (key.startsWith($$"${envy.") && key.endsWith("}")) {
        println("$k=$v")
      }
    }
  }

  private fun getEnv(agentArgs: String?): String {
    if (agentArgs != null) return agentArgs

    val availableEnvs = Utils.envsList()

    val envList = availableEnvs.joinToString(", ", prefix = "[", postfix = "]")
    throw IllegalArgumentException(
      """
      Missing environment argument.
        Available envs: $envList.
        How to apply: -javaagent:envy-agent.jar=prod-eu-west
      """.trimIndent()
    )
  }
}
