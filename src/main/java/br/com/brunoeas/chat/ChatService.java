package br.com.brunoeas.chat;

import br.com.brunoeas.chat.dtos.*;
import br.com.brunoeas.chat.exception.BeanValidator;
import br.com.brunoeas.chat.exception.ChatException;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class ChatService {

    @Inject
    RedisDataSource redisDataSource;

    @ConfigProperty(name = "config-redis.keys.chat-messages")
    String chatMessagesKey;

    @ConfigProperty(name = "config-redis.keys.users")
    String usersKey;

    public void sendMessage(final SendMessageDTO message) throws ChatException {
        BeanValidator.validate(message);

        log.infof("Message: %s", message);

        message.setUsername(message.getUsername().trim());
        message.setText(message.getText().trim());

        final UserDTO user = this.retrieveUserOrCreateNewOne(message.getUsername());

        final MessageDTO messageDTO = MessageDTO.builder()
                .username(user.getCode().toString())
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .text(message.getText())
                .build();
        this.redisDataSource.list(MessageDTO.class).rpush(this.chatMessagesKey, messageDTO);
    }

    public PageDTO<MessageDTO> listMessagesWithPagination(final ListMessagesWithPaginationDTO request) throws ChatException {
        BeanValidator.validate(request);

        final ListCommands<String, MessageDTO> listCommand = this.redisDataSource.list(MessageDTO.class);
        final long total = listCommand.llen(this.chatMessagesKey);

        final List<MessageDTO> rawMessages = listCommand.lrange(this.chatMessagesKey, total - request.getPageSize(), -1);
        if (!rawMessages.isEmpty()) {
            final Set<UserDTO> users = this.redisDataSource.set(UserDTO.class).smembers(this.usersKey);

            final LinkedList<MessageDTO> messageList = rawMessages.stream()
                    .map(raw -> this.mapUserNameInDTO(raw, users))
                    .collect(Collectors.toCollection(LinkedList::new));

            return PageDTO.<MessageDTO>builder()
                    .total(total)
                    .list(messageList)
                    .build();
        } else {
            return PageDTO.<MessageDTO>builder()
                    .total(total)
                    .list(new LinkedList<>())
                    .build();
        }
    }

    public void deleteAllMessages() {
        this.redisDataSource.key(String.class).del(this.chatMessagesKey);
    }

    private UserDTO retrieveUserOrCreateNewOne(final String username) {
        final SetCommands<String, UserDTO> setCommands = this.redisDataSource.set(UserDTO.class);

        return setCommands.smembers(this.usersKey)
                .stream()
                .filter(userStream -> username.equalsIgnoreCase(userStream.getName()))
                .findFirst()
                .orElseGet(() -> {
                    final UserDTO newUser = UserDTO.builder()
                            .code(UUID.randomUUID())
                            .name(username)
                            .build();
                    setCommands.sadd(this.usersKey, newUser);
                    return newUser;
                });
    }

    private MessageDTO mapUserNameInDTO(final MessageDTO messageDTO, final Collection<UserDTO> users) {
        final UserDTO user = users.stream()
                .filter(u -> Objects.equals(u.getCode().toString(), messageDTO.getUsername()))
                .findFirst()
                .orElse(UserDTO.builder().name("UNKNOWN").build());
        messageDTO.setUsername(user.getName());
        return messageDTO;
    }

}
