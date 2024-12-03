package br.com.brunoeas.chat.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class UserDTO implements Serializable {

    private UUID code;

    private String name;

}
