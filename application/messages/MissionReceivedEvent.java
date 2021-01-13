package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.List;

public class MissionReceivedEvent implements Event<Void> {

    MissionInfo missionInfo;

    public MissionReceivedEvent(MissionInfo missionInfo){
        this.missionInfo = missionInfo;
    }

    public List<String> getAgentsSerialNumber(){
        return missionInfo.getSerialAgentsNumbers();
    }

    public String getGadget(){
        return missionInfo.getGadget();
    }

    public int getTimeIssued(){
        return missionInfo.getTimeIssued();
    }

    public int getTimeExpired(){
        return missionInfo.getTimeExpired();
    }

    public int getDuration(){
        return missionInfo.getDuration();
    }

    public String getMissionName(){
        return missionInfo.getMissionName();
    }
}
