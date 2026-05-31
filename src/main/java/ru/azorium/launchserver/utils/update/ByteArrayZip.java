package ru.azorium.launchserver.utils.update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ByteArrayZip {

    public static byte[] create(List<Path> files) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Path file : files) {
                try (InputStream inputStream = Files.newInputStream(file)) {
                    ZipEntry entry = new ZipEntry(file.toString());
                    zos.putNextEntry(entry);

                    byte[] buffer = new byte[1048576];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }
            }
            zos.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return baos.toByteArray();
    }

}
