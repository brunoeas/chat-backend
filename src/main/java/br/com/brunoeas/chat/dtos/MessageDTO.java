package br.com.brunoeas.chat.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MessageDTO implements Serializable {

    private String username;

    private String timestamp;

    private String text;

}
