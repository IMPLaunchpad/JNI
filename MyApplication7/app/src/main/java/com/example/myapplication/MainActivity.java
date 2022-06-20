package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int FULL_LED1 = 9;
    public static final int FULL_LED2 = 8;
    public static final int FULL_LED3 = 7;
    public static final int FULL_LED4 = 6;
    public static final int ALL_LED = 5;

    public static final Integer[][] fullLED = {
        { FULL_LED1, FULL_LED1, FULL_LED2, FULL_LED2},
        { FULL_LED1, FULL_LED1, FULL_LED2, FULL_LED2},
        { FULL_LED3, FULL_LED3, FULL_LED4, FULL_LED4},
        { FULL_LED3, FULL_LED3, FULL_LED4, FULL_LED4}
    };

    Button[][] buttonList = new Button[4][4];

    SoundPool[][] soundPoolList = new SoundPool[4][4];

    Integer[][] viewIdList = new Integer[4][4];

    Map<Integer, Integer> viewIdIndexMap = new HashMap<>();

    Integer[][][] audioIdList = {
            {
                    {R.raw.a1, R.raw.a2, R.raw.a3, R.raw.a4_1},
                    {R.raw.no, R.raw.no, R.raw.a4_2, R.raw.a4_3},
                    {R.raw.a5, R.raw.a6, R.raw.a7, R.raw.a8_1},
                    {R.raw.no, R.raw.no, R.raw.a8_2, R.raw.a8_3}
            }
    };

    Integer[][] soundIds = new Integer[4][4];

    Integer[][] colorsTable = {
            {0x80, 0x00, 0x00},
            {0x00, 0x80, 0x00},
            {0x00, 0x00, 0x80},
            {0x80, 0x80, 0x80}
    };

    Button selectDialog;

    AlertDialog.Builder builder;

    String[] songs;

    Map<Integer, Integer> keyMap = new HashMap<Integer, Integer>() {{
        put(111, 0);
        put(8, 1);
        put(9, 2);
        put(10, 3);
        put(11, 4);
        put(12, 5);
        put(13, 6);
        put(14, 7);
        put(15, 8);
        put(16, 9);
        put(7, 10);
        put(69, 11);
        put(70, 12);
        put(67, 13);
        put(61, 14);
        put(45, 15);
    }};

    int count = 0;

    // Used to load the 'myapplication' library on application startup.
    static {
        System.loadLibrary("myapplication");
    }

    private native int TextLCDOut(String str1, String str2);
    private native int FLEDControl(int led_num, int val1, int val2, int val3);
    private native int IOCtlClear();
    private native int LEDControl(int data);
    private native int PiezoControl(int value);

    private ActivityMainBinding binding;

    String str1, str2;

    boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        viewIdList[0][0] = R.id.button00;
        viewIdList[0][1] = R.id.button01;
        viewIdList[0][2] = R.id.button02;
        viewIdList[0][3] = R.id.button03;

        viewIdList[1][0] = R.id.button10;
        viewIdList[1][1] = R.id.button11;
        viewIdList[1][2] = R.id.button12;
        viewIdList[1][3] = R.id.button13;

        viewIdList[2][0] = R.id.button20;
        viewIdList[2][1] = R.id.button21;
        viewIdList[2][2] = R.id.button22;
        viewIdList[2][3] = R.id.button23;

        viewIdList[3][0] = R.id.button30;
        viewIdList[3][1] = R.id.button31;
        viewIdList[3][2] = R.id.button32;
        viewIdList[3][3] = R.id.button33;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                soundPoolList[i][j] = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

                soundPoolList[i][j].setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                        int i = count / 4;
                        int j = count % 4;

                        buttonList[i][j].setBackgroundColor(getResources().getColor(R.color.gray));

                        count++;
                    }
                });
            }
        }

        selectDialog = findViewById(R.id.selectSong);

        selectDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    public void showDialog() {
        songs = getResources().getStringArray(R.array.songs);

        final String[] selected = { "" };
        final int[] selectedIndex = {-1};
        final boolean[] isSelected = {false};

        builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Select Songs to Play")
                .setSingleChoiceItems(songs, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected[0] = songs[which];
                        selectedIndex[0] = which;

                        String[] info = selected[0].split(" ");

                        String artist = info[0];
                        String artistPrefix = "Artist : ";

                        String title = info[2];
                        String titlePrefix = "Title  : ";

                        str1 = artistPrefix + artist;
                        str2 = titlePrefix + title;

                        IOCtlClear();
                        TextLCDOut(str1, str2);
                    }
                });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Unselected", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView textView = findViewById(R.id.info);
                textView.setText(str1+" \n"+str2);

                Toast.makeText(MainActivity.this, "Selected : [" + selected[0] + "]", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                FLEDControl(ALL_LED, 0, 0, 0);
                FLEDControl(ALL_LED, 100, 100, 100);
                PiezoControl(1); // 부저 on

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        int buttonId = viewIdList[i][j];
                        int audioId = audioIdList[0][i][j];

                        buttonList[i][j] = findViewById(buttonId);

                        soundIds[i][j] = soundPoolList[i][j].load(MainActivity.this, audioId, 0);

                        viewIdIndexMap.put(viewIdList[i][j], i * 4 + j);

                    }
                }


                int offset = 18;
                int bpm = 105 - offset;
                int interval = bpm * 2;

                class BPMDisplay implements Runnable {

                    @Override
                    public void run() {
                        while(true){
                            LEDControl(0b1);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }

                            LEDControl(0b10);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b100);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b1000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b10000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b100000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b1000000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b10000000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b1000000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b100000);
                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b10000);
                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b1000);

                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b100);
                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b10);
                            try {
                                Thread.sleep(interval);
                            }catch (Exception e){
                            }
                            LEDControl(0b1);
                        }
                    }
                }

                BPMDisplay bpmDisplay = new BPMDisplay();
                Thread t = new Thread(bpmDisplay) ;
                t.start() ;

                try {
                    Thread.sleep(2500);
                }catch (Exception e){
                }

                FLEDControl(ALL_LED, 0, 0, 0);
                PiezoControl(0); // 부저 off
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode != 0){
            Integer key = keyMap.get(keyCode);

            if (key == null) return false;

            int i = key / 4;
            int j = key % 4;

            int k = i % 2;
            int l = j % 2;

            int t = k * 2 + l;

            int r = colorsTable[t][0];
            int g = colorsTable[t][1];
            int b = colorsTable[t][2];

            int screenR = colorsTable[t][0] > 0 ? 0xFF : colorsTable[t][0];
            int screenG = colorsTable[t][1] > 0 ? 0xFF : colorsTable[t][1];
            int screenB = colorsTable[t][2] > 0 ? 0xFF : colorsTable[t][2];

            buttonList[i][j].setBackgroundColor(Color.rgb(screenR, screenG, screenB));

            soundPoolList[i][j].play(soundIds[i][j], 1f, 1f, 0,0, 1f);

            FLEDControl(fullLED[i][j], r, g, b);

            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode != 0){
            Integer key = keyMap.get(keyCode);

            if (key == null) return false;

            int i = key / 4;
            int j = key % 4;

            buttonList[i][j].setBackgroundColor(getResources().getColor(R.color.gray));
            FLEDControl(ALL_LED, 0, 0, 0);

            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                soundPoolList[i][j].release();
            }
        }
    }


}