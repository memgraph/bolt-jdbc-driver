package org.memgraph.jdbc.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.neo4j.driver.types.Relationship;

import java.io.IOException;
import java.util.Map;

import static org.memgraph.jdbc.utils.DataConverterUtils.relationshipToMap;

public class RelationshipSerializer extends JsonSerializer<Relationship> {
    @Override
    public void serialize(Relationship value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        Map<String, Object> relMap = relationshipToMap(value);
        jsonGenerator.writeObject(relMap);
    }
}
