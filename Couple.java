package bgu.spl.mics;

import java.util.List;

public class Couple {

    private int moneyPenny;
    private List<String> agentsName;

    public Couple(int moneyPenny, List<String> agentsName){
        this.moneyPenny = moneyPenny;
        this.agentsName = agentsName;
    }

    public int getMoneyPenny() {
        return moneyPenny;
    }

    public List<String> getAgentsName(){
        return agentsName;
    }

}