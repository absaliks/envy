package absaliks.envy.data.env

import kotlinx.serialization.Serializable

@Serializable
data class Env(
  val name: String,
  val k8sContext: String? = null,
  val resolutionPath: List<String> = listOf(),
  val children: List<Env> = listOf()
) {

  fun isLeaf() = children.isEmpty()

}