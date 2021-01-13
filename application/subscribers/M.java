package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Report;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {

    private int time;
    private Diary diary;


    public M(String name) {
        super(name);
        time = 0;
        diary = Diary.getInstance();
    }

    @Override
    protected void initialize() {

        Callback<MissionReceivedEvent> callbackGadget = new Callback<MissionReceivedEvent>() {
            @Override
            public void call(MissionReceivedEvent m) {
                diary.increment();
                AgentsAvailableEvent agentsAvailableEvent = new AgentsAvailableEvent(m.getAgentsSerialNumber(), m.getDuration());
                Future<Couple> fAgents = getSimplePublisher().sendEvent(agentsAvailableEvent);
                //if fAgent == null -> non moneyPenny subscribes to handle this event
                if (fAgents != null) {
                    Couple c = fAgents.get();
                    //if c == null -> moneyPenny unregister
                    //if c.getMoneyPenny() == -1 -> some acquire agents didn't exist in the squad
                    if (c != null && c.getMoneyPenny() != -1) {
                        Future<Integer> fGadget = getSimplePublisher().sendEvent(new GadgetAvailableEvent(m.getGadget()));
                        //if fAgent == null -> Q no subscribe to handle this event
                        if (fGadget != null) {
                            Integer t = fGadget.get();
                            //if t == null -> Q unregister
                            //if t == -1 -> the gadget isn't exist in the inventory
                            if (t != null && t != -1 & time <= m.getTimeExpired()) {
                                agentsAvailableEvent.getFuture().resolve(true);
                                updateDiary(m, fAgents.get().getMoneyPenny(), m.getAgentsSerialNumber(), fAgents.get().getAgentsName(), fGadget.get());
                            }
                        }
                    }
                }
                //if the mission is abort
                if (!agentsAvailableEvent.getFuture().isDone()) {
                    agentsAvailableEvent.getFuture().resolve(false);
                }
                complete(m, null);
            }

        };
        this.subscribeEvent(MissionReceivedEvent.class, callbackGadget);

        Callback<TickBroadcast> callbackTick = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast t) {
                time = t.getTime();
            }
        };
        this.subscribeBroadcast(TickBroadcast.class, callbackTick);

        Callback<TerminateBroadcast> callbackTerminate = new Callback<TerminateBroadcast>() {
            @Override
            public void call(TerminateBroadcast t) {
                terminate();
            }
        };
        this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);

    }

    private void updateDiary(MissionReceivedEvent m, int monneyPenny, List<
            String> agentsSerialNumber, List<String> agentsName, int QTime) {
        Report report = new Report();
        report.setMissionName(m.getMissionName());
        report.setM(Integer.parseInt(getName()));
        report.setMoneypenny(monneyPenny);
        report.setAgentsSerialNumbers(agentsSerialNumber);
        report.setAgentsNames(agentsName);
        report.setGadgetName(m.getGadget());
        report.setTimeIssued(m.getTimeIssued());
        report.setTimeCreated(time);
        report.setQTime(QTime);
        diary.addReport(report);
    }
}
