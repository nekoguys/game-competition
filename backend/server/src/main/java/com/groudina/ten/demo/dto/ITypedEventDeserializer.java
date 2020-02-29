package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ITypedEventDeserializer extends StdDeserializer<ITypedEvent> {
    protected ITypedEventDeserializer() {
        this(null);
    }

    protected ITypedEventDeserializer(Class<?> vc) {
        super(vc);
    }


    @Override
    public ITypedEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String type = node.get("type").asText();

        if (type.equals("EndRound")) {
            EndRoundEventDto endRoundEventDto = EndRoundEventDto.builder()
                    .isEndOfGame(node.get("endOfGame").asBoolean())
                    .roundNumber(node.get("roundNumber").asInt())
                    .build();
            return endRoundEventDto;
        } else { //NewRound
            NewRoundEventDto newRoundEventDto = NewRoundEventDto.builder()
                    .beginTime(node.get("beginTime").asLong())
                    .roundNumber(node.get("roundNumber").asInt())
                    .roundLength(node.get("roundLength").asInt())
                    .build();
            return newRoundEventDto;
        }
    }
}
