package br.com.brunoeas.chat.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SendMessageDTO implements Serializable {

    @NotBlank(message = "REQUIRED_FIELD")
    private String username;

    @NotBlank(message = "REQUIRED_FIELD")
    private String text;

}
