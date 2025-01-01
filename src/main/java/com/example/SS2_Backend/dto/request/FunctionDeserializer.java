package com.example.SS2_Backend.dto.request;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@Deprecated
public class FunctionDeserializer extends StdDeserializer<ArrayList<String>> {
    protected FunctionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ArrayList<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            JsonNode evaluationNode = node.get("evaluateFunction");
            ArrayList<String> functions = new ArrayList<>();
            if (evaluationNode != null && evaluationNode.isArray()) {
                Iterator<JsonNode> evaluateIterator = evaluationNode.elements();
                int i=0;
                while (evaluateIterator.hasNext()) {
                    JsonNode evaluateNode = evaluateIterator.next();
                    functions.add(evaluateNode.get(Integer.toString(i)).asText());
                    i++;
                }
            }
            return functions;
        }
    }

