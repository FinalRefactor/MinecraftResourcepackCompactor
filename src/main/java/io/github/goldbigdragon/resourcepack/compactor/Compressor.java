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

import com.googlecode.pngtastic.core.PngException;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class Compressor extends Thread {

    private Main main;
    private CountDownLatch countDownLatch;

    public Compressor(Main main, int count, CountDownLatch countDownLatch) {
        this.main = main;
        this.setName("[Thread" + count + "] Created");
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            int remaining;
            long start;
            String fileName;

            while (main.running) {
                    start = System.currentTimeMillis();
                    fileName = main.imageFilePath.get(0);
                    main.imageFilePath.remove(fileName);
                    input = new File(fileName);
                    if (fileName.endsWith(".png")) {
                        Integer compressionLevel = 10 - ((int) main.compressPower * 10);
                        if (compressionLevel > 9) { compressionLevel = 9; } else if (compressionLevel < 0) {
                            compressionLevel = 0;
                        }

                        Integer iterations = 1;
                        String compressor = "zopfli";
                        String logLevel = "none";

                        PngOptimizer optimizer = new PngOptimizer(logLevel);
                        optimizer.setCompressor(compressor, iterations);

                        try {
                            PngImage pngImage = new PngImage(fileName, logLevel);
                            optimizer.optimize(pngImage, fileName, false, compressionLevel);

                        } catch (PngException | IOException e) {
                            e.printStackTrace();
                        }

                        remaining = main.fileCount - (main.jsonFilePath.size() + main.imageFilePath.size());
                        System.out.println("[" +
                                           remaining +
                                           " / " +
                                           main.totalSize +
                                           "]" +
                                           fileName +
                                           " : " +
                                           (System.currentTimeMillis() - start) +
                                           "ms");
                        continue;
                    }
                remaining = main.fileCount - (main.jsonFilePath.size() + main.imageFilePath.size());
                System.out.println("[" +
                                   remaining +
                                   " / " +
                                   main.fileCount +
                                   "]" +
                                   fileName +
                                   " : " +
                                   (System.currentTimeMillis() - start) +
                                   "ms");
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("[Thread" + getName() + "] Finished work");
        countDownLatch.countDown();
    }

    public String getConvertedTextureName(int order) {
        String amount = Integer.toString(order / 26);
        order %= 26;
        return Character.toString((char) (order + 97)) + (amount.equals("0") ? "" : amount);
    }
}
