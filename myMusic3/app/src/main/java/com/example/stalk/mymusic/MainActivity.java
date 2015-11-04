package com.example.stalk.mymusic;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {
    Button button;
    TextView textView;

    final String TAG = "Func";

    int myBufferSize ;
    AudioRecord audioRecord;
    boolean isReading = false;

    private short   nChannels;
    private int     sRate;
    private short   mBitsPersample;
    private int     mBufferSize;
    private int     mAudioSource;
    private int     aFormat;
    private int     mPeriodInFrames;
    private byte[]  myBuffer;
    private static final int TIMER_INTERVAL = 120;

    private RandomAccessFile randomAccessWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);


//        button.setOnClickListener(new View.OnClickListener() {
//            int i = 1;
//            int a = 1;
//            @Override
//            public void onClick(View v) {
//                if (i == 1) {
//                    button.setBackgroundResource(R.drawable.preloader);
//                    i = 2;
//                } else {
//                    button.setBackgroundResource(R.drawable.button1);
//                    i = 1;
//                }
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            public void run() {
//                                textView.setText(Integer.toString(a++));
//                            }
//                        });
//                    }
//                }, 0, 1000);
//            }
//        });

        mBitsPersample = 16;
        nChannels = 1;
        sRate = 44100;
        mPeriodInFrames = sRate * TIMER_INTERVAL / 1000;
        //myBufferSize = mPeriodInFrames * mBitsPersample / 8 * nChannels;
        //myBufferSize = 65400;
        createAudioRecorder();

        //String mRcordFilePath = Environment.getExternalStorageDirectory() + "/testwav.wav";

        //prepare(mRcordFilePath);

        Log.d(TAG, "init state = " + audioRecord.getState());
    }

    void createAudioRecorder() {
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int minInternalBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfig, audioFormat);
        int internalBufferSize = minInternalBufferSize * 4;
        Log.d(TAG, "minInternalBufferSize = " + minInternalBufferSize
                + ", internalBufferSize = " + internalBufferSize
                + ", myBufferSize = " + myBufferSize);
        myBufferSize = internalBufferSize * 2;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, internalBufferSize);
    }

    public void prepare( String filePath) {
        try {
            // nChannels = 1;
            //sRate = 44100;
            //mBitsPersample = 16;
            // write file header
            randomAccessWriter = new RandomAccessFile(filePath, "rw");
            //randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
            randomAccessWriter.setLength(0);
            randomAccessWriter.writeBytes("RIFF");
            randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
            randomAccessWriter.writeBytes("WAVE");
            randomAccessWriter.writeBytes("fmt ");
            randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
            randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
            randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
            randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
            randomAccessWriter.writeInt(Integer.reverseBytes(sRate * nChannels * mBitsPersample / 8)); // Byte rate, SampleRate*NumberOfChannels*mBitsPersample/8
            randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * mBitsPersample / 8))); // Block align, NumberOfChannels*mBitsPersample/8
            randomAccessWriter.writeShort(Short.reverseBytes(mBitsPersample)); // Bits per sample
            randomAccessWriter.writeBytes("data");
            //randomAccessWriter.seek(4); // Write size to RIFF header
            // randomAccessWriter.writeInt(Integer.reverseBytes(8236));
            //randomAccessWriter.seek(40); // Write size to Subchunk2Size field
            // randomAccessWriter.writeInt(Integer.reverseBytes(8192));
            //randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0

            myBuffer = new byte[mPeriodInFrames * mBitsPersample / 8 * nChannels];

        }

        catch(Exception e) {
            if (e.getMessage() != null) {
                Log.e(TAG, e.getMessage());
            } else {
                Log.e(TAG, "Unknown error occured in prepare()");
            }

        }
    }

    public void recordStart(View v) {
        Log.d(TAG, "record start");
        audioRecord.startRecording();     // начинается запись звука с микрофона
        int recordingState = audioRecord.getRecordingState();
        Log.d(TAG, "recordingState = " + recordingState);

    }

    public void recordStop(View v) {
        Log.d(TAG, "record stop");
        audioRecord.stop();  // конец записи
    }

    public void readStart(View v) {
        Log.d(TAG, "read start");
        isReading = true;
        new Thread( new Runnable() {
            @Override
            public void run() {
                if (audioRecord == null)
                    return;

                byte[] myBuffer = new byte[myBufferSize];
                int readCount = 0;
                int totalCount = 0;
                while (isReading) {
                    readCount = audioRecord.read(myBuffer, 0, myBuffer.length);  // здесь идет запись в wav файл
                    totalCount += readCount;
                    Log.d(TAG, "readCount = " + readCount);
                    Log.d(TAG, "totalCount = " + totalCount);
                    myBufferSize = totalCount;
                }

                if(isReading == false)
                {
                    String mRcordFilePath = Environment.getExternalStorageDirectory() + "/testwav.wav";

                    prepare(mRcordFilePath);

                    try {

                        Log.d(TAG, "myBuffer" + myBuffer);

                        randomAccessWriter.seek(4);
                        randomAccessWriter.writeInt(Integer.reverseBytes(36 + myBufferSize)); // дописывается в заголовок размер файла без первых 8 байт
                        randomAccessWriter.seek(40);
                        randomAccessWriter.writeInt(Integer.reverseBytes(myBufferSize)); // дописывается в заголовок размер области данных
                        randomAccessWriter.write(myBuffer);    // записываются сами данные из буфера в файл
                        randomAccessWriter.close();
                    } catch (IOException e) {

                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }


                }
            }
        }).start();
    }

    public void readStop(View v) {
        Log.d(TAG, "read stop");
        isReading = false;                // признак конца чтения байт в буфер приложения

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isReading = false;
        if (audioRecord != null) {
            audioRecord.release();
        }
    }
}
