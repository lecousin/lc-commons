package net.lecousin.commons.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Event that does not emit an object.
 * <p>
 * Note that listeners subscribing to the event before it is emitted are ensured to be called in order,
 * however if a listener subscribes while the others listeners are being called, it is not called before a new event is emitted.
 * </p>
 */
@Slf4j
public class EmptyEvent implements Listenable {
	
	private List<Runnable> listeners;
	
	@Override
	public synchronized void listen(Runnable listener) {
		if (listeners == null)
			listeners = new LinkedList<>();
		listeners.add(listener);
	}
	
	@Override
	public synchronized void unlisten(Runnable listener) {
		if (listeners != null && listeners.remove(listener) && listeners.isEmpty())
			listeners = null;
	}
	
	@Override
	public boolean hasListeners() {
		return listeners != null;
	}
	
	/**
	 * Emit the event.
	 */
	public void emit() {
		List<Runnable> list;
		synchronized (this) {
			list = listeners != null ? new ArrayList<>(listeners) : null;
		}
		if (list != null)
			for (Runnable listener : list)
				try {
					listener.run();
				} catch (Exception e) {
					log.error("EmptyEvent listener error", e);
				}
	}
	
}
