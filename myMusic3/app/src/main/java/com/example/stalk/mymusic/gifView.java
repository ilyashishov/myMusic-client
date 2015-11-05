package com.example.stalk.mymusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;

public class gifView  extends View{

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWitdh, movieHeight;
    private long movieDuration;
    private long movieStatr;

    public gifView(Context context){
        super(context);
        init(context);
    }

    public gifView(Context context, AttributeSet attrs){
        super(context);
        init(context);
    }

    public gifView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        setFocusable(true);

        gifInputStream = context.getResources().openRawResource(R.drawable.load);

        gifMovie = Movie.decodeStream(gifInputStream);
        movieWitdh = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(movieWitdh, movieHeight);
    }

    public  int getMovieWitdh(){
        return movieWitdh;
    }

    public  int getMovieHeight(){
        return movieHeight;
    }
    public  long getMovieDuration(){
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas){
        long now = SystemClock.uptimeMillis();

        if(movieStatr == 0){
            movieStatr = now;
        }

        if(gifMovie != null){
            int dur = gifMovie.duration();
            if(dur == 0){
                dur = 1000;
            }
            int relTime = (int)((now - movieStatr) % dur);

            gifMovie.setTime(relTime);
            gifMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }

}
