import util.Logger
import util.NetworkClient
import util.NetworkSimulator
import util.View
import kotlin.properties.Delegates

/**
 * Configuration class.
 */
data class ChatConfig(
  val authentication: Boolean,
  val color: Boolean,
  val encryption: EncryptionMethod?,
  val logging: Boolean
)

/**
 * Exception that is thrown when functionality is accessed that is not
 * available for the current configuration.
 */
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


// Code heavily inspired by rhe ChatApp_Preprocessor.kt file //

sealed interface Message {
  val sender: Int

  fun encrypt(encryption: EncryptionMethod): Message
  fun decrypt(encryption: EncryptionMethod): Message
}
// TODO: implement task a


data class connect_mess(override val sender: Int) : Message {
  override fun encrypt(encryption: EncryptionMethod): connect_mess = this
  override fun decrypt(encryption: EncryptionMethod): connect_mess = this
  override fun toString(): String = "[$sender]"
}

data class AuthenticationMessage(
  override val sender: Int,
  val username: String,
  val password: String
) : Message {
  override fun encrypt(encryption: EncryptionMethod): AuthenticationMessage =
    AuthenticationMessage(
      sender,
      encryption.encrypt(username),
      encryption.encrypt(password)
    )

  override fun decrypt(encryption: EncryptionMethod): AuthenticationMessage =
    AuthenticationMessage(
      sender,
      encryption.decrypt(username),
      encryption.decrypt(password)
    )

  override fun toString(): String = "[$sender] u=$username p=$password"
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
  private val config: ChatConfig,
  private val network: NetworkSimulator<Message>,
  private val registeredUsers: Map<String, String> = mapOf()
) : NetworkClient<Message> {
  override var networkAddress by Delegates.notNull<Int>()
  val logger = Logger()

  private val clients: MutableSet<Int> = mutableSetOf()
  private val unauthenticatedClients: MutableSet<Int> = mutableSetOf()

  override fun handleMessage(message: Message): Boolean {

    return when (message) {

      is connect_mess -> {
        if (config.logging) {
          logger.log("New client: ${message.sender}")
        }
        if (config.authentication) {
          unauthenticatedClients.add(message.sender)
        } else {
          clients.add(message.sender)
        }
        sendMessage(message.sender, connect_mess(networkAddress))
        true
      }
      is AuthenticationMessage -> {

        val decryptedMessage = if (config.encryption != null) {
          message.decrypt(config.encryption)
        } else {message}

        val sender = decryptedMessage.sender
        val username = decryptedMessage.username
        val password = decryptedMessage.password

        if (registeredUsers[username] == password) {
          if (config.logging) logger.log("Successfully authenticated client: $sender")
          unauthenticatedClients.remove(sender)
          clients.add(sender)
          sendMessage(sender, TextMessage(networkAddress, "Authentication successful."))
          return true
        } else {
          if (config.logging) logger.log("Failed to authenticate client: $sender")
          sendMessage(sender, TextMessage(networkAddress, "Authentication failed."))
          return false
        }
      }
      is TextMessage -> {
        val sender = message.sender
        if (config.authentication) {
          if (!isAuthenticated(sender)) {
            if (config.logging) logger.log("Rejected message from unauthenticated client: ${message.sender}")
            sendMessage( sender, TextMessage(networkAddress,"You must authenticate before sending messages."))
            return false
          }
        }
        if (config.logging) logger.log("Broadcasting message from sender ${message.sender}")
        clients.forEach { network.sendMessage(it, message) }
        return true
      }
      else -> false
    }
  }


  private fun isAuthenticated(clientId: Int): Boolean = clients.contains(clientId)

  private fun sendMessage(clientId: Int, message: Message) {
    if (config.encryption !=null)
      network.sendMessage(clientId, message.encrypt(config.encryption))
    else
      network.sendMessage(clientId, message)
  }


}

//==============================================================================
// Client
//==============================================================================
class ChatClient(
  private val config: ChatConfig,
  private val network: NetworkSimulator<Message>
) : NetworkClient<Message> {
  val view = View()
  val logger = Logger()
  override var networkAddress by Delegates.notNull<Int>()
  private var serverId: Int? = null
  private var isAuthenticated = false

  override fun handleMessage(message: Message): Boolean {
    if (config.logging) logger.log("Received message from sender ${message.sender}")
    val decryptedMessage = if (config.encryption != null) {
      message.decrypt(config.encryption)
    } else {message}

    when (decryptedMessage) {
      is connect_mess -> serverId = decryptedMessage.sender
      is AuthenticationMessage -> if (config.authentication && decryptedMessage.sender == serverId) {
        isAuthenticated = true
      }
      is TextMessage -> displayMessage(decryptedMessage)
    }
    return true
  }

  private fun displayMessage(message: TextMessage) {
    if (config.color && message.color != null) {
      view.printMessage(message.sender, message.message, message.color)
    } else {
      view.printMessage(message.sender, message.message)
    }
  }

  fun connect(serverId: Int) {
    network.sendMessage(serverId, connect_mess(networkAddress))

  }

  fun send(message: String) {
    val textMessage = TextMessage(networkAddress, message)
    if (config.logging) logger.log("Sending message: $textMessage")
    sendMessage(textMessage)
  }

  fun send(message: String, color: String) {
    if (config.color) {
      val textMessage = TextMessage(networkAddress, message, color)
      if (config.logging) logger.log("Sending message: $textMessage")
      sendMessage(textMessage)
    }
    else throw ConfigurationError()
  }

  fun authenticate(username: String, password: String) {
    if (!isAuthenticated) {
      val authenticationMessage = AuthenticationMessage(networkAddress, username, password)
      sendMessage(authenticationMessage)
      if (config.logging) logger.log("Sending authentication request: $authenticationMessage")
    }
  }

  private fun sendMessage(message: Message) {
    val serverId = requireNotNull(serverId)
    if (config.encryption != null) {
      network.sendMessage(serverId, message.encrypt(config.encryption))
    }
    else network.sendMessage(serverId, message)
  }

}
