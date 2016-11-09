/*package gr.cite.femme.application.core;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return new ObjectMapper()
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true);
    }
}*/