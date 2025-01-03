import util.NetworkClient
import util.NetworkSimulator
import util.View
import kotlin.properties.Delegates


class ConfigurationError : RuntimeException()


/**
 * Interface for encryption methods.
 */
sealed interface EncryptionMethod {
  fun encrypt(str: String): String
  fun decrypt(str: String): String
}

/**
 * ROT13 "encryption".
 */
data object ROT13 : EncryptionMethod {
  // This allows writing "<Int> + <Char>" in the encrypt function below.
  operator fun Int.plus(c: Char): Int = this + c.code

  override fun encrypt(str: String): String =
    str.map { c ->
      when {
        (c >= 'A') && (c <= 'Z') ->
          ((c - 'A' + 13) % 26 + 'A').toChar()

        (c >= 'a') && (c <= 'z') ->
          ((c - 'a' + 13) % 26 + 'a').toChar()

        else -> c
      }
    }.joinToString("")

  override fun decrypt(str: String): String = encrypt(str)
}

/**
 * String-reversal "encryption".
 */
data object REVERSE : EncryptionMethod {
  override fun encrypt(str: String): String = str.reversed()

  override fun decrypt(str: String): String = encrypt(str)
}

//==============================================================================
// Messages
//==============================================================================
/**
 * Interface for all message types.
 */
sealed interface Message {
  val sender: Int

  fun encrypt(encryption: EncryptionMethod): Message
  fun decrypt(encryption: EncryptionMethod): Message
}

/**
 * A text message class.
 */
data class connect_mess(override val sender: Int) : Message {
  override fun encrypt(encryption: EncryptionMethod): connect_mess = this
  override fun decrypt(encryption: EncryptionMethod): connect_mess = this
  override fun toString(): String = "[$sender]"
}

data class TextMessage(
  override val sender: Int,
  val message: String,
  val color: String? = null
) : Message {
  override fun encrypt(encryption: EncryptionMethod): TextMessage =
    TextMessage(sender, encryption.encrypt(message), color)

  override fun decrypt(encryption: EncryptionMethod): TextMessage =
    TextMessage(sender, encryption.decrypt(message), color)

  override fun toString(): String = "[$sender] $message"
}

//==============================================================================
// Server
//==============================================================================
class ChatServer(
  private val network: NetworkSimulator<Message>,
) : NetworkClient<Message> {
  override var networkAddress by Delegates.notNull<Int>()

  private val clients: MutableSet<Int> = mutableSetOf()

  override fun handleMessage(message: Message): Boolean {
    return when (message) {
      is connect_mess -> connect(message)
      is TextMessage -> broadcast(message)

    }
  }

  private fun connect(message: connect_mess): Boolean {

      clients.add(message.sender)
    sendMessage(message.sender, connect_mess(networkAddress))
    return true
  }

  private fun broadcast(message: TextMessage): Boolean{
    clients.forEach { network.sendMessage(it, message) }
    return true
  }


  private fun sendMessage(clientId: Int, message: Message) {
      network.sendMessage(clientId, message)
  }

}

class ChatClient(
  private val network: NetworkSimulator<Message>
) : NetworkClient<Message> {
  val view = View()
  override var networkAddress by Delegates.notNull<Int>()
  private var serverId: Int? = null

  override fun handleMessage(message: Message): Boolean {
    when (message) {
      is connect_mess -> serverId = message.sender
      is TextMessage -> displayMessage(message)
    }
    return true
  }

  private fun displayMessage(message: TextMessage) {
      view.printMessage(message.sender, message.message)
  }

  fun connect(serverId: Int) {
    network.sendMessage(serverId, connect_mess(networkAddress))

  }

  fun send(message: String) {
    val textMessage = TextMessage(networkAddress, message)
    sendMessage(textMessage)
  }


  private fun sendMessage(message: Message) {
    val serverId = requireNotNull(serverId)
    network.sendMessage(serverId, message)
  }
}
