package com.bomeans.bomeans_sdk_google_cir_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bomeans.IRKit.BIRReader;
import com.bomeans.IRKit.BIRReaderCallback;
import com.bomeans.IRKit.IRKit;

import java.util.List;

public class MainActivity extends Activity {

    private String API_KEY = "";    // paste your Bomeans API Key here to access the Bomeans IR database

    private BIRReader mMyIrReader;

    private TextView mInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBomeansSDK();

        initializeIRReader();

        mInfoView = (TextView) findViewById(R.id.text_info);

        Button btnSend = (Button)findViewById(R.id.button_learn_ir);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button btnLearn = (Button)findViewById(R.id.button_learn_ir);
        btnLearn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null != mMyIrReader)
                {
                    mMyIrReader.startLearningAndSearchCloud(
                            false, // clear the previous learning result in cache for every new learning
                            BIRReader.PREFER_REMOTE_TYPE.TV, new BIRReader.BIRReaderRemoteMatchCallback() {

                        @Override
                        public void onRemoteMatchSucceeded(List<BIRReader.RemoteMatchResult> list) {

                            String infoStr = "\n\nMatched remote id:\n";
                            for (BIRReader.RemoteMatchResult result : list) {
                                infoStr += result.modelID + "\n";
                            }

                            mInfoView.setText(mInfoView.getText() + infoStr);
                        }

                        @Override
                        public void onRemoteMatchFailed(BIRReader.CloudMatchErrorCode cloudMatchErrorCode) {
                            mInfoView.setText("No matched remote!");
                        }

                        @Override
                        public void onFormatMatchSucceeded(List<BIRReader.ReaderMatchResult> list) {
                            String infoStr = "Matched format:\n";
                            for (BIRReader.ReaderMatchResult result : list) {
                                infoStr += String.format("%s, 0x%X, 0x%X\n", result.formatId, result.customCode, result.keyCode);
                            }

                            mInfoView.setText(infoStr);
                        }

                        @Override
                        public void onFormatMatchFailed(BIRReader.FormatParsingErrorCode formatParsingErrorCode) {
                            mInfoView.setText("No matched format!");
                        }
                    });
                }
            }
        });
    }

    private void initializeBomeansSDK()
    {
        IRKit.setup(API_KEY, getApplicationContext());

        IRKit.setUseChineseServer(false);

        IRKit.setIRHW(new MyCIRDevice(this));
    }

    private void initializeIRReader()
    {
        IRKit.createIRReader(true, new BIRReaderCallback() {
            @Override
            public void onReaderCreated(BIRReader birReader) {
                mMyIrReader = birReader;
            }

            @Override
            public void onReaderCreateFailed() {
                mMyIrReader = null;

                mInfoView.setText("Cannot connect to Bomeans database.\n\n1. Check Internet connection.\n2. Check API Key in code.");
            }
        });
    }

}
