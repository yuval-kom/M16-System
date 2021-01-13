package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) {
        String inputFile = args[0];
        String inventoryOutputFile = args[1];
        String diaryOutputFile = args[2];

        Gson gson = new Gson();
        FileReader file = null;
        try {
            file = new FileReader(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(file);
        JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);

        //load inventory
        loadInventory(jsonObject);

        //load squad
        loadSquad(jsonObject);

        JsonObject services = jsonObject.getAsJsonObject("services");

        int MNumber = services.get("M").getAsInt();
        int moneyPennyNumber = services.get("Moneypenny").getAsInt();
        int duration = services.get("time").getAsInt();

        //load the missions of intelligence
        JsonArray intelligence = services.get("intelligence").getAsJsonArray();
        List<Intelligence> intelligenceList = loadMissions(intelligence);
        
        List<Thread> threadList = createThread(duration, intelligenceList, MNumber, moneyPennyNumber);

        for (Thread t : threadList) {
            t.start();
        }
        for (Thread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }

        //update output files
        try {
            printToFiles(diaryOutputFile, inventoryOutputFile);
        } catch (IOException e) {
        }
    }

    private static List<Intelligence> loadMissions (JsonArray intelligence){
        List<Intelligence> intelligenceList = new LinkedList<>();
        for (int i = 0; i < intelligence.size(); i++) {
            JsonObject unitOfMissions = intelligence.get(i).getAsJsonObject();
            JsonArray theMissions = unitOfMissions.getAsJsonArray("missions");
            List<MissionInfo> missions = new LinkedList<>();
            for (int j = 0; j < theMissions.size(); j++) {
                JsonObject aMission = theMissions.get(j).getAsJsonObject();
                JsonArray serialAgentsNumbers = aMission.getAsJsonArray("serialAgentsNumbers");
                List<String> theAgents = new LinkedList<>();
                for (int k = 0; k < serialAgentsNumbers.size(); k++) {
                    theAgents.add(serialAgentsNumbers.get(k).getAsString());
                }
                int missionDuration = aMission.get("duration").getAsInt();
                String gadget = aMission.get("gadget").getAsString();
                String missionName = aMission.get("name").getAsString();
                int timeExpired = aMission.get("timeExpired").getAsInt();
                int timeIssued = aMission.get("timeIssued").getAsInt();
                MissionInfo missionToAdd = new MissionInfo();
                missionToAdd.setSerialAgentsNumbers(theAgents);
                missionToAdd.setDuration(missionDuration);
                missionToAdd.setGadget(gadget);
                missionToAdd.setMissionName(missionName);
                missionToAdd.setTimeExpired(timeExpired);
                missionToAdd.setTimeIssued(timeIssued);
                missions.add(missionToAdd);
            }
            intelligenceList.add(new Intelligence("" + i, missions));
        }
        return intelligenceList;
    }

    private static void printToFiles(String diary, String inventory) throws IOException {
        Diary.getInstance().printToFile(diary);
        Inventory.getInstance().printToFile(inventory);
    }

    private static List<Thread> createThread(int duration, List<Intelligence> intelligenceList, int numOfM, int numOfMp) {
        List<Thread> threadList = new LinkedList<>();

        Q q = new Q("Q");
        Thread qThread = new Thread(q);
        threadList.add(qThread);

        for (int i = numOfM; i > 0; i--) {
            M m = new M("" + i);
            Thread mThread = new Thread(m);
            threadList.add(mThread);
        }

        for (int j = numOfMp; j > 0; j--) {
            Moneypenny moneypenny;
            moneypenny = new Moneypenny("" + j);
            Thread moneypennyThread = new Thread(moneypenny);
            threadList.add(moneypennyThread);
        }

        for (Intelligence i : intelligenceList) {
            Thread intelligenceThread = new Thread(i);
            threadList.add(intelligenceThread);
        }

        TimeService timeService = new TimeService("", duration);
        Thread timeServiceThread = new Thread(timeService);
        threadList.add(timeServiceThread);

        return threadList;
    }

    private static void loadInventory(JsonObject jsonObject) {
        JsonArray inventory = jsonObject.getAsJsonArray("inventory");
        String[] inventoryArr = new String[inventory.size()];
        for (int i = 0; i < inventoryArr.length; i++) {
            inventoryArr[i] = inventory.get(i).getAsString();
        }
        Inventory.getInstance().load(inventoryArr);
    }

    private static void loadSquad(JsonObject jsonObject) {
        JsonArray squad = jsonObject.getAsJsonArray("squad");
        Agent[] agents = new Agent[squad.size()];
        for (int i = 0; i < agents.length; i++) {
            JsonObject agent = squad.get(i).getAsJsonObject();
            String name = agent.get("name").getAsString();
            String serialNumber = agent.get("serialNumber").getAsString();
            Agent newAgent = new Agent();
            newAgent.setName(name);
            newAgent.setSerialNumber(serialNumber);
            agents[i] = newAgent;
        }
        Squad.getInstance().load(agents);
    }
}
