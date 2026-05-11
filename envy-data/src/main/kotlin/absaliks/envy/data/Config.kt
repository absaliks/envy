package absaliks.envy.data

import absaliks.envy.data.env.Env
import com.charleskorn.kaml.YamlNode
import kotlinx.serialization.Serializable

@Serializable
data class Config(
  val defaults: Defaults?,
  val envs: List<Env>,
  val data: YamlNode
)

@Serializable
data class Defaults(
  val k8sNamespace: String?,
)
