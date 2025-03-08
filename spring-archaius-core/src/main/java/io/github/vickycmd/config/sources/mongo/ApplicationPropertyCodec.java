package io.github.vickycmd.config.sources.mongo;

import io.github.vickycmd.config.model.ApplicationProperty;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.springframework.util.StringUtils;

/**
 * A codec for encoding and decoding {@link ApplicationProperty} objects to and from BSON format.
 *
 * <p>This class implements the {@link Codec} interface to provide custom serialization
 * and deserialization logic for {@link ApplicationProperty} instances. It reads and writes
 * BSON documents, mapping fields such as "_id", "key", "description", and "value" to the
 * corresponding properties of the {@link ApplicationProperty} class.
 *
 * <p>The {@code decode} method reads a BSON document from a {@link BsonReader} and constructs
 * an {@link ApplicationProperty} object. The {@code encode} method writes an
 * {@link ApplicationProperty} object to a {@link BsonWriter} as a BSON document.
 *
 * <p>Usage example:
 * <pre>
 *     ApplicationPropertyCodec codec = new ApplicationPropertyCodec();
 *     ApplicationProperty property = codec.decode(reader, decoderContext);
 *     codec.encode(writer, property, encoderContext);
 * </pre>
 *
 * <p>Note: The "description" field is only written if it contains text, as determined by
 * {@link StringUtils#hasText}.
 * @see ApplicationProperty
 * @see Codec
 *
 * Author: Vicky
 * Version: 1.0
 * Since: 1.0
 */
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
