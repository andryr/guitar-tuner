/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.guitartuner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by andry on 24/04/16.
 */
public class AudioProcessor implements Runnable {

    private static final int[] SAMPLE_RATES = {44100, 22050, 16000, 11025, 8000};


    public interface PitchDetectionListener {
        void onPitchDetected(float freq, double avgIntensity);
    }

    private float mLastComputedFreq = 0;

    private AudioRecord mAudioRecord;
    private PitchDetectionListener mPitchDetectionListener;
    private boolean mStop = false;

    public AudioProcessor(PitchDetectionListener pitchDetectionListener) {
        mPitchDetectionListener = pitchDetectionListener;
    }

    private void initializeAudioRecord() {
        int bufSize = 16384;
        int avalaibleSampleRates = SAMPLE_RATES.length;
        int i = 0;
        do {
            int sampleRate = SAMPLE_RATES[i];
            int minBufSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (minBufSize != AudioRecord.ERROR_BAD_VALUE && minBufSize != AudioRecord.ERROR) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(bufSize, minBufSize * 4));
            }
            i++;
        }
        while (i < avalaibleSampleRates && mAudioRecord != null && mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED);
    }

    public void stop() {
        mStop = true;
    }

    @Override
    public void run() {
        initializeAudioRecord();


        mAudioRecord.startRecording();
        int bufSize = 8192;
        final int sampleRate = mAudioRecord.getSampleRate();
        final short[] buffer = new short[bufSize];
        do {
            final int read = mAudioRecord.read(buffer, 0, bufSize);
            if (read > 0) {
                final double intensity = averageIntensity(buffer, read);

                if (intensity >= 50 && zeroCrossing(buffer) <= 250) {

                    float freq = getPitch(buffer, read / 4, read, sampleRate, 50, 500);
                    if (Math.abs(freq - mLastComputedFreq) <= 5f) {
                        mPitchDetectionListener.onPitchDetected(freq, intensity);
                    }
                    mLastComputedFreq = freq;


                }
            }
        } while (!mStop);
        mAudioRecord.stop();
        mAudioRecord.release();
    }

    private double averageIntensity(short[] data, int frames) {

        double sum = 0;
        for (int i = 0; i < frames; i++) {
            sum += Math.abs(data[i]);
        }
        return sum / frames;

    }

    private int zeroCrossing(short[] data) {
        int len = data.length;
        int count = 0;
        boolean prevValPositive = data[0] >= 0;
        for (int i = 1; i < len; i++) {
            boolean positive = data[i] >= 0;
            if (prevValPositive == !positive)
                count++;

            prevValPositive = positive;
        }
        return count;
    }

    private float getPitch(short[] data, int windowSize, int frames, float sampleRate, float minFreq, float maxFreq) {

        float maxOffset = sampleRate / minFreq;
        float minOffset = sampleRate / maxFreq;


        int minSum = Integer.MAX_VALUE;
        int minSumLag = 0;

        for (int lag = (int) minOffset; lag <= maxOffset; lag++) {
            int sum = 0;
            for (int i = 0; i < windowSize; i++) {

                int oldIndex = i - lag;

                int sample = ((oldIndex < 0) ? data[frames + oldIndex] : data[oldIndex]);

                sum += Math.abs(sample - data[i]);
            }

            if (sum < minSum) {
                minSum = sum;
                minSumLag = lag;
            }
        }

        return sampleRate / minSumLag;
    }


}
