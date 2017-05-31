package com.blundell.tut;

import android.app.Activity;
import android.os.Bundle;

import com.blundell.sequencer.Music;
import com.blundell.sequencer.Sequencer;

public class MainActivity extends Activity {

    private Sequencer sequencer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sequencer = Sequencer.onDefaultBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sequencer.play(Music.POKEMON_ANIME_THEME);
    }

    @Override
    protected void onStop() {
        sequencer.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        sequencer.close();
        super.onDestroy();
    }
}
