package tech.rayline.core.parse;

import tech.rayline.core.plugin.RedemptivePlugin;

import java.io.File;

public interface ResourceFileHook<RawType> {
    <T> T read(RedemptivePlugin plugin, File file, Class<T> type) throws Exception;
    RawType readRaw(RedemptivePlugin plugin, File file) throws Exception;

    void write(RedemptivePlugin plugin, Object o, File file) throws Exception;
    void writeRaw(RedemptivePlugin plugin, Object type, File file) throws Exception;
}
