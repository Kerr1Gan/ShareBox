package com.newindia.sharebox.qrimage;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * Created by KerriGan on 2016/4/24.
 */
public class QrUtils {

    private static int _width=200,_height=200;


    public static Bitmap createQRImage(String url)
    {
        return createQRImage(url,_width,_height);
    }

    public static Bitmap createQRImage(String url,int width,int height)
    {
        Bitmap b=null;
        try
        {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1)
            {
                return null;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//设置容错级别,H为最高
//            hints.put(EncodeHintType.MAX_SIZE, LOGO_WIDTH_MAX);// 设置图片的最大值
//            hints.put(EncodeHintType.MIN_SIZE, LOGO_WIDTH_MIN);// 设置图片的最小值
            hints.put(EncodeHintType.MARGIN, 1);//设置白色边距值
//            // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
//            BitMatrix matrix = new MultiFormatWriter().encode(content,
//                    BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_WIDTH, hints);


            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * width + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            b=bitmap;
        }
        catch (WriterException e)
        {
            e.printStackTrace();
            b=null;
        }

        return b;
    }
}
