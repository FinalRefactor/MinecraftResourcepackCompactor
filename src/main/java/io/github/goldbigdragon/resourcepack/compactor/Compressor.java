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

package io.github.goldbigdragon.resourcepack.compactor;

import com.google.common.io.MoreFiles;
import io.github.goldbigdragon.resourcepack.compactor.compressor.FileCompressor;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class Compressor extends Thread {

    private Main main;
    private CountDownLatch doneSignal;

    public Compressor(Main main, int count, CountDownLatch doneSignal) {
        this.main = main;
        this.setName("[Thread" + count + "] Created");
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        while (true) {
            Optional<Path> optionalFile = main.getNextFileToProcess();
            if (!optionalFile.isPresent()) {
                break;
            }

            Path file = optionalFile.get();
            processFile(file);

            System.out.println("[" +
                    main.getRemaining() +
                    " / " +
                    main.originalFileCount +
                    "]" +
                    file.getFileName() +
                    " : " +
                    (System.currentTimeMillis() - start) +
                    "ms");
        }

        System.out.println("[Thread" + getName() + "] Finished work");
        doneSignal.countDown();
    }

    private void processFile(Path file) {
        String fileExtension = MoreFiles.getFileExtension(file);
        if (main.compressorMap.containsKey(fileExtension)) {
            FileCompressor compressor = main.compressorMap.get(fileExtension);
            try {
                compressor.compress(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
