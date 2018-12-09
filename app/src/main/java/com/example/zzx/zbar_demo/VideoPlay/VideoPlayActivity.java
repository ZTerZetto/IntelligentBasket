package com.example.zzx.zbar_demo.VideoPlay;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.zzx.zbar_demo.R;

public class VideoPlayActivity extends AppCompatActivity {

 /*   private VideoView video;*/
    public static String path = "rtmp://10.193.0.20:8090/hls/mystream";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        /*if (!LibsChecker.checkVitamioLibs(this))
            return;
        video = (VideoView) findViewById(R.id.video1);
        //Uri uri = Uri.parse(path);
        //video.setVideoURI(uri);
        video.setVideoPath(path);
        video.setMediaController(new MediaController(VideoPlayActivity.this));
        video.requestFocus();

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
            }
        });*/

    }

}
