package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {

    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> topics;
    private ConcurrentHashMap<Subscriber, LinkedBlockingQueue<Message>> messages;
    private ConcurrentHashMap<Event, Future> eventFuture;
    private static MessageBroker instance = new MessageBrokerImpl();

    public MessageBrokerImpl() {
        topics = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
        eventFuture = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        topics.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        topics.get(type).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        topics.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        topics.get(type).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        eventFuture.get(e).resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (Subscriber sub : topics.get(b.getClass()))
            messages.get(sub).add(b);
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        ConcurrentLinkedQueue<Subscriber> subscribers = topics.get(e.getClass());
        Subscriber s;
        if (subscribers == null) {
            return null;
        }

        Future<T> future = new Future<>();
        eventFuture.put(e, future);
        synchronized (e.getClass()) {
            if (subscribers.isEmpty()) {
                //No one subscribe to the event
                return null;
            }
            //Make sure Subscribers get the event in a round-robin manner
            s = subscribers.poll();
            subscribers.add(s);
        }

        //Add the message to the chosen subscriber queue
        synchronized (s) {
            LinkedBlockingQueue<Message> msgOfSub = messages.get(s);
            if (msgOfSub == null)
                return null;
            msgOfSub.add(e);	
        }
        return future;
    }

    @Override
    public void register(Subscriber m) {
        messages.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(Subscriber m) {
        //Remove m from the MsgQ which he subscribe to
        for (Map.Entry<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> unit : topics.entrySet()) {
            synchronized (unit.getKey()) {
                unit.getValue().remove(m);
            }
        }

        //Resolve the await messages
        LinkedBlockingQueue<Message> qOfMsg;
        synchronized (m) {
            qOfMsg = messages.remove(m);
        }
        while (!qOfMsg.isEmpty()) {
            Message msg = qOfMsg.poll();
            Future<?> futureToResolve = eventFuture.get(msg);
            if (futureToResolve != null) {
                futureToResolve.resolve(null);
            }
        }

    }

    @Override
    public Message awaitMessage(Subscriber m) {
        try {
            return messages.get(m).take();
        } catch (InterruptedException e) {
            return null;
        }
    }
}
