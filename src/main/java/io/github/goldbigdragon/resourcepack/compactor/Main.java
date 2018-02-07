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

import io.github.goldbigdragon.resourcepack.compactor.compressor.FileCompressor;
import io.github.goldbigdragon.resourcepack.compactor.compressor.JpegCompressor;
import io.github.goldbigdragon.resourcepack.compactor.compressor.JsonCompressor;
import io.github.goldbigdragon.resourcepack.compactor.compressor.PngCompressor;
import io.github.goldbigdragon.resourcepack.compactor.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Main {

    public ResourceBundle langBundle;

    public Path rootDirectory;
    public ArrayDeque<Path> contents;

    public long originalSize;
    public long originalFileCount;

    public int threadCount;
    public Map<String, FileCompressor> compressorMap = new HashMap<>();

    public boolean compressText;
    public boolean compressImage;
    public boolean searchInnerDir;
    public float compressionQuality;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        promptOptions();
        printOptions();

        JpegCompressor jpegCompressor = new JpegCompressor(compressionQuality);
        compressorMap.put("jpg", jpegCompressor);
        compressorMap.put("jpeg", jpegCompressor);
        JsonCompressor jsonCompressor = new JsonCompressor();
        compressorMap.put("json", jsonCompressor);
        compressorMap.put("mcmeta", jsonCompressor);
        PngCompressor pngCompressor = new PngCompressor((int) (10 - compressionQuality * 10));
        compressorMap.put("png", pngCompressor);

        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Compressor(this, i, doneSignal).start();
        }
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            return;
        }

        try {
            printResult();
        } catch (IOException ignored) {
        }
    }

    public synchronized Optional<Path> getNextFileToProcess() {
        if (contents.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(contents.remove());
        }
    }

    public synchronized int getRemaining() {
        return contents.size();
    }

    private void promptOptions() {
        Scanner scanner = new Scanner(System.in);

        while (langBundle == null) {
            System.out.println(
                    "[Select a language]: en-US[English], ko-KR[한국어], ja-JP[日本語], zh-TW[中国的], ru-RU[русский]");
            String code = scanner.nextLine();
            try {
                langBundle = ResourceBundle.getBundle("Lang", Locale.forLanguageTag(code));
            } catch (MissingResourceException e) {
                System.out.println("The locale is not supported: " + code);
            }
        }

        printlnLocalized("type.resource-pack-path");
        System.out.print(" ▶ ");
        String pathString = scanner.nextLine();

        loop:while (true) {
            printlnLocalized("select.compress-mode");
            System.out.print(" ▶ ");
            switch (scanner.nextLine()) {
                case "1":
                    compressText = true;
                    break loop;
                case "2":
                    compressImage = true;
                    break loop;
                case "3":
                    compressText = true;
                    compressImage = true;
                    break loop;
            }
        }

        if (compressImage) {
            loop:while (true) {
                printlnLocalized("select.compress-power");
                System.out.print(" ▶ ");

                switch (scanner.nextLine()) {
                    case "1":
                        compressionQuality = 0.9F;
                        break loop;
                    case "2":
                        compressionQuality = 0.7F;
                        break loop;
                    case "3":
                        compressionQuality = 0.5F;
                        break loop;
                    case "4":
                        compressionQuality = 0.3F;
                        break loop;
                    case "5":
                        while (true) {
                            printlnLocalized("select.compress-power.type");
                            System.out.print(" ▶ ");
                            String compressPowerInput = scanner.nextLine();
                            int compressionPower;
                            try {
                                compressionPower = Integer.parseInt(compressPowerInput);
                            } catch (NumberFormatException e) {
                                continue;
                            }

                            if (compressionPower >= 0 && compressionPower <= 1000) {
                                compressionQuality = 1.0f - (compressionPower * 0.001f);
                                break;
                            }
                        }
                        break loop;
                }
            }
        }

        while (true) {
            printlnLocalized("select.thread-count");
            System.out.print(" ▶ ");
            String threadCountInput = scanner.nextLine();
            int threadCountParsed;
            try {
                threadCountParsed = Integer.parseInt(threadCountInput);
            } catch (NumberFormatException e) {
                continue;
            }

            if (threadCountParsed >= 1 && threadCountParsed <= 1000) {
                break;
            }
        }

        rootDirectory = Paths.get(pathString);
        try {
            contents = new ArrayDeque<>(Util.getContents(rootDirectory));
        } catch (IOException e) {
            return;
        }

        originalFileCount = contents.size();
        try {
            originalSize = Util.sumSize(contents);
        } catch (IOException ignored) {
        }
    }

    private void printOptions() {
        System.out.println("\n\nPath\t: " + rootDirectory.toString());
        System.out.print("Compress Target : ");
        if (compressText) {
            System.out.print("Minecraft modelling(Cubik Pro) .json , .mcmeta ");
        } else if (compressImage) {
            System.out.print(".png, .jpg, .jpeg");
        }
        System.out.println();
        if (compressImage) {
            System.out.println("Compression Quality : " + compressionQuality);
        }
        System.out.println("[START]");
    }

    private void printResult() throws IOException {
        long afterSize = Util.sumSize(Util.getContents(rootDirectory));
        System.out.println("[END]");
        System.out.println("Edited Files\t: " + contents.size());

        System.out.println(originalSize + " [Before]");
        System.out.println(FileUtils.byteCountToDisplaySize(afterSize) + " [After]");
        System.out.println(FileUtils.byteCountToDisplaySize(originalSize - afterSize) + " [Saved]");
    }

    private void printlnLocalized(String key) {
        System.out.println(
                new String(langBundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
    }

}
