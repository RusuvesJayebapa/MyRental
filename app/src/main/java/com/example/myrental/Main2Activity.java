package com.example.myrental;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {

    TextView textView;
    DatePickerDialog datePickerDialog;
    Button button;
    String day, mon;
    ImageView imageView;
    VideoView videoView;
    MediaController mediaController;
    LinearLayout linearLayout;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        textView = findViewById(R.id.date);
//        button = findViewById(R.id.button);
//
//        Toast.makeText(Main2Activity.this, "hshushu"+ Period.between(LocalDate.parse("1998-09-01"), LocalDate.now()), Toast.LENGTH_LONG).show();
//
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar c = Calendar.getInstance();
//
//
//                datePickerDialog = new DatePickerDialog(Main2Activity.this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                        if (dayOfMonth<10)
//                            day = "0"+dayOfMonth;
//                        if (month<10)
//                             mon = "0"+month;
//                        textView.setText(day+"/"+mon+"/"+year);
//                    }
//                }, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
//                datePickerDialog.show();
//            }
//        });
////        String d = "01/09/1998";
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(Main2Activity.this, "hshushu"+ Period.between(LocalDate.parse(textView.getText().toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()), Toast.LENGTH_LONG).show();
//            }
//        });
//        String videopath = "F:\\RAW";
//        imageView = findViewById(R.id.image);
//        imageView.setImageBitmap(createVideoThumbNail(videopath));
        videoView = findViewById(R.id.video);
        linearLayout = findViewById(R.id.line1);
        mediaController = new MediaController(Main2Activity.this);
        setMediaController();
        playVideoRawFolder();
     //   playVideoFromURL();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("video/mp4 image/*"), "S"),1);
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
//                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                    @Override
//                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//
//                    }
//                });
            }
        });

    }
    private void playVideoFromURL()
    {
        String urlVideo = "";
        videoView.setVideoURI(Uri.parse(urlVideo));
        videoView.start();
    }

    private void playVideoRawFolder()
    {
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video);
        videoView.setVideoURI(uri);
        videoView.start();
    }

    private void setMediaController()
    {
        mediaController.setMediaPlayer(videoView);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }
//    public Bitmap createVideoThumbNail(String path)
//    {
//        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            videoView.setVideoURI(uri);
            videoView.start();

            ContentResolver contentResolver = this.getContentResolver();
            Toast.makeText(Main2Activity.this, "lko "+contentResolver.getType(uri), Toast.LENGTH_LONG).show();
        }
    }
}
