package io.github.goldbigdragon.resourcepack.compactor.compressor;

import com.googlecode.pngtastic.core.PngException;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import java.io.IOException;
import java.nio.file.Path;

public class PngCompressor implements FileCompressor {
    private int compressionLevel;

    public PngCompressor(int compressionLevel) {
        this.compressionLevel = 10 - compressionLevel * 10;
        if (this.compressionLevel > 9) {
            this.compressionLevel = 9;
        } else if (this.compressionLevel < 0) {
            this.compressionLevel = 0;
        }
    }

    @Override
    public void compress(Path path) throws IOException {
        PngOptimizer optimizer = new PngOptimizer("none");
        optimizer.setCompressor("zopfli", 1);

        PngImage pngImage = new PngImage(path.toAbsolutePath().toString(), "none");
        optimizer.optimize(pngImage, path.toAbsolutePath().toString(), false, compressionLevel);
    }
}
