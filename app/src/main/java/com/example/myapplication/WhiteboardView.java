package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class WhiteboardView extends View {
    private Paint paint;
    private List<Path> paths;
    private List<Paint> paints;
    private Path currentPath;
    private int currentColor;
    private float currentStrokeWidth = 5f;

    public WhiteboardView(Context context) {
        super(context);
        init();
    }

    public WhiteboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paths = new ArrayList<>();
        paints = new ArrayList<>();
        currentColor = Color.BLACK;
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(currentStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(currentColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        if (currentPath != null) {
            canvas.drawPath(currentPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                currentPath.lineTo(x, y);
                paths.add(currentPath);
                Paint newPaint = new Paint(paint);
                paints.add(newPaint);
                currentPath = null;
                break;
        }
        invalidate();
        return true;
    }

    public synchronized void clear() {
        if (paths != null && paints != null) {
            paths.clear();
            paints.clear();
            currentPath = null;
            invalidate();
        }
    }

    public void undo() {
        if (!paths.isEmpty()) {
            paths.remove(paths.size() - 1);
            paints.remove(paints.size() - 1);
            invalidate();
        }
    }

    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setStrokeWidth(float width) {
        currentStrokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public float getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }

    public void saveImage() {
        // 创建一个与View大小相同的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // 绘制当前View的内容到Bitmap
        draw(canvas);

        // 获取保存图片的文件名
        String fileName = "Whiteboard_" + System.currentTimeMillis() + ".png";

        // 获取Pictures目录
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File file = new File(path, fileName);

        try {
            // 将Bitmap保存为PNG文件
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // 通知媒体库更新
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{file.toString()},
                    new String[]{"image/png"}, null);

            // 显示成功提示
            Toast.makeText(getContext(), "图片已保存到相册", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}