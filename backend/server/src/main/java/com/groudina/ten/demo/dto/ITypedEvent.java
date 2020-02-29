package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@JsonDeserialize(using = ITypedEventDeserializer.class)
public interface ITypedEvent extends Serializable {
    public String getType();
}
