package absaliks.envy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EnvyApplication

fun main(args: Array<String>) {
  runApplication<EnvyApplication>(*args)
}
