package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int FULL_LED1 = 9;
    public static final int FULL_LED2 = 8;
    public static final int FULL_LED3 = 7;
    public static final int FULL_LED4 = 6;
    public static final int ALL_LED = 5;



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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //System.out.println(getValue());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        //tv.setText(String.valueOf(getValue()));
        String str1 = new String("      sing");
        String str2 = new String("      song");
        IOCtlClear();
        TextLCDOut(str1, str2);


        LEDControl(0b01010010);

        FLEDControl(ALL_LED, 0, 0, 0);
        FLEDControl(ALL_LED, 100, 100, 100);

        try{
            Thread.sleep(3000);
        }catch (Exception e){
        }
        FLEDControl(ALL_LED, 0, 0, 0);

        PiezoControl(1); // 부저 on

        try{
        Thread.sleep(1000);
        }catch (Exception e){
        }

        PiezoControl(0); // 부저 off
    }

    /**
     * A native method that is implemented by the 'myapplication' native library,
     * which is packaged with this application.
     */

}