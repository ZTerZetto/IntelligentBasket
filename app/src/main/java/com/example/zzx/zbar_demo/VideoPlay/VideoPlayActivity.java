package com.example.zzx.zbar_demo.VideoPlay;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.zzx.zbar_demo.R;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoPlayActivity extends AppCompatActivity {

    private VideoView video;
    private Button btn;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        initView();
    }

    private void initView() {
        video = (VideoView) findViewById(R.id.video1);
        btn = (Button) findViewById(R.id.btn1);
        et = (EditText) findViewById(R.id.et2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String path = Environment.getExternalStorageDirectory().getPath()+"/"+et.getText().toString();
                String path = et.getText().toString();
                //String path = "http://www.modrails.com/videos/passenger_nginx.mov";
                Uri uri = Uri.parse(path);
                video.setVideoURI(uri);
                video.setMediaController(new MediaController(VideoPlayActivity.this));

                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        video.start();
                    }
                });
            }
        });
    }
}
