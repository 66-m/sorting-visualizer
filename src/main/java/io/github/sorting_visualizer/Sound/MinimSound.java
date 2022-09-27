package io.github.sorting_visualizer.Sound;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import ddf.minim.*;


public class MinimSound extends Sound {

    Minim minim;
    AudioOutput out;
    double timegone;

    public MinimSound(ArrayController arrayController){
        super((arrayController));
        proc = MainController.processing;
        minim = new Minim(proc);
        out = minim.getLineOut(Minim.MONO);
        timegone = System.nanoTime();
    }

    @Override
    public void playSound(int index) {
        int barHeight = (arrayController.get(index) + 1) * (proc.height - 5) / arrayController.getLength();
        out.playNote(0.0f, 0.03f, 1000f * barHeight/proc.height);
        timegone = System.nanoTime();
    }

    @Override
    public void mute(boolean mute) {}
}
