package tech.rayline.core.parse;

import lombok.Data;

import java.io.File;
import java.lang.reflect.Field;

@Data final class RegisteredResourceFile {
    private final ResourceFile annotation;
    private final File file;
    private final Object instanceBound;
    private final Field field;
    private final ResourceFileHook<?> hook;
}
