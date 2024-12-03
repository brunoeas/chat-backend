package br.com.brunoeas.chat.exception;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Collections;
import java.util.Map;

@Provider
@Priority(1000)
public class ExceptionHandler implements ExceptionMapper<Exception> {

    /**
     * Map an exception to a {@link Response}. Returning {@code null} results in a
     * {@link Response.Status#NO_CONTENT} response. Throwing a runtime exception results in a
     * {@link Response.Status#INTERNAL_SERVER_ERROR} response.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(final Exception exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ChatErrorListDTO.builder()
                        .errors(Collections.singletonList(
                                ChatErrorDTO.builder()
                                        .code(ErrorMessageEnum.UNKNOWN_ERROR.getCode())
                                        .message(ErrorMessageEnum.UNKNOWN_ERROR.getMessage())
                                        .monitoredVariables(Map.of(MonitoredVariableKeys.REASON, exception.toString()))
                                        .build()
                        ))
                        .build())
                .build();
    }

}
