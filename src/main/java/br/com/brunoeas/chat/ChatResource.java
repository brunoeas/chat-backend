package br.com.brunoeas.chat;

import br.com.brunoeas.chat.dtos.ListMessagesWithPaginationDTO;
import br.com.brunoeas.chat.dtos.SendMessageDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    ChatService chatService;

    @POST
    public Response sendMessage(final SendMessageDTO message) {
        this.chatService.sendMessage(message);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listMessages(@BeanParam final ListMessagesWithPaginationDTO request) {
        return Response.ok(this.chatService.listMessagesWithPagination(request)).build();
    }

    @DELETE
    public Response deleteAllMessages() {
        this.chatService.deleteAllMessages();
        return Response.ok().build();
    }

}
