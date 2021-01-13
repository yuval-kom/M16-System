package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {

	private int time;
	private Inventory inventory;

	public Q(String name) {
		super(name);
		time = 0;
		inventory = Inventory.getInstance();
	}

	@Override
	protected void initialize() {

		Callback<GadgetAvailableEvent> callbackGadget = new Callback<GadgetAvailableEvent>() {
			@Override
			public void call(GadgetAvailableEvent g){
				if (inventory.getItem(g.getGadget())){
					complete(g, time);
				}
				else{
					complete(g, -1);
				}
			}
		} ;
		this.subscribeEvent(GadgetAvailableEvent.class, callbackGadget);

		Callback<TickBroadcast> callbackTick = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast t){
				time = t.getTime();
			}
		} ;
		this.subscribeBroadcast(TickBroadcast.class, callbackTick);

		Callback<TerminateBroadcast> callbackTerminate = new Callback<TerminateBroadcast>() {
			@Override
			public void call(TerminateBroadcast t){
				terminate();
			}
		} ;
		this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);
	}
}