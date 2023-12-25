package app.quickcase.sdk.spring.record.templating;

import java.util.List;

import app.quickcase.sdk.spring.record.RecordExtractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.Binding;
import com.github.mustachejava.Code;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.reflect.BaseObjectHandler;
import com.github.mustachejava.util.Wrapper;

public class ExtractorObjectHandler extends BaseObjectHandler {
    private final RecordExtractor extractor;

    public ExtractorObjectHandler(RecordExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Object coerce(Object object) {
        // Unwrap Optional<> returned by extractor
        final Object coerced = super.coerce(object);

        // Unwrap JsonNode returned by extractor
        if (coerced instanceof final JsonNode node) {
            if (node.isTextual()) {
                return node.textValue();
            }
            if (node.isNumber()) {
                return node.numberValue();
            }
        }
        return coerced;
    }

    @Override
    public Wrapper find(String name, List<Object> scopes) {
        return scopes1 -> coerce(extractor.extract(name));
    }

    @Override
    public Binding createBinding(String name, TemplateContext tc, Code code) {
        return new Binding() {
            // We find the wrapper just once since only the name is needed
            private final Wrapper wrapper = find(name, null);

            @Override
            public Object get(List<Object> scopes) {
                return wrapper.call(scopes);
            }
        };
    }
}
