package com.vp.statusdownloader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import java.io.File;
import android.os.Environment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Color;

public class MainActivity extends Activity {

    private static final int REQ_CODE = 101;
    private final String STATUS_PATH = "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/";

    private GridLayout layoutImages;
    private GridLayout layoutVideos;
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the tab layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout tabLayout = new LinearLayout(this);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        final TextView tabImages = new TextView(this);
        tabImages.setText("Images");
        tabImages.setTextSize(18);
        tabImages.setGravity(Gravity.CENTER);
        tabImages.setPadding(40, 20, 40, 20);

        final TextView tabVideos = new TextView(this);
        tabVideos.setText("Videos");
        tabVideos.setTextSize(18);
        tabVideos.setGravity(Gravity.CENTER);
        tabVideos.setPadding(40, 20, 40, 20);
        
        LinearLayout imageTabWrapper = new LinearLayout(this);
        imageTabWrapper.setOrientation(LinearLayout.VERTICAL);
        imageTabWrapper.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        imageTabWrapper.setGravity(Gravity.CENTER);
        imageTabWrapper.addView(tabImages);

        final View underlineImage = new View(this);
        underlineImage.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        imageTabWrapper.addView(underlineImage);

        LinearLayout videoTabWrapper = new LinearLayout(this);
        videoTabWrapper.setOrientation(LinearLayout.VERTICAL);
        videoTabWrapper.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        videoTabWrapper.setGravity(Gravity.CENTER);
        videoTabWrapper.addView(tabVideos);

        final View underlineVideo = new View(this);
        underlineVideo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        videoTabWrapper.addView(underlineVideo);
        
        tabLayout.addView(imageTabWrapper);
        tabLayout.addView(videoTabWrapper);

        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(contentLayout);

        root.addView(tabLayout);
        root.addView(scroll);
        setContentView(root);

        layoutImages = new GridLayout(this);
        layoutImages.setColumnCount(3);
        layoutImages.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        layoutImages.setUseDefaultMargins(true);
        layoutImages.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        layoutVideos = new GridLayout(this);
        layoutVideos.setColumnCount(3);
        layoutVideos.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        layoutVideos.setUseDefaultMargins(true);
        layoutVideos.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        underlineImage.setBackgroundColor(Color.GREEN);
        underlineVideo.setBackgroundColor(Color.TRANSPARENT);
        // Tab click logic
        tabImages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTab("images");
                    underlineImage.setBackgroundColor(Color.GREEN);
                    underlineVideo.setBackgroundColor(Color.TRANSPARENT);
                }
            });
            
        tabVideos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTab("videos");
                    underlineImage.setBackgroundColor(Color.TRANSPARENT);
                    underlineVideo.setBackgroundColor(Color.GREEN);
                }
            });
            
        // Permissions
        if (Build.VERSION.SDK_INT >= 30) {
            if (!hasAllFilesAccessPermission()) {
                Toast.makeText(this, "Grant All Files Access", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Settings access failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                loadStatuses();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_CODE);
            } else {
                loadStatuses();
            }
        }
    }

    private void showTab(String tab) {
        contentLayout.removeAllViews();
        if (tab.equals("images")) {
            contentLayout.addView(layoutImages);
        } else {
            contentLayout.addView(layoutVideos);
        }
    }
    
    private void updateTabStyle(TextView selected, TextView other) {
        selected.setBackgroundColor(0xFF4CAF50); // Green underline (bottom only)
        selected.setPadding(40, 20, 40, 20);
        selected.setTextColor(Color.BLACK);

        other.setBackgroundColor(Color.TRANSPARENT);
        other.setPadding(40, 20, 40, 20);
        other.setTextColor(Color.GRAY);
    }

    private void loadStatuses() {
        File dir = new File(STATUS_PATH);
        if (!dir.exists()) {
            Toast.makeText(this, "Status folder not found", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            Toast.makeText(this, "No files found", Toast.LENGTH_SHORT).show();
            return;
        }

        int imgCount = 0;
        int vidCount = 0;

        for (final File file : files) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bmp != null) {
                    final String path = file.getAbsolutePath();
                    ImageView img = new ImageView(this);
                    Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 250, 250, false);
                    img.setImageBitmap(getCircularBitmap(scaledBmp, 6, Color.GREEN));
                    int size = getItemSize();
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = size;
                    params.height = size;
                    params.setMargins(10, 10, 10, 10);
                    img.setLayoutParams(params);
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    img.setPadding(10, 10, 10, 10);
                    //img.setBackgroundResource(android.R.drawable.picture_frame);
                    img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showFullImage(path);
                            }
                        });
                    layoutImages.addView(img);
                    imgCount++;
                }
            } else if (file.getName().endsWith(".mp4")) {
                final String videoPath = file.getAbsolutePath();
                ImageView thumb = new ImageView(this);
                // ✅ Generate thumbnail from video
                Bitmap bmp = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);

                // Fallback if thumbnail fails
                if (bmp == null) {
                    bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
                }
                Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 250, 250, false);
                thumb.setImageBitmap(getCircularBitmap(scaledBmp, 6, Color.GREEN));
                int size = getItemSize();
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size;
                params.height = size;
                params.setMargins(10, 10, 10, 10);
                thumb.setLayoutParams(params);
                thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                thumb.setPadding(10, 10, 10, 10);
                
                // Play icon
                ImageView playIcon = new ImageView(this);
                playIcon.setImageResource(R.drawable.play); // Make sure play.png is in res/drawable/
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(80, 80); // or dynamic size
                iconParams.gravity = Gravity.CENTER;
                playIcon.setLayoutParams(iconParams);
                
                // FrameLayout to hold both
                FrameLayout frame = new FrameLayout(this);
                FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(size, size);
                frameParams.setMargins(10, 10, 10, 10);
                frame.setLayoutParams(frameParams);
                
                // Add views
                frame.addView(thumb);
                frame.addView(playIcon);
                
                //thumb.setBackgroundResource(android.R.drawable.picture_frame);
                frame.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFullVideo(videoPath);
                        }
                    });
                layoutVideos.addView(frame);
                vidCount++;
            }
        }

        Toast.makeText(this, "Loaded: " + imgCount + " images & " + vidCount + " videos", Toast.LENGTH_SHORT).show();
        showTab("images");
    }

    private void showFullImage(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Image View
        ImageView img = new ImageView(this);
        Bitmap bmp = BitmapFactory.decodeFile(path);
        img.setImageBitmap(bmp);
        img.setAdjustViewBounds(true);

        // Layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.addView(img);

        // Download button
        final Button downloadButton = new Button(this);
        downloadButton.setText("Download");
        layout.addView(downloadButton);

        // Set layout in builder
        builder.setView(layout);
        builder.setPositiveButton("Close", null);

        // Create and show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set button click listener
        downloadButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        File srcFile = new File(path);
                        File destDir = new File(Environment.getExternalStorageDirectory(), "DCIM/StatusDownloader-VP");
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                        }

                        File destFile = new File(destDir, srcFile.getName());
                        copyFile(srcFile, destFile);

                        Toast.makeText(MainActivity.this, "Downloaded to: " + destFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    
    private void showFullVideo(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // VideoView
        final VideoView videoView = new VideoView(this);
        videoView.setVideoURI(Uri.parse(path));
        videoView.setLayoutParams(new LinearLayout.LayoutParams(
                                      ViewGroup.LayoutParams.MATCH_PARENT,
                                      ViewGroup.LayoutParams.WRAP_CONTENT
                                  ));
        videoView.setMediaController(new android.widget.MediaController(this));
        videoView.requestFocus();
        videoView.start();

        // Layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.addView(videoView);

        // Download button
        final Button downloadButton = new Button(this);
        downloadButton.setText("Download");
        layout.addView(downloadButton);

        builder.setView(layout);
        builder.setPositiveButton("Close", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        downloadButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        File srcFile = new File(path);
                        File destDir = new File(Environment.getExternalStorageDirectory(), "DCIM/StatusDownloader-VP");
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                        }

                        File destFile = new File(destDir, srcFile.getName());
                        copyFile(srcFile, destFile);

                        Toast.makeText(MainActivity.this, "Downloaded to: " + destFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    
    private int getItemSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int spacing = 20; // total spacing including padding/margin
        return (screenWidth / 3) - spacing;
    }
    
    private Bitmap getCircularBitmap(Bitmap bitmap, int borderWidth, int borderColor) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);

        // Draw circular image
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // Draw circular green border inside the canvas
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);

        // 👉 Radius is half of size minus half of strokeWidth
        float radius = (size / 2f) - (borderWidth / 2f);
        canvas.drawCircle(size / 2f, size / 2f, radius, paint);

        return output;
    }
    
    private void copyFile(File source, File dest) throws Exception {
        java.io.InputStream in = new java.io.FileInputStream(source);
        java.io.OutputStream out = new java.io.FileOutputStream(dest);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        
        // 👉 Trigger system scan
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(dest));
        sendBroadcast(scanIntent);

        in.close();
        out.close();
    }

    private boolean hasAllFilesAccessPermission() {
        try {
            Class env = Class.forName("android.os.Environment");
            return ((Boolean) env.getMethod("isExternalStorageManager").invoke(null)).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] grants) {
        if (reqCode == REQ_CODE && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) {
            loadStatuses();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}

