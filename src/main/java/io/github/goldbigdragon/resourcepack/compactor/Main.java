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

import io.github.goldbigdragon.resourcepack.compactor.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public boolean running = false;

    public Path path;
    public boolean searchInnerDir;
    public float compressionQuality;

    public int fileCount;
    private long originalSize;

    public boolean compressText;
    public boolean compressImage;

    private static void println(ResourceBundle bundle, String key) {
        System.out.println(
                new String(bundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        ResourceBundle bundle = null;
        while (bundle == null) {
            System.out.println(
                    "[Select the language]: en-US[English], ko-KR[한국어], ja-JP[日本語], zh-TW[中国的], ru-RU[русский]");
            String code = scanner.nextLine();
            try {
                bundle = ResourceBundle.getBundle("Lang", Locale.forLanguageTag(code));
            } catch (MissingResourceException e) {
                System.out.println("Locale does not supported: " + code);
            }
        }

        println(bundle, "type.resource-pack-path");
        System.out.print(" ▶ ");
        String pathString = scanner.nextLine();

        loop:while (true) {
            println(bundle, "select.compress-mode");
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
                println(bundle, "select.compress-power");
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
                            println(bundle, "select.compress-power.type");
                            System.out.print(" ▶ ");
                            String toParse = scanner.nextLine();
                            try {
                                int parsedInt = Integer.parseInt(toParse);
                                if (parsedInt >= 0 && parsedInt <= 1000) {
                                    compressionQuality = 1.0f - (parsedInt * 0.001f);
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // ignore error
                            }

                        }
                        break loop;
                }
            }
        }

        int threadCount;
        while (true) {
            println(bundle, "select.thread-count");
            System.out.print(" ▶ ");
            String threadCountInput = scanner.nextLine();
            int parsedInt;
            try {
                parsedInt = Integer.parseInt(threadCountInput);
            } catch (NumberFormatException e) {
                continue;
            }

            if (parsedInt >= 1 && parsedInt <= 1000) {
                threadCount = parsedInt;
                break;
            }
        }

        System.out.println("\n\nPath\t: " + pathString);
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

        path = Paths.get(pathString);
        List<Path> paths;
        try {
            paths = Util.getContents(path);
        } catch (IOException e) {
            return;
        }
        fileCount = paths.size();

        try {
            originalSize = Util.sumSize(paths);
        } catch (IOException e) {
            return;
        }

        running = true;
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Compressor(this, i).start();
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


    public void printResult() throws IOException {
        long afterSize = Util.sumSize(Util.getContents(path));
        System.out.println("[END]");
        System.out.println("Edited Files\t: " + fileCount);

        System.out.println(originalSize + " [Before]");
        System.out.println(FileUtils.byteCountToDisplaySize(afterSize) + " [After]");
        System.out.println(FileUtils.byteCountToDisplaySize(originalSize - afterSize) + " [Saved]");
    }

}
