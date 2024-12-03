package br.com.brunoeas.chat;

import br.com.brunoeas.chat.dtos.SendMessageDTO;
import br.com.brunoeas.chat.exception.BeanValidator;
import br.com.brunoeas.chat.exception.ChatException;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;

@JBossLog
@ApplicationScoped
public class ChatService {

    @Inject
    RedisDataSource redisDataSource;

    @ConfigProperty(name = "config-redis.keys.chat-list")
    String chaveListaChat;

    public void sendMessage(final SendMessageDTO message) throws ChatException {
        BeanValidator.validate(message);

        log.infof("Message: %s", message);

        message.setUsername(message.getUsername().trim());
        message.setText(message.getText().trim());

        final String formattedMessage = String.format(
                "{%s}:{%s}:%s", message.getUsername(), LocalDateTime.now(), message.getText()
        );
        this.redisDataSource.list(String.class).lpush(this.chaveListaChat, formattedMessage);
    }

}
