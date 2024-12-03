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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

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

        final String formattedMessage = String.format("{%s}:{%s}:%s", user.getCode(), LocalDateTime.now(), message.getText());
        this.redisDataSource.list(String.class).lpush(this.chatMessagesKey, formattedMessage);
    }

    public PageDTO<MessageDTO> listMessagesWithPagination(final ListMessagesWithPaginationDTO request) {
        final ListCommands<String, String> listCommand = this.redisDataSource.list(String.class);

        final List<String> rawMessages = listCommand.lrange(this.chatMessagesKey, request.getPageIndex(), request.getPageSize());

        final LinkedList<MessageDTO> messageList = rawMessages.stream()
                .map(this::mapRawMessageToDTO)
                .collect(Collectors.toCollection(LinkedList::new));

        return PageDTO.<MessageDTO>builder()
                .total(listCommand.llen(this.chatMessagesKey))
                .list(messageList)
                .build();
    }

    private MessageDTO mapRawMessageToDTO(final String raw) {
        final Pattern pattern = Pattern.compile("\\{(.*?)}");
        final Matcher matcher = pattern.matcher(raw);

        String username = null;
        String uuidUser = null;
        if (matcher.find()) {
            uuidUser = matcher.group(1);
            final String uuidUserFinal = uuidUser;
            final Set<UserDTO> users = this.redisDataSource.set(UserDTO.class).smembers(this.usersKey);
            final UserDTO user = users.stream()
                    .filter(u -> Objects.equals(u.getCode().toString(), uuidUserFinal))
                    .findFirst()
                    .orElse(UserDTO.builder().name("UNKNOWN").build());
            username = user.getName();
        }

        String timestamp = null;
        if (matcher.find()) {
            timestamp = matcher.group(1);
        }

        final String text = raw.split(String.format("{%s}:{%s}:", uuidUser, timestamp))[0];

        return MessageDTO.builder()
                .username(username)
                .timestamp(isNull(timestamp) ? null : LocalDateTime.parse(timestamp))
                .text(text)
                .build();
    }

}
