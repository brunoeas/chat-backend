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

        final SetCommands<String, UserDTO> setCommand = this.redisDataSource.set(UserDTO.class);
        final Set<UserDTO> users = setCommand.smembers(this.usersKey);
        final UserDTO user = users.stream()
                .filter(userStream -> message.getUsername().equalsIgnoreCase(userStream.getName()))
                .findFirst()
                .orElseGet(() -> {
                    final UserDTO newUser = UserDTO.builder().code(UUID.randomUUID()).name(message.getUsername()).build();
                    setCommand.sadd(this.usersKey, newUser);
                    return newUser;
                });

        final MessageDTO messageDTO = MessageDTO.builder()
                .username(user.getCode().toString())
                .timestamp(LocalDateTime.now())
                .text(message.getText())
                .build();
        this.redisDataSource.list(MessageDTO.class).lpush(this.chatMessagesKey, messageDTO);
    }

    public PageDTO<MessageDTO> listMessagesWithPagination(final ListMessagesWithPaginationDTO request) {
        final ListCommands<String, MessageDTO> listCommand = this.redisDataSource.list(MessageDTO.class);

        final List<MessageDTO> rawMessages = listCommand.lrange(this.chatMessagesKey, request.getPageIndex(), request.getPageSize());
        final Set<UserDTO> users = this.redisDataSource.set(UserDTO.class).smembers(this.usersKey);

        final LinkedList<MessageDTO> messageList = rawMessages.stream()
                .map(raw -> this.mapUserNameInDTO(raw, users))
                .collect(Collectors.toCollection(LinkedList::new));

        return PageDTO.<MessageDTO>builder()
                .total(listCommand.llen(this.chatMessagesKey))
                .list(messageList)
                .build();
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
