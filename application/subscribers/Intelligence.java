package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.List;

/**
 * A Publisher\Subscriber
 * Holds a list of Info objects and sends them
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {

    private List<MissionInfo> missionToSend;
    private int time;

    public Intelligence(String name, List<MissionInfo> missions) {
        super(name);
        missionToSend = missions;
    }

    @Override
    protected void initialize() {

        Callback<TickBroadcast> callbackIntelligence = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast c) {
                time = c.getTime();
                for (MissionInfo m : missionToSend) {
                    if (m.getTimeIssued() == time) {
                        MissionReceivedEvent newMission = new MissionReceivedEvent(m);
                        getSimplePublisher().sendEvent(newMission);
                    }
                }
            }
        };
        this.subscribeBroadcast(TickBroadcast.class, callbackIntelligence);

        Callback<TerminateBroadcast> callbackTerminate = new Callback<TerminateBroadcast>() {
            @Override
            public void call(TerminateBroadcast t) {
                terminate();
            }
        };
        this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);
    }

}