package br.com.brunoeas.chat.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageDTO implements Serializable {

    private String username;

    private LocalDateTime timestamp;

    private String text;

}
