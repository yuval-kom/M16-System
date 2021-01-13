package bgu.spl.mics.application.messages;
import bgu.spl.mics.Couple;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

import java.util.List;


public class AgentsAvailableEvent implements Event<Couple> {

    private List<String> serialNumbers;
    private int moneyPenny;
    private int duration;
    private Future<Boolean> future; //complete by M to indicate moneyPenny send\release the agents

    public AgentsAvailableEvent(List<String> serialNumbers, int duration){
        this.serialNumbers = serialNumbers;
        moneyPenny = 0;
        this.duration = duration;
        future = new Future<>();
    }

    public List<String> getSerialNumbers(){
        return serialNumbers;
    }

    public int getMoneyPenny(){return moneyPenny;}

    public Future<Boolean> getFuture(){return future;}

    public int getDuration() {
        return duration;
    }

    public void setMoneyPenny(int moneyPenny){
        this.moneyPenny = moneyPenny;
    }
}
