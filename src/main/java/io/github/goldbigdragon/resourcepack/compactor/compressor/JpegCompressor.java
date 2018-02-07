/*
 * This file is part of MinecraftResourceCompactor.
 *
 * MinecraftResourcepackCompactor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * MinecraftResourcepackCompactor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.goldbigdragon.resourcepack.compactor.compressor;

import io.github.goldbigdragon.resourcepack.compactor.OldMain;
import io.github.goldbigdragon.resourcepack.compactor.util.Util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JpegCompressor implements FileCompressor {
    private JpegCompressor() {
    }

    private static final class SingletonHolder {
        private static final JpegCompressor INSTANCE = new JpegCompressor();
    }

    public static JpegCompressor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void compress(Path path) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(Util.getFileExtension(path)).next();
        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(Files.newOutputStream(path))) {
            BufferedImage image = ImageIO.read(path.toFile());
            writer.setOutput(outputStream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(OldMain.compressPower);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            writer.dispose();
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".jpg") || name.endsWith(".jpeg");
    }
}
