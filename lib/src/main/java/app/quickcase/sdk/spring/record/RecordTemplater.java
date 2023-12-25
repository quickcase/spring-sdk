package app.quickcase.sdk.spring.record;

import java.io.StringReader;
import java.io.StringWriter;

import app.quickcase.sdk.spring.record.templating.ExtractorObjectHandler;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

public class RecordTemplater {
    private final RecordExtractor extractor;

    public RecordTemplater(RecordExtractor extractor) {
        this.extractor = extractor;
    }

    public String render(String template) {
        final DefaultMustacheFactory mf = new DefaultMustacheFactory();
        mf.setObjectHandler(new ExtractorObjectHandler(extractor));

        final Mustache mustache = mf.compile(new StringReader(template), "template");

        final StringWriter writer = new StringWriter();
        mustache.execute(writer, new Object[0]);

        return writer.toString();
    }
}
