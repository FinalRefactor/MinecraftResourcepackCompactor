package io.github.goldbigdragon.resourcepack.compactor.compressor;

import com.googlecode.pngtastic.core.PngException;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import java.io.IOException;
import java.nio.file.Path;

public class PngCompressor implements FileCompressor {
    private int compressionLevel;

    public PngCompressor(int compressPower) {
        this.compressionLevel = 10 - compressPower * 10;
    }

    @Override
    public void compress(Path path) {
        if (compressionLevel > 9) {
            compressionLevel = 9;
        } else if (compressionLevel < 0) {
            compressionLevel = 0;
        }

        Integer iterations = 1;
        String compressor = "zopfli";
        String logLevel = "none";

        PngOptimizer optimizer = new PngOptimizer(logLevel);
        optimizer.setCompressor(compressor, iterations);

        try {
            PngImage pngImage = new PngImage(path.toAbsolutePath().toString(), logLevel);
            optimizer.optimize(pngImage, path.toAbsolutePath().toString(), false, compressionLevel);
        } catch (PngException | IOException e) {
            e.printStackTrace();
        }
    }
}
