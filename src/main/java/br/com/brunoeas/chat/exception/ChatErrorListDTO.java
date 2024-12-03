package br.com.brunoeas.chat.exception;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ChatErrorListDTO implements Serializable {

    private List<ChatErrorDTO> errors;

}
