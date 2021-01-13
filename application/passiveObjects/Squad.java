package bgu.spl.mics.application.passiveObjects;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

    private Map<String, Agent> agents = new HashMap<>(); //the String is the serialNumber of the agent
    private static Squad instance = new Squad();

    /**
     * Retrieves the single instance of this class.
     */
    public static Squad getInstance() {
        return instance;
    }

    /**
     * Initializes the squad. This method adds all the agents to the squad.
     * <p>
     *
     * @param agents Data structure containing all data necessary for initialization
     *               of the squad.
     */
    public void load(Agent[] agents) {
        for (int i = 0; i < agents.length; i++) {
            this.agents.put(agents[i].getSerialNumber(), agents[i]);
        }
    }

    /**
     * Releases agents.
     */
    public void releaseAgents(List<String> serials) {
        for (String serialNumber : serials) {
            Agent agent = agents.get(serialNumber);
            agent.release();
            synchronized (agent) {
                agent.notifyAll();
            }
        }
    }

    /**
     * simulates executing a mission by calling sleep.
     *
     * @param time milliseconds to sleep
     */
    public void sendAgents(List<String> serials, int time) {
        try {
            Thread.sleep(time*100);
        } catch (InterruptedException e) {
        }
        releaseAgents(serials);
    }

    /**
     * acquires an agent, i.e. holds the agent until the caller is done with it
     *
     * @param serials the serial numbers of the agents
     * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
     */
    public boolean getAgents(List<String> serials) {
        if (!agents.keySet().containsAll(serials)) {
            return false;
        }
        serials.sort(Comparator.naturalOrder()); //to avoid dead-locks
        try {
            for (String s : serials) {
                Agent agent = agents.get(s);
                synchronized (agent) {
                    while (!agent.isAvailable()) {
                        agent.wait();
                    }
                    agents.get(s).acquire();
                }
            }
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }


    /**
     * gets the agents names
     *
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials) {
        List<String> names = new LinkedList<>();
        for (String serialNumber : serials) {
            String name = agents.get(serialNumber).getName();
            names.add(name);
        }
        return names;
    }

}