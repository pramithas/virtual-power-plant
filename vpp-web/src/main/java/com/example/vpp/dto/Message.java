package com.example.vpp.dto;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class Message<T> implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private T payload;

  public Message( T payload) {
    super();
    this.payload = payload;
  }

  public Message(String traceId, T payload) {
    super();
    this.payload = payload;
  }

  public Message() {
    super();
  }
}
