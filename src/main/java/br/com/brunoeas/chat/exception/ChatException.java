package br.com.brunoeas.chat.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper = true)
@Getter
public class ChatException extends RuntimeException {

    private final Collection<ChatErrorDTO> errors;

    public ChatException(final Collection<ChatErrorDTO> errors) {
        super();
        Objects.requireNonNull(errors);
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Field \"errorMessageEnumMap\" is empty.");
        }
        this.errors = errors;
    }

    public ChatException(final ErrorMessageEnum errorMessageEnum, final Map<String, String> monitoredVariables) {
        super();
        Objects.requireNonNull(errorMessageEnum);
        final Map<String, String> monitoredVariablesNewInstance = isNull(monitoredVariables) ? new HashMap<>() : new HashMap<>(monitoredVariables);
        this.errors = Collections.singletonList(
                ChatErrorDTO.builder()
                        .code(errorMessageEnum.getCode())
                        .message(errorMessageEnum.getMessage())
                        .monitoredVariables(monitoredVariablesNewInstance)
                        .build()
        );
    }

    public ChatException(final ErrorMessageEnum errorMessageEnum) {
        this(errorMessageEnum, null);
    }

}
