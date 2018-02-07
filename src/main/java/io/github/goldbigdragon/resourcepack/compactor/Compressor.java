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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compressor extends Thread {
    public Compressor(int count) {
        this.setName("[Thread" + count + "] Created");
    }

    @Override
    public void run() {
        try {
            BufferedReader br;
            BufferedWriter bw;
            File file;
            StringBuilder sb;
            StringBuilder originalSb;
            String line;
            String[] textures;
            boolean copyrightIndication;
            int textureNumber;
            ArrayList<String> originalTextureName;
            ArrayList<String> tinifyTextureName;

            File input;

            int remainning;
            long start;
            String fileName;

            while (OldMain.started) {
                if (OldMain.compressText && !OldMain.jsonFilePath.isEmpty()) {
                } else if (OldMain.compressImage && !OldMain.imageFilePath.isEmpty()) {
                    start = System.currentTimeMillis();
                    fileName = OldMain.imageFilePath.get(0);
                    OldMain.imageFilePath.remove(fileName);
                    input = new File(fileName);
                    if (fileName.endsWith(".png")) {
                        Integer compressionLevel = 10 - ((int) OldMain.compressPower * 10);
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

                        remainning = OldMain.totalSize - (OldMain.jsonFilePath.size() + OldMain.imageFilePath.size());
                        System.out.println("[" +
                                           remainning +
                                           " / " +
                                           OldMain.totalSize +
                                           "]" +
                                           fileName +
                                           " : " +
                                           (System.currentTimeMillis() - start) +
                                           "ms");
                        continue;
                    }
                } else {
                    break;
                }
                remainning = OldMain.totalSize - (OldMain.jsonFilePath.size() + OldMain.imageFilePath.size());
                System.out.println("[" +
                                   remainning +
                                   " / " +
                                   OldMain.totalSize +
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
        if (OldMain.threads.size() == 1) {
            OldMain.sendResult();
        }
        OldMain.threads.remove(this);
    }

    public String getConvertedTextureName(int order) {
        String amount = Integer.toString(order / 26);
        order %= 26;
        return Character.toString((char) (order + 97)) + (amount.equals("0") ? "" : amount);
    }
}
