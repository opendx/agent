package com.fgnb.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 * Created by jiangyitao.
 */
public class ImgUtil {


    public static String encryptToBase64String(String imgPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(imgPath));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void decryptByBase64String(String base64String,String filePath) throws IOException {
       Files.write(Paths.get(filePath), Base64.getDecoder().decode(base64String), StandardOpenOption.CREATE);
    }
}
