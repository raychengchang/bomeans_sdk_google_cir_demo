package com.bomeans.bomeans_sdk_google_cir_demo;

import android.content.Context;
import android.hardware.ConsumerIrManager;

import com.bomeans.IRKit.*;

import java.util.ArrayList;


/**
 * Created by Bomeans on 2017/3/27.
 */

public class MyCIRDevice implements BIRIrHW {

    private BIRReceiveDataCallback2 mCallback;

    private ConsumerIrManager mGoogleCIRMgr = null;

    public MyCIRDevice(Context context) {
        mGoogleCIRMgr = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        if (!mGoogleCIRMgr.hasIrEmitter()) {
            mGoogleCIRMgr = null;
        }
    }

    @Override
    public int SendIR(int frequency, int[] pattern) {
        if (null != mGoogleCIRMgr) {
            mGoogleCIRMgr.transmit(frequency, pattern);
        }

        return IRKit.BIROK;
    }

    @Override
    public int sendMultipIR(int[] frequencyArray, ArrayList<int[]> patternArray) {
        if (null != mGoogleCIRMgr) {
            for (int i = 0; i < frequencyArray.length; i++) {
                mGoogleCIRMgr.transmit(frequencyArray[i], patternArray.get(i));
            }
        }

        return IRKit.BIROK;
    }

    @Override
    public int sendUARTCommand(byte[] commandData) {

        byte commandID = getCommandId(commandData);

        if (commandID == 0x30) { // if it's switching learning mode command

            // synchronous call, better run it in another thread since this may take 15 seconds before timeout
            MyLearningResult result = consumerIRLearn();

            if (result == null) {
                // failed
                mCallback.onLearningFailed();
            }
            else {
                // succeeded
                mCallback.onLearningDataReceived(result.frequency, result.pattern);
            }

        } else {
            // there may be other command such as reading firmware version, but we just ignore them all
        }

        return IRKit.BIROK;
    }

    @Override
    public int getHwType() {
        // not been referenced so any returned value would do so far
        return BIRIrHW.BIR_DirectConnect;
    }

    @Override
    public int isConnection() {
        // not been referenced so any returned value would do so far
        return IRKit.BIROK;
    }

    @Override
    public void setReceiveDataCallback(BIRReceiveDataCallback2 birReceiveDataCallback2) {
        mCallback = birReceiveDataCallback2;
    }

    // this class just simulate the returned ir learning data structure (ir learning result)
    private MyLearningResult consumerIRLearn() {
        try {
            Thread.sleep(2000); // simulate the waiting time for IR signals
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MyLearningResult result = new MyLearningResult();
        result.frequency = 38000;
        result.pattern = new int[]
                { 3370,1680,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,1260,420,1260,420,1260,420,1260,420,420,420,420,420,1260,420,420,420,1260,420,1260,420,1260,420,1260,420,420,420,1260,420,67450,3370,1680,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,420,1260,420,420,420,1260,420,1260,420,1260,420,1260,420,420,420,420,420,1260,420,420,420,1260,420,1260,420,1260,420,1260,420,420,420,1260,420,67450};

        result.len = result.pattern.length;

        return result;
    }

    // Get the command id
    // All the command in/out to/from the IR hardware is packed into a form as:
    // 0xFF, 0x61, 0x00, len0, len1, cmd_id, payload....., checksum, 0xF0
    // since we handle the "switch to IR learning mode" here only, so just filtering out the
    // command id and check if it's the one we are waiting for.
    private byte getCommandId(byte[] data) {
        // basic check
        if ((null == data) || (data.length < 8)) {
            return (byte)0xFF;
        }

        // check prefix bytes and post-fix byte
        if ((data[0] != (byte)0xFF) || (data[1] != 0x61) || data[data.length - 1] != (byte)0xF0) {
            return (byte)0xFF;
        }

        // length check
        int length = data[3] + (((int)data[4]) << 8);   // command (1-byte) + data length + checksum (1-byte)
        if (length + 6 != data.length) {
            return (byte)0xFF;
        }
        int payloadLength = length - 2; // data length

        // checksum
        int checksum = 0;
        for (int i = 2; i < 6 + payloadLength; i++) {
            checksum += data[i];
        }
        checksum &= 0xFF;
        if (data[6 + payloadLength] != (byte)checksum) {
            return (byte)0xFF;
        }

        // return the command ID byte if all the checks are good
        return data[5];
    }

    class MyLearningResult {
        public int len;
        public int frequency;
        public int[] pattern;
    }
}
