package io.github.vickycmd.config.sources.mongo;

import io.github.vickycmd.config.model.ApplicationProperty;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.springframework.util.StringUtils;

public class ApplicationPropertyCodec implements Codec<ApplicationProperty> {

    @Override
    public ApplicationProperty decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        String id = reader.readObjectId("_id").toString();
        String key = reader.readString("key");
        String description = reader.readString("description");
        String value = reader.readString("value");
        reader.readEndDocument();
        return new ApplicationProperty(id, key, value, description);
    }

    @Override
    public void encode(BsonWriter writer, ApplicationProperty applicationProperty, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("_id", applicationProperty.getId());
        writer.writeString("key", applicationProperty.getKey());
        if (StringUtils.hasText(applicationProperty.getDescription()))
            writer.writeString("description", applicationProperty.getDescription());
        writer.writeString("value", applicationProperty.getValue());
        writer.writeEndDocument();

    }

    @Override
    public Class<ApplicationProperty> getEncoderClass() {
        return ApplicationProperty.class;
    }
}
