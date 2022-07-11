package org.neo4j.jdbc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Point;
import org.neo4j.driver.types.Relationship;


public class JSONUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER;
    static {
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        SimpleModule module = new SimpleModule("MemgraphJdbcSerializer");
        module.addSerializer(Node.class, new NodeSerializer());
        module.addSerializer(Relationship.class, new RelationshipSerializer());
        module.addSerializer(Path.class, new PathSerializer());
        module.addSerializer(Point.class, new PointSerializer()); // TODO add more serializers in order to remove ObjectConverter
        OBJECT_MAPPER.registerModule(module);
        OBJECT_MAPPER.setDefaultPrettyPrinter(new MemgraphJdbcPrettyPrinter());
        OBJECT_WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    }

    public static String writeValueAsString(Object value) {
        try {
            return OBJECT_WRITER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertValue(Object value, Class<T> type) {
        try {
            return OBJECT_MAPPER.convertValue(value, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
