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

    private String API_KEY = "36c3862a5dddca583f3fb7e8effb712c0540ff7de";

    private BIRReader mMyIrReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBomeansSDK();

        initializeIRReader();

        final TextView infoView = (TextView) findViewById(R.id.text_info);

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

                            infoView.setText(infoView.getText() + infoStr);
                        }

                        @Override
                        public void onRemoteMatchFailed(BIRReader.CloudMatchErrorCode cloudMatchErrorCode) {
                            infoView.setText("No matched remote!");
                        }

                        @Override
                        public void onFormatMatchSucceeded(List<BIRReader.ReaderMatchResult> list) {
                            String infoStr = "Matched format:\n";
                            for (BIRReader.ReaderMatchResult result : list) {
                                infoStr += String.format("%s, 0x%X, 0x%X\n", result.formatId, result.customCode, result.keyCode);
                            }

                            infoView.setText(infoStr);
                        }

                        @Override
                        public void onFormatMatchFailed(BIRReader.FormatParsingErrorCode formatParsingErrorCode) {
                            infoView.setText("No matched format!");
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
            }
        });
    }

}
