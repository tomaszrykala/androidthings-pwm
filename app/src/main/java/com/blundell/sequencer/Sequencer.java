package com.blundell.sequencer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Sequencer {

    public static final String DEFAULT_BUS = "PWM1";

    private final Handler buzzerSongHandler;
    private final String buzzerPin;
    private final Pwm bus;

    private Queue<Music.Note> sequence;

    private Sequencer(Pwm bus, String buzzerPin) {
        this.bus = bus;
        this.buzzerPin = buzzerPin;

        final HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        buzzerSongHandler = new Handler(handlerThread.getLooper());
    }

    public static Sequencer onDefaultBus() {
        return onBus(DEFAULT_BUS);
    }

    public static Sequencer onBus(String buzzerPin) {
        final Pwm bus;
        final PeripheralManagerService service = new PeripheralManagerService();
        try {
            bus = service.openPwm(buzzerPin);
        } catch (IOException e) {
            throw new IllegalStateException(buzzerPin + " bus cannot be opened.", e);
        }

        try {
            bus.setPwmDutyCycle(50);
        } catch (IOException e) {
            throw new IllegalStateException(buzzerPin + " bus cannot be configured.", e);
        }
        return new Sequencer(bus, buzzerPin);
    }

    public void play(List<Music.Note> notes) {
        if (notes != null && !notes.isEmpty()) {
            sequence = new ArrayDeque<>(notes);
            buzzerSongHandler.post(playSong);
        } else {
            throw new IllegalStateException("No notes to play");
        }
    }

    public void stop() {
        buzzerSongHandler.removeCallbacks(playSong);
    }

    public void close() {
        try {
            bus.close();
        } catch (IOException e) {
            Log.e("TUT", buzzerPin + " bus cannot be closed, you may experience errors on next launch.", e);
        }
    }

    private final Runnable playSong = new Runnable() {
        @Override
        public void run() {
            if (sequence.isEmpty()) {
                return;
            }

            final Music.Note note = sequence.poll();

            if (note.isRest()) {
                SystemClock.sleep(note.getPeriod());
            } else {
                try {
                    bus.setPwmFrequencyHz(note.getFrequency());
                    bus.setEnabled(true);
                    SystemClock.sleep(note.getPeriod());
                    bus.setEnabled(false);
                } catch (IOException e) {
                    throw new IllegalStateException(buzzerPin + " bus cannot play note.", e);
                }
            }
            buzzerSongHandler.post(this);
        }
    };
}
