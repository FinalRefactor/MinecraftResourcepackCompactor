package io.github.goldbigdragon.resourcepack.compactor;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * Copyright 2018 GoldBigDragon (https://github.com/GoldBigDragon)
 *
 * GoldBigDragonRPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License.

 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Main {
    public static boolean started = false;

    public static String path = "./";
    public static boolean searchInnerDir = true;
    public static float compressPower = 1f;

    public static List<String> jsonFilePath = new ArrayList<>();
    public static List<String> imageFilePath = new ArrayList<>();

    public static ArrayList<Compressor> threads = new ArrayList<>();

    public static int totalSize = 0;
    private static BigInteger originalSize = new BigInteger("0");


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
        jsonFilePath.clear();
        imageFilePath.clear();

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

        getFilePath(new File(path));
        totalSize = jsonFilePath.size() + imageFilePath.size();

        originalSize = folderSize(new File(path));

        started = true;
        threads.clear();
        for (int count = 0; count < threadAmount; count++) {
            Compressor thread = new Compressor(count);
            threads.add(thread);
            thread.start();
        }
    }

    public static void getFilePath(File dir) {
        File[] fList = dir.listFiles();
        for (File f : fList) {
            if (searchInnerDir && f.isDirectory()) {
                getFilePath(new File(f.getAbsolutePath()));
            } else if (f.isFile()) {
                if (compressText && f.getName().endsWith(".json") || f.getName().endsWith(".mcmeta")) {
                    jsonFilePath.add(f.getAbsolutePath());
                } else if (compressImage && f.getName().endsWith(".png") ||
                           f.getName().endsWith(".jpg") ||
                           f.getName().endsWith(".jpeg")) { imageFilePath.add(f.getAbsolutePath()); }
            }
        }
    }

    public static BigInteger folderSize(File directory) {
        BigInteger bi = new BigInteger("0");
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                bi = bi.add(BigInteger.valueOf(file.length()));
            } else {
                bi = bi.add(folderSize(file));
            }
        }
        return bi;
    }

    public static void sendResult() {
        BigInteger afterSize = folderSize(new File(path));
        System.out.println("[END]");
        System.out.println("Edited Files\t: " + totalSize);

        int originalGb = 0;
        int originalMb = 0;
        int originalKb = 0;

        int afterGb = 0;
        int afterMb = 0;
        int afterKb = 0;
        //GB
        if (originalSize.compareTo(BigInteger.valueOf(1073741824)) > -1) {
            originalGb = originalSize.divide(BigInteger.valueOf(1073741824)).intValue();
            originalSize = originalSize.subtract(BigInteger.valueOf(originalGb * 1073741824));
        }
        //MB
        if (originalSize.compareTo(BigInteger.valueOf(1048576)) > -1) {
            originalMb = originalSize.divide(BigInteger.valueOf(1048576)).intValue();
            originalSize = originalSize.subtract(BigInteger.valueOf(originalMb * 1048576));
        }
        //KB
        if (originalSize.compareTo(BigInteger.valueOf(1024)) > -1) {
            originalKb = originalSize.divide(BigInteger.valueOf(1024)).intValue();
            originalSize = originalSize.subtract(BigInteger.valueOf(originalKb * 1024));
        }

        //GB
        if (afterSize.compareTo(BigInteger.valueOf(1073741824)) > -1) {
            afterGb = afterSize.divide(BigInteger.valueOf(1073741824)).intValue();
            afterSize = afterSize.subtract(BigInteger.valueOf(afterGb * 1073741824));
        }
        //MB
        if (afterSize.compareTo(BigInteger.valueOf(1048576)) > -1) {
            afterMb = afterSize.divide(BigInteger.valueOf(1048576)).intValue();
            afterSize = afterSize.subtract(BigInteger.valueOf(afterMb * 1048576));
        }
        //KB
        if (afterSize.compareTo(BigInteger.valueOf(1024)) > -1) {
            afterKb = afterSize.divide(BigInteger.valueOf(1024)).intValue();
            afterSize = afterSize.subtract(BigInteger.valueOf(afterKb * 1024));
        }

        StringBuilder sb = new StringBuilder();
        if (originalGb > 0) {
            sb.append(originalGb).append("GB ");
        }
        if (originalMb > 0) {
            sb.append(originalMb).append("MB ");
        }
        if (originalKb > 0) {
            sb.append(originalKb).append("KB ");
        }
        sb.append(originalSize.toString());
        sb.append("Byte [Before]");
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
