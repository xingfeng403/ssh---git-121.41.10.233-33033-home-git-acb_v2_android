package com.youtu.acb.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Created by xingf on 16/6/1.
 */
public class CreateCode {
    private static final char[] CHARS = {'1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'l',
            'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};  //预定随机数库
    private static final int CodeLength = 4; // 随机数个数
    private static final int LineNumber = 8;     //线条数目
    private static final int WIDTH = 140, HEIGHT = 80; // 位图长、宽
    private static final int FontSize = 40;     //随机数字体大小
    private static int base_padding_left;
    private static final int random_padding_left = 23,
            base_padding_top = 45, random_padding_top = 10;
    private static Random random = new Random();
    public static String code = "default";

    /*********************************************************************************
     * 方  法 名：createRandomBitmap
     * 功能描述：生成随机验证码视图
     *********************************************************************************/
    public static Bitmap createRandomBitmap() {
        /**
         * (1)生成一组随机数
         * */
        code = createRandomText();  //生成4个随机数
        /***
         * (2)创建位图Bitmap,画布Canvas,初始化画笔Paint
         * */
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888); //创建位图，并指定其长、宽
        Canvas canvas = new Canvas(bitmap);    //创建指定位图的画布
        canvas.drawColor(Color.WHITE);     //设置画布的背景为白色
        Paint paint = new Paint();     //定义画笔paint
        paint.setTextSize(FontSize);   //设置画笔字体大小
        /**
         * (3)生成四个随机数风格各异(颜色、位置、形状)的位图
         * */
        base_padding_left = 20;
        for (int i = 0; i < code.length(); i++) {
            //设置一个随机数的风格
            int color = RandomColor();
            paint.setColor(color);    //设置(画笔)该随机数的颜色
            paint.setFakeBoldText(false);//设置画笔为非粗体
            float skewX = random.nextInt(11) / 10;
            skewX = random.nextBoolean() ? skewX : (-skewX);
            paint.setTextSkewX(skewX);   //设置字体倾斜方向(负数表示右斜,整数表示左斜)
            //设置一个随机数位置
//   int padding_left = base_padding_left + random.nextInt(random_padding_left);
            int padding_top = base_padding_top + random.nextInt(random_padding_top);
            //绘制该随机数
            canvas.drawText(code.charAt(i) + "", base_padding_left, padding_top, paint);
            base_padding_left += random_padding_left;
        }
        /**
         * (4)绘制线条
         **/
        for (int i = 0; i < LineNumber; i++) {
            mdrawLine(canvas, paint);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);    //保存
        canvas.restore();
        return bitmap;
    }

    /*********************************************************************************
     * 方  法 名：createRandomText
     * 功能描述：
     *********************************************************************************/
    private static String createRandomText() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < CodeLength; i++) {
            buffer.append(CHARS[random.nextInt(CHARS.length)]);  //CHARS下标限定在0~CodeLength之间
        }
        return buffer.toString();  //生成4个随机数
    }

    /********************************************************************************
     * 方  法 名：RandomColor
     * 功能描述：生成一个随机颜色
     *********************************************************************************/
    private static int RandomColor() {
        int red = random.nextInt(256);     //红色：0~256之间
        int green = random.nextInt(256);   //绿色：0~256之间
        int blue = random.nextInt(256);    //蓝色：0~256之间
        return Color.rgb(red, green, blue);   //返回生成随机颜色值
    }

    /*********************************************************************************
     * 方  法 名：mdrawLine
     * 功能描述：绘制一条线条,参数：当前画布，当前画笔
     **********************************************************************************/
    private static void mdrawLine(Canvas canvas, Paint paint) {
        //a.设置该线条颜色
        int color = RandomColor();
        paint.setColor(color);
        //b.设置该随机数的位置(起点和终点,0~WIDTH,0~HEIGHT)
        int startX = random.nextInt(WIDTH);
        int startY = random.nextInt(HEIGHT);
        int stopX = random.nextInt(WIDTH);
        int stopY = random.nextInt(HEIGHT);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }
}
