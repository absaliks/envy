package absaliks.envy.data

import absaliks.envy.data.env.Env
import com.charleskorn.kaml.YamlNode
import kotlinx.serialization.Serializable

@Serializable
class Config(
  val envs: List<Env>,
  val data: YamlNode
)