package br.com.brunoeas.chat.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;

@Data
@Builder
public class PageDTO<T> implements Serializable {

    private LinkedList<T> list;

    private Long total;

}
