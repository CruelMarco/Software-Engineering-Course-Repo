import util.Logger
import util.NetworkClient
import util.NetworkSimulator
import util.View
import kotlin.properties.Delegates

/**
 * Configuration class.
 */

/**
 * Exception that is thrown when functionality is accessed that is not
 * available for the current configuration.
 */
class ConfigurationError : RuntimeException()


/**
 * Interface for encryption methods.
 */

sealed interface Message {
    val sender: Int
}

/**
 * A text message class.
 */
data class ConnectionMessage(override val sender: Int) : Message {
    override fun toString(): String = "[$sender]"
}

data class AuthenticationMessage(
    override val sender: Int,
    val username: String,
    val password: String
) : Message {
    override fun toString(): String = "[$sender] u=$username p=$password"
}

data class TextMessage(
    override val sender: Int,
    val message: String,
    val color: String? = null
) : Message {
    override fun toString(): String = "[$sender] $message"
}

//==============================================================================
// Server
//==============================================================================
class ChatServer(
    private val network: NetworkSimulator<Message>,
    private val registeredUsers: Map<String, String> = mapOf()
) : NetworkClient<Message> {
    override var networkAddress by Delegates.notNull<Int>()

    private val clients: MutableSet<Int> = mutableSetOf()
    private val unauthenticatedClients: MutableSet<Int> = mutableSetOf()

    override fun handleMessage(message: Message): Boolean {
        return when (message) {
            is ConnectionMessage -> connect(message)
            is AuthenticationMessage -> authenticate(message)
            is TextMessage -> broadcast(message)

        }
    }

    private fun connect(message: ConnectionMessage): Boolean {
        unauthenticatedClients.add(message.sender)
        sendMessage(message.sender, ConnectionMessage(networkAddress))
        return true
    }

    private fun broadcast(message: TextMessage): Boolean {
        val sender = message.sender
        if (!isAuthenticated(sender)) {
            sendMessage(sender, TextMessage(networkAddress, "You must authenticate before sending messages."))
            return false
        }
        clients.forEach { network.sendMessage(it, message) }
        return true
    }

    private fun authenticate(message: AuthenticationMessage): Boolean {
        val decryptedMessage = message
        val sender = decryptedMessage.sender
        val username = decryptedMessage.username
        val password = decryptedMessage.password

        if (registeredUsers[username] == password) {
            unauthenticatedClients.remove(sender)
            clients.add(sender)
            sendMessage(sender, TextMessage(networkAddress, "Authentication successful."))
            return true
        } else {
            sendMessage(sender, TextMessage(networkAddress, "Authentication failed."))
            return false
        }
    }

    private fun isAuthenticated(clientId: Int): Boolean = clients.contains(clientId)

    private fun sendMessage(clientId: Int, message: Message) {
        network.sendMessage(clientId, message)


    }
}
//==============================================================================
// Client
//==============================================================================
class ChatClient(
    private val network: NetworkSimulator<Message>
) : NetworkClient<Message> {
    val view = View()
    val logger = Logger()
    override var networkAddress by Delegates.notNull<Int>()
    private var serverId: Int? = null
    private var isAuthenticated = false

    override fun handleMessage(message: Message): Boolean {
        val decryptedMessage = message

        when (decryptedMessage) {
            is ConnectionMessage -> serverId = decryptedMessage.sender
            is AuthenticationMessage -> if (decryptedMessage.sender == serverId) {isAuthenticated = true}
            is TextMessage -> displayMessage(decryptedMessage)
        }
        return true
    }

    private fun displayMessage(message: TextMessage) {
        view.printMessage(message.sender, message.message)
    }

    fun connect(serverId: Int) {
        network.sendMessage(serverId, ConnectionMessage(networkAddress))

    }

    fun send(message: String) {
        val textMessage = TextMessage(networkAddress, message)
        sendMessage(textMessage)
    }

    fun authenticate(username: String, password: String) {
        if (!isAuthenticated) {
            val authenticationMessage = AuthenticationMessage(networkAddress, username, password)
            sendMessage(authenticationMessage)
        }
    }

    private fun sendMessage(message: Message) {
        val serverId = requireNotNull(serverId)
        network.sendMessage(serverId, message)
    }

}
