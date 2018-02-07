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

import de.xn__ho_hia.storage_unit.StorageUnit;
import de.xn__ho_hia.storage_unit.StorageUnits;
import org.jooq.lambda.Unchecked;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static boolean started = false;

    public static String path = "./";
    public static boolean searchInnerDir = true;
    public static float compressPower = 1f;

    public static ArrayList<Compressor> threads = new ArrayList<>();

    public static int totalSize = 0;
    private static StorageUnit<?> originalSize = StorageUnits.kilobyte(0);


    public static boolean compressText = false;
    public static boolean compressImage = false;

    private static void println(ResourceBundle bundle, String key) {
        System.out.println(
                new String(bundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        ResourceBundle bundle = null;
        String temp;
        Scanner scanner = new Scanner(System.in);
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
        path = scanner.nextLine();

        int threadAmount;

        loop:
        while (true) {
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
            loop:
            while (true) {
                println(bundle, "select.compress-power");
                System.out.print(" ▶ ");

                switch (scanner.nextLine()) {
                    case "1":
                        compressPower = 0.9F;
                        break loop;
                    case "2":
                        compressPower = 0.7F;
                        break loop;
                    case "3":
                        compressPower = 0.5F;
                        break loop;
                    case "4":
                        compressPower = 0.3F;
                        break loop;
                    case "5":
                        while (true) {
                            println(bundle, "select.compress-power.type");
                            System.out.print(" ▶ ");
                            String toParse = scanner.nextLine();
                            try {
                                int parsedInt = Integer.parseInt(toParse);
                                if (parsedInt >= 0 && parsedInt <= 1000) {
                                    compressPower = 1.0f - (parsedInt * 0.001f);
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
        while (true) {
            println(bundle, "select.thread-count");
            System.out.print(" ▶ ");
            temp = scanner.nextLine();
            try {
                int parsedInt = Integer.parseInt(temp);
                if (parsedInt >= 1 && parsedInt <= 1000) {
                    threadAmount = parsedInt;
                    break;
                }
            } catch (NumberFormatException e) {
                // ignore error
            }
        }

        System.out.println("\n\nPath\t: " + path);
        System.out.print("Compress Target : ");
        if (compressText) {
            System.out.print("Minecraft modelling(Cubik Pro) .json , .mcmeta ");
        } else if (compressImage) {
            System.out.print(".png, .jpg, .jpeg");
        }
        System.out.println();
        if (compressImage) {
            System.out.println("Compress Power : " + compressPower);
        }
        System.out.println("[START]");

        List<Path> paths = getPaths(Paths.get(path));
        totalSize = paths.size();

        originalSize = summarizeSize(paths);

        started = true;
        threads.clear();
        for (int count = 0; count < threadAmount; count++) {
            Compressor thread = new Compressor(count);
            threads.add(thread);
            thread.start();
        }
    }

    public static List<Path> getPaths(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StorageUnit<?> summarizeSize(Collection<Path> paths) {
        return paths.stream()
                    .map(Unchecked.function(Files::size))
                    .map(StorageUnits::decimalValueOf)
                    .reduce(StorageUnit::add)
                    .orElseThrow(() -> new IllegalArgumentException("Stream must not be a null"));
    }

    public static void sendResult() {
        StorageUnit<?> afterSize = summarizeSize(getPaths(Paths.get(path)));
        System.out.println("[END]");
        System.out.println("Edited Files\t: " + totalSize);


        System.out.println(originalSize + " [Before]");
        System.out.println(sb.toString());

        sb = new StringBuilder();
        if (afterGb > 0) {
            sb.append(afterGb).append("GB ");
        }
        if (afterMb > 0) {
            sb.append(afterMb).append("MB ");
        }
        if (afterKb > 0) {
            sb.append(afterKb).append("KB ");
        }
        sb.append(afterSize.toString());
        sb.append("Byte [After]");
        System.out.println(sb.toString());

        sb = new StringBuilder();
        if (originalGb - afterGb > 0) {
            sb.append(originalGb - afterGb).append("GB ");
        }
        if (originalMb - afterMb > 0) {
            sb.append(originalMb - afterMb).append("MB ");
        }
        if (originalKb - afterKb > 0) {
            sb.append(originalKb - afterKb).append("KB ");
        }
        sb.append(originalSize.subtract(afterSize).toString());
        sb.append("Byte [Saved]");
        System.out.println(sb.toString());
    }

}
