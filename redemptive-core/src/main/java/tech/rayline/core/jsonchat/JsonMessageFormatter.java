package tech.rayline.core.jsonchat;

import lombok.Data;
import tech.rayline.core.plugin.Formatter;

@Data public final class JsonMessageFormatter {
    private final Formatter.FormatBuilder builder;

    public String get() {
        return builder.get();
    }

    public Formatter.FormatBuilder with(String key, Object value) {
        return builder.with(key, value);
    }

    public Formatter.FormatBuilder withModifier(String key, Object value) {
        return builder.withModifier(key, value);
    }
}
