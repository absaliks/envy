package absaliks.envy.agent

fun main() {
  val envsList = Utils.envsList()
  val properties = propertiesList(envsList)
  System.out.println("""
    This is a java agent jar implementing "premain" method, it has to be attached to a java app before it starts. \
    This can be done with command line option: -javaagent:envy-agent.jar={environment}
    Available environments: $envsList
    
    Available properties:$properties
    """)
}

fun propertiesList(envsList: List<String>): String {
  return envsList
    .flatMap { env -> Utils.loadProperties(env).keys }
    .distinct()
    .sorted()
    .joinToString("\n - ", prefix = "\n - ")
}
