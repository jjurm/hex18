package com.treecio.hexplore.ble;

import android.os.AsyncTask;

import java.util.Random;

/**
 * Created by s156386 on 17-3-2018.
 */

public class SimpleStateScheduler extends StateScheduler {
    // The scheduler running on a separate thread
    AsyncScheduler scheduler;

    public SimpleStateScheduler(State stateBroadcasting, State stateDiscovery) {
        super(stateBroadcasting, stateDiscovery);
        scheduler = new AsyncScheduler();
    }

    @Override
    public void start() {
        scheduler.execute(stateBroadcasting, stateDiscovery);
    }

    @Override
    public void stop() {
        scheduler.cancel(true);
    }

    private static class AsyncScheduler extends AsyncTask<State, Void, String> {

        @Override
        protected String doInBackground(State... states) {
            Random rand = new Random();
            while (!Thread.interrupted()) {
                try {
                    // Put the device in broadcast mode
                    states[1].transitionOut();
                    states[0].transitionIn();
                    Thread.sleep(3 * BleConfig.TIME_UNIT);
                    // Put the device in discovery mode
                    states[0].transitionOut();
                    states[1].transitionIn();
                    Thread.sleep(1 * BleConfig.TIME_UNIT);
                    // In broadcast again
                    states[1].transitionOut();
                    states[0].transitionIn();
                    // And discovery mode
                    Thread.sleep(3 * BleConfig.TIME_UNIT);
                    states[0].transitionOut();
                    states[1].transitionIn();

                    // Add a random offset to prevent devices from never finding eachother when their cycles line up
                    Thread.sleep((long) (1 * BleConfig.TIME_UNIT * rand.nextDouble()));
                } catch (InterruptedException e) {
                    // Stop both states when interrupted
                    states[0].transitionOut();
                    states[1].transitionOut();
                    break;
                }

            }
            return "Execution stopped";
        }
    }
}
