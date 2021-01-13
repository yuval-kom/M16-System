package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Squad;


/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {

    private Squad squad;

    public Moneypenny(String name) {
        super(name);
        squad = Squad.getInstance();
    }

    @Override
    protected void initialize() {

        Callback<AgentsAvailableEvent> callbackAvailable = new Callback<AgentsAvailableEvent>() {
            @Override
            public void call(AgentsAvailableEvent a) {
                if (squad.getAgents(a.getSerialNumbers())) {
                    a.setMoneyPenny(Integer.parseInt(getName()));
                    Couple T = new Couple(a.getMoneyPenny(), squad.getAgentsNames(a.getSerialNumbers()));
                    complete(a, T);

                    if (a.getFuture().get()) {
                        //true -> the mission is executes
                        squad.sendAgents(a.getSerialNumbers(), a.getDuration());
                    } else {
                        //false -> the mission is abort
                        squad.releaseAgents(a.getSerialNumbers());
                    }

                } else {
                    Couple T = new Couple(-1, null);
                    complete(a, T);
                }
            }
        };
        this.subscribeEvent(AgentsAvailableEvent.class, callbackAvailable);


        Callback<TerminateBroadcast> callbackTerminate = new Callback<TerminateBroadcast>() {
            @Override
            public void call(TerminateBroadcast t) {
                terminate();
            }
        };
        this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);

    }
}