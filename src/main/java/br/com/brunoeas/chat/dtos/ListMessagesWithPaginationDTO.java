package br.com.brunoeas.chat.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class ListMessagesWithPaginationDTO implements Serializable {

    @Positive(message = "REQUIRED_FIELD")
    @NotNull(message = "REQUIRED_FIELD")
    @Builder.Default
    @DefaultValue("100")
    @QueryParam(Fields.pageSize)
    private Long pageSize = 100L;

}
