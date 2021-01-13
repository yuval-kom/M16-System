package bgu.spl.mics.application.publishers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {

	private int time;
	private int duration;

	public TimeService(String name, int duration) {
		super(name);
		this.duration = duration;
		time = 0;
	}

	@Override
	protected void initialize() {}

	@Override
	public void run() {
		while (time <= duration){
			try {
				Thread.sleep(100);
				getSimplePublisher().sendBroadcast(new TickBroadcast(time));
				if(time == duration){
					getSimplePublisher().sendBroadcast(new TerminateBroadcast());
				}
				time++;
			} catch (InterruptedException e) {
			}
		}
	}
}
