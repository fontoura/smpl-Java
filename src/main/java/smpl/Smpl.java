/*
 * Copyright (c) 2020 Felipe Michels Fontoura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package smpl;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A discrete event simulation subsystem.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
public final class Smpl
{
	/**
	 * The pseudo-random number generator.
	 */
	private final Rand randomNumberGenerator = new Rand();

	/**
	 * Facility collection.
	 */
	private final Map<FacilityIdentifier, FacilityData> facilities = new HashMap<FacilityIdentifier, FacilityData>();

	/**
	 * Next available block number.
	 * <p>
	 * Since this version of 'smpl' is not based on the same block-based memory structure as the original, this value is merely used for generating identifiers.
	 */
	private int nextBlockNumber;

	/**
	 * Flag which enables trace log messages.
	 */
	private boolean traceEnabled;

	/**
	 * Current output destination.
	 */
	private PrintStream outputStream;

	/**
	 * The initial time of the simulation.
	 */
	private double start;

	/**
	 * Stream number that should be used during next initialization.
	 */
	private int randomNumberStream = 1;

	/**
	 * The name of the simulation model.
	 */
	private String modelName;

	/**
	 * Available element list header.
	 * <p>
	 * Unlike original 'smpl', the event list is not preallocated. However, discarded event descriptors are reused to prevent excessive allocations.
	 */
	private EventDescriptor availableEventPoolHead;

	/**
	 * Event queue header.
	 * <p>
	 * The event queue is ordered based on the 'triggerTimestamp' field, in ascending order.
	 */
	private EventDescriptor eventQueueHead;

	/**
	 * Current simulation time.
	 */
	private double clock;

	/**
	 * Event code of the last event dispatched by the simulation subsystem.
	 */
	private int lastDispatchedEventCode;

	/**
	 * Token of the last event dispatched by the simulation subsystem.
	 */
	private Object lastDispatchedToken;

	/**
	 * Initializes the simulation subsystem.
	 * <p>
	 * This method resets all facilities, events, statistics and advances to the next random number stream.
	 *
	 * @param s The model name.
	 */
	public void init(String s)
	{
		if (s == null)
		{
			throw new IllegalArgumentException("The model name must be provided!");
		}

		outputStream = System.out;

		// element pool & namespace headers.
		nextBlockNumber = 1;

		// event list & descriptor chain headers.
		eventQueueHead = null;
		availableEventPoolHead = null;
		facilities.clear();

		// sim., interval start, last trace times.
		clock = 0.0;
		start = 0.0;

		// current event no. & trace flags.
		lastDispatchedEventCode = 0;
		traceEnabled = false;

		// save the model name.
		modelName = s;

		// set the pseudo-random stream number.
		randomNumberGenerator.stream(randomNumberStream);
		randomNumberStream = (randomNumberStream + 1) > 15 ? 1 : randomNumberStream;
	}

	/**
	 * Gets the pseudo-random number generator.
	 *
	 * @return The pseudo-random number generator.
	 */
	public final Rand rand()
	{
		return randomNumberGenerator;
	}

	/**
	 * Resets all measurements taken so far.
	 */
	public final void reset()
	{
		for (FacilityIdentifier facilityIdentifier : facilities.keySet())
		{
			FacilityData facilityData = facilities.get(facilityIdentifier);
			facilityData.clearQueueExitCount();
			facilityData.clearPreemptCount();
			facilityData.clearTotalQueueingTime();
			for (int serverNumber = 0; serverNumber < facilityData.getTotalServers(); serverNumber++)
			{
				FacilityServer facilityServer = facilityData.getServer(serverNumber);
				facilityServer.clearReleaseCount();
				facilityServer.clearTotalBusyTime();
			}
		}
		start = clock;
	}

	/**
	 * Gets the simulation model name.
	 *
	 * @return The simulation model name.
	 */
	public final String mname()
	{
		return modelName;
	}

	/**
	 * Gets the name of a facility.
	 *
	 * @param facilityIdentifier The identifier of the facility.
	 * @return The name of the facility.
	 */
	public final String fname(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		return facilityData.getName();
	}

	/**
	 * Gets a facility.
	 *
	 * @param facilityIdentifier The identifier of the facility.
	 * @return The facility.
	 */
	private final FacilityData get_facility(FacilityIdentifier facilityIdentifier)
	{
		if (facilityIdentifier == null)
		{
			throw new IllegalArgumentException("The facility identifier must be provided!");
		}

		FacilityData facilityData = facilities.get(facilityIdentifier);
		if (facilityData == null)
		{
			throw new IllegalArgumentException("The facility identifier is not valid!");
		}

		return facilityData;
	}

	/**
	 * Virtually allocates a number of consecutive data blocks and returns the number of the first block.
	 * <p>
	 * Unlike original 'smpl', this method does not actually allocate the data blocks, since data is not stored in data blocks but in high-level objects.
	 * <p>
	 * Also, unlike original 'smpl', this method does not fail if the block pool is exhausted (since there is no actual block pool). Also, it does not fail if the simulation has already started.
	 *
	 * @param n The amount of data blocks that should be allocated.
	 * @return The number of the first block.
	 */
	private final int get_blk(int n)
	{
		int index = nextBlockNumber;
		nextBlockNumber += n;
		return index;
	}

	/**
	 * Gets an event descriptor.
	 * <p>
	 * This method tries to get an event descriptor from the available event descriptor pool. If no event descriptor is available, a new one is created.
	 *
	 * @return The event descriptor.
	 */
	private final EventDescriptor get_elm()
	{
		EventDescriptor eventDescriptor = availableEventPoolHead;
		if (eventDescriptor != null)
		{
			availableEventPoolHead = eventDescriptor.getNext();
			eventDescriptor.setNext(null);
		}
		else
		{
			int blockNumber = get_blk(1);
			eventDescriptor = new EventDescriptor(blockNumber);
		}
		return eventDescriptor;
	}

	/**
	 * Recycles an event descriptor which is no longer used.
	 * <p>
	 * This method adds the event descriptor to the the available event descriptor pool.
	 *
	 * @param eventDescriptor The event descriptor.
	 */
	private final void put_elm(EventDescriptor eventDescriptor)
	{
		eventDescriptor.setNext(availableEventPoolHead);
		eventDescriptor.setToken(null);
		availableEventPoolHead = eventDescriptor;
	}

	/**
	 * Schedules an event to be triggered at a later time.
	 *
	 * @param eventCode A number which identifies the event type.
	 * @param token An object which identifies the event target.
	 * @param te The time to event, that is, how much time from from the current time will the event take to be triggered.
	 */
	public final void schedule(int eventCode, double te, Object token)
	{
		if (te < 0.0 || Double.isInfinite(te) || Double.isNaN(te))
		{
			throw new IllegalArgumentException("The time to event must be a finite positive number!");
		}
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}

		EventDescriptor eventDescriptor = get_elm();

		eventDescriptor.setEventCode(eventCode);
		eventDescriptor.setToken(token);
		eventDescriptor.setRemainingTimeToEvent(0.0);
		eventDescriptor.setTriggerTime(clock + te);

		enlist_evl(eventDescriptor);

		if (traceEnabled)
		{
			msg("SCHEDULE EVENT " + eventCode + " FOR TOKEN " + token);
		}
	}

	/**
	 * Causes the next event in the simulated environment and returns an object describing it.
	 * <p>
	 * This method checks which is the next event, advances the virtual time to the time of the event, and returns the event code-token pair.
	 * <p>
	 * Unlike original 'smpl', this method does not crash is the event list is empty. Rather, it returns a null event-code pair.
	 *
	 * @return The event code-token pair.
	 */
	public final CausedEvent cause()
	{
		CausedEvent result = null;

		if (eventQueueHead != null)
		{
			// delink element
			EventDescriptor dequeuedEventDescriptor = eventQueueHead;
			eventQueueHead = dequeuedEventDescriptor.getNext();

			lastDispatchedEventCode = dequeuedEventDescriptor.getEventCode();
			lastDispatchedToken = dequeuedEventDescriptor.getToken();
			clock = dequeuedEventDescriptor.getTriggerTime();

			// return to pool
			put_elm(dequeuedEventDescriptor);

			result = new CausedEvent(lastDispatchedEventCode, lastDispatchedToken);

			if (traceEnabled)
			{
				msg("CAUSE EVENT " + lastDispatchedEventCode + " FOR TOKEN " + lastDispatchedToken);
			}
		}

		return result;
	}

	/**
	 * Gets the current time in the simulated environment.
	 * <p>
	 * This value does not change until {@link #cause()} is invoked.
	 *
	 * @return The current time in the simulated environment.
	 */
	public final double time()
	{
		return clock;
	}

	/**
	 * Cancels an upcoming event based on its event code.
	 *
	 * @param eventCode The event code.
	 * @return The token of the canceled event, or {@code null}.
	 */
	public final Object cancel(int eventCode)
	{
		// search for the event in the event queue.
		EventDescriptor predEventDescriptor = null;
		EventDescriptor succEventDescriptor = eventQueueHead;
		while (succEventDescriptor != null && succEventDescriptor.getEventCode() != eventCode)
		{
			predEventDescriptor = succEventDescriptor;
			succEventDescriptor = predEventDescriptor.getNext();
		}

		// removes the event from the event queue.
		Object token = null;
		if (succEventDescriptor != null)
		{
			token = succEventDescriptor.getToken();
			if (traceEnabled)
			{
				msg("CANCEL EVENT " + succEventDescriptor.getEventCode() + " FOR TOKEN " + token);
			}

			if (succEventDescriptor == eventQueueHead)
			{
				// unlink event */
				eventQueueHead = succEventDescriptor.getNext();
			}
			else
			{
				// list entry & deallocate it
				predEventDescriptor.setNext(succEventDescriptor.getNext());
			}
			put_elm(succEventDescriptor);
		}

		return token;
	}

	/**
	 * Reverts the scheduling of an upcoming event based on its event code and token.
	 *
	 * @param eventCode The event code.
	 * @param token An object which identifies the event target.
	 * @return A boolean indicating if the event was cancelled.
	 */
	public final boolean unschedule(int eventCode, Object token)
	{
		// search for the event in the event queue.
		EventDescriptor predEventDescriptor = null;
		EventDescriptor succEventDescriptor = eventQueueHead;
		while (succEventDescriptor != null && (succEventDescriptor.getEventCode() != eventCode || !Objects.equals(succEventDescriptor.getToken(), token)))
		{
			predEventDescriptor = succEventDescriptor;
			succEventDescriptor = predEventDescriptor.getNext();
		}

		// removes the event from the event queue.
		boolean cancelled = false;
		if (succEventDescriptor != null)
		{
			cancelled = true;

			if (traceEnabled)
			{
				msg("UNSCHEDULE EVENT " + succEventDescriptor.getEventCode() + " FOR TOKEN " + token);
			}

			if (succEventDescriptor == eventQueueHead)
			{
				// unlink event
				eventQueueHead = succEventDescriptor.getNext();
			}
			else
			{
				// list entry & deallocate it
				predEventDescriptor.setNext(succEventDescriptor.getNext());
			}
			put_elm(succEventDescriptor);
		}

		return cancelled;
	}

	/**
	 * Suspends and upcoming event.
	 * <p>
	 * This method removes an event from the event queue based on the event token, and returns its event descriptor.
	 *
	 * @param token An object which identifies the event target.
	 * @return The event descriptor.
	 */
	private final EventDescriptor suspend(Object token)
	{
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}

		// search for the event in the event queue.
		EventDescriptor predEventDescriptor = null;
		EventDescriptor succEventDescriptor = eventQueueHead;
		while (succEventDescriptor != null && !Objects.equals(succEventDescriptor.getToken(), token))
		{
			predEventDescriptor = succEventDescriptor;
			succEventDescriptor = predEventDescriptor.getNext();
		}

		// if no event has been scheduled for token, an exception must be thrown.
		if (succEventDescriptor == null)
		{
			throw new IllegalArgumentException("There is no event scheduled for given token!");
		}

		// removes the event from the event queue.
		if (succEventDescriptor == eventQueueHead)
		{
			eventQueueHead = succEventDescriptor.getNext();
		}
		else
		{
			predEventDescriptor.setNext(succEventDescriptor.getNext());
		}

		if (traceEnabled)
		{
			msg("SUSPEND EVENT " + succEventDescriptor.getEventCode() + " FOR TOKEN " + token);
		}

		return succEventDescriptor;
	}

	/**
	 * Adds an event descriptor to the event queue.
	 *
	 * @param eventDescriptor The event descriptor.
	 */
	private final void enlist_evl(EventDescriptor eventDescriptor)
	{
		// scan for position to insert the event descriptor.
		EventDescriptor predEventDescriptor = null;
		EventDescriptor succEventDescriptor = eventQueueHead;
		while (true)
		{
			if (succEventDescriptor == null)
			{
				// end of list
				break;
			}
			else
			{
				if (succEventDescriptor.getTriggerTime() > eventDescriptor.getTriggerTime())
				{
					break;
				}
			}
			predEventDescriptor = succEventDescriptor;
			succEventDescriptor = predEventDescriptor.getNext();
		}

		// adds the event descriptor to the list.
		eventDescriptor.setNext(succEventDescriptor);
		if (succEventDescriptor != eventQueueHead)
		{
			predEventDescriptor.setNext(eventDescriptor);
		}
		else
		{
			eventQueueHead = eventDescriptor;
		}
	}

	/**
	 * Adds an event descriptor to the queue of a facility.
	 *
	 * @param facilityData The facility.
	 * @param eventDescriptor The event descriptor.
	 */
	private final void enlist_facilityEvq(FacilityData facilityData, EventDescriptor eventDescriptor)
	{
		// 'head' points to head of queue/event list
		EventDescriptor predEventDescriptor = null;
		EventDescriptor succEventDescriptor = facilityData.getHeadEventDescriptor();
		while (true)
		{
			// scan for position to insert entry: event list is ordered in ascending 'arg' values, queues in descending order
			if (succEventDescriptor == null)
			{
				// end of list
				break;
			}
			else
			{
				int v = succEventDescriptor.getPriority();
				int arg = eventDescriptor.getPriority();

				// queue: if entry is for a preempted token (l4, the remaining event time, >0), insert entry at beginning of its priority class; otherwise, insert it at the end
				if ((v < arg) || ((v == arg) && (eventDescriptor.getRemainingTimeToEvent() > 0.0)))
				{
					break;
				}
			}
			predEventDescriptor = succEventDescriptor;
			succEventDescriptor = predEventDescriptor.getNext();
		}

		eventDescriptor.setNext(succEventDescriptor);
		if (succEventDescriptor != facilityData.getHeadEventDescriptor())
		{
			predEventDescriptor.setNext(eventDescriptor);
		}
		else
		{
			facilityData.setHeadEventDescriptor(eventDescriptor);
		}
	}

	/**
	 * Creates a facility with a given name and number of available servers.
	 *
	 * @param facilityName The name of the facility.
	 * @param totalServers The number of servers.
	 * @return The unique identifier of the facility.
	 */
	public final FacilityIdentifier facility(String facilityName, int totalServers)
	{
		if (totalServers <= 0)
		{
			throw new IllegalArgumentException("The facility must have at least one server!");
		}

		int blockNumber = get_blk(totalServers + 2);
		FacilityIdentifier newFacilityIdentifier = new FacilityIdentifier(blockNumber);
		FacilityData newFacilityData = new FacilityData(blockNumber, facilityName, totalServers);

		facilities.put(newFacilityIdentifier, newFacilityData);

		if (traceEnabled)
		{
			msg("CREATE FACILITY " + facilityName + " WITH ID " + newFacilityIdentifier);
		}

		return newFacilityIdentifier;
	}

	/**
	 * Requests a facility.
	 * <p>
	 * This method attempts to reserve (take ownership) over a non-busy server of a given facility.
	 * <ul>
	 * <li>If there is a non-busy server, this method will reserve it and return {@link RequestResult#RESERVED}.</li>
	 * <li>If all servers are busy, this method will enqueue a request on the facility queue and return {@link RequestResult#RESERVED}. Once a server is non-busy again, an event - with the same cause as the last event and the provided token - will be triggered.</li>
	 * </ul>
	 * <p>
	 * This method should probably be invoked with the same token as the previous event, but it is not mandatory to do so.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @param token An object which identifies the event target.
	 * @param priority Priority of the request. Higher values mean higher priority.
	 *
	 * @return A value indicating if a facility server was requested.
	 */
	public final RequestResult request(FacilityIdentifier facilityIdentifier, Object token, int priority)
	{
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}

		RequestResult result;

		FacilityData facilityData = get_facility(facilityIdentifier);
		if (facilityData.getBusyServers() < facilityData.getTotalServers())
		{
			// facility nonbusy - reserve 1st-found nonbusy server
			FacilityServer chosenServer = null;
			for (int iterServerNumber = 0; iterServerNumber < facilityData.getTotalServers(); iterServerNumber++)
			{
				FacilityServer iterServer = facilityData.getServer(iterServerNumber);
				if (iterServer.getBusyToken() == null)
				{
					chosenServer = iterServer;
					break;
				}
			}

			chosenServer.setBusyToken(token);
			chosenServer.setBusyPriority(priority);
			chosenServer.setBusyTime(clock);

			facilityData.incrementBusyServers();

			if (traceEnabled)
			{
				msg("REQUEST FACILITY " + facilityData.getName() + " FOR TOKEN " + token + ":  RESERVED");
			}

			result = RequestResult.RESERVED;
		}
		else
		{
			// facility busy - enqueue token marked w/event, priority
			enqueue(facilityData, token, priority, lastDispatchedEventCode, 0.0);

			if (traceEnabled)
			{
				msg("REQUEST FACILITY " + facilityData.getName() + " FOR TOKEN " + token + ":  QUEUED  (inq = " + facilityData.getEventQueueLength() + ")");
			}

			result = RequestResult.QUEUED;
		}

		return result;
	}

	/**
	 * Enqueues an event for a given token in a faility.
	 * <p>
	 * This method enqueues a request on the queue of a facility.
	 *
	 * @param facilityData The facility.
	 * @param token An object which identifies the event target.
	 * @param pri The priority of the event.
	 * @param ev The event number.
	 * @param te The remaining time to event.
	 */
	private final void enqueue(FacilityData facilityData, Object token, int pri, int ev, double te)
	{
		facilityData.increaseTotalQueueingTime(facilityData.getEventQueueLength() * (clock - facilityData.getTimeOfLastChange()));
		facilityData.incrementEventQueueLength();
		facilityData.setTimeOfLastChange(clock);

		EventDescriptor eventDescriptor = get_elm();
		eventDescriptor.setToken(token);
		eventDescriptor.setEventCode(ev);
		eventDescriptor.setRemainingTimeToEvent(te);
		eventDescriptor.setPriority(pri);

		enlist_facilityEvq(facilityData, eventDescriptor);
	}

	/**
	 * Preempts a facility.
	 * <p>
	 * This method attempts to reserve (take ownership) over a server of a given facility, even if it's busy.
	 * <ul>
	 * <li>If there is a non-busy server, this method will reserve it and return {@link RequestResult#RESERVED}.</li>
	 * <li>If all servers are busy, and all of them have a priority which is higher or the same as this request, this method will enqueue a request on the facility queue and return {@link RequestResult#QUEUED}. Once a server is non-busy again, an event with the same cause as the last event and the provided token will be triggered.</li>
	 * <li>If all servers are busy, and at least one of them have lesser priority than this request, one of the servers which was requested with the lowest priority will be forcefully released. Also, the most recent event scheduled by the owner of the server (identified by its the token) will be suspended and added to the queue of facility. Once a server is available again, it will again be reserved for the previous owner, and the event which was previously suspended will be scheduled again, after the same amount of time the event had from the preemption time. In this case, the method will return {@link RequestResult#RESERVED}.</li>
	 * </ul>
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @param token An object which identifies the event target.
	 * @param priority Priority of the request. Higher values mean higher priority.
	 *
	 * @return A value indicating if a facility server was requested.
	 */
	public final RequestResult preempt(FacilityIdentifier facilityIdentifier, Object token, int priority)
	{
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}
		RequestResult result;

		FacilityData facilityData = get_facility(facilityIdentifier);
		FacilityServer chosenServer = null;
		if (facilityData.getBusyServers() < facilityData.getTotalServers())
		{
			// facility nonbusy - locate 1st-found nonbusy server
			for (int iterServerNumber = 0; iterServerNumber < facilityData.getTotalServers(); iterServerNumber++)
			{
				FacilityServer iterServer = facilityData.getServer(iterServerNumber);
				if (iterServer.getBusyToken() == null)
				{
					chosenServer = iterServer;
					break;
				}
			}

			result = RequestResult.RESERVED;

			if (traceEnabled)
			{
				msg("PREEMPT FACILITY " + facilityData.getName() + " FOR TOKEN " + token + ":  RESERVED");
			}
		}
		else
		{
			// facility busy - find server with lowest-priority user

			// indices of server elements 1 & n
			chosenServer = facilityData.getServer(0);
			for (int iterServerNumber = 1; iterServerNumber < facilityData.getTotalServers(); iterServerNumber++)
			{
				FacilityServer iterServer = facilityData.getServer(iterServerNumber);
				if (iterServer.getBusyPriority() < chosenServer.getBusyPriority())
				{
					chosenServer = iterServer;
				}
			}

			if (priority <= chosenServer.getBusyPriority())
			{
				// requesting token's priority is not higher than
				// that of any user: enqueue requestor & return r=1
				enqueue(facilityData, token, priority, lastDispatchedEventCode, 0.0);
				result = RequestResult.QUEUED;
				if (traceEnabled)
				{
					msg("PREEMPT FACILITY " + facilityData.getName() + " FOR TOKEN " + token + ":  QUEUED  (inq = " + facilityData.getEventQueueLength() + ")");
				}
			}
			else
			{
				// preempt user of server k. suspend event, save
				// event number & remaining event time, & enqueue
				// preempted token. If remaining event time is 0
				// (preemption occurred at the instant release was
				// to occur, set 'te' > 0 for proper enqueueing
				// (see 'enlist'). Update facility & server stati-
				// stics for the preempted token, and set r = 0 to
				// reserve the facility for the preempting token.
				if (traceEnabled)
				{
					msg("PREEMPT FACILITY " + facilityData.getName() + " FOR TOKEN " + token + ":  INTERRUPT");
				}

				Object preemptedToken = chosenServer.getBusyToken();
				EventDescriptor preemptedEventDescriptor = suspend(preemptedToken);

				int ev = preemptedEventDescriptor.getEventCode();
				double te = preemptedEventDescriptor.getTriggerTime() - clock;
				if (te == 0.0)
				{
					te = 1.0e-99;
				}

				put_elm(preemptedEventDescriptor);

				enqueue(facilityData, preemptedToken, chosenServer.getBusyPriority(), ev, te);
				if (traceEnabled)
				{
					msg("QUEUE FOR TOKEN " + preemptedToken + " (inq = " + facilityData.getEventQueueLength() + ")");
					msg("RESERVE " + facilityData.getName() + " FOR TOKEN " + token + ":  RESERVED");
				}

				chosenServer.incrementReleaseCount();
				chosenServer.increaseTotalBusyTime(clock - chosenServer.getBusyTime());

				facilityData.decrementBusyServers();
				facilityData.incrementPreemptCount();
				result = RequestResult.RESERVED;
			}
		}

		if (result == RequestResult.RESERVED)
		{
			// reserve server k of facility
			chosenServer.setBusyToken(token);
			chosenServer.setBusyPriority(priority);
			chosenServer.setBusyTime(clock);

			facilityData.incrementBusyServers();
		}

		return result;
	}

	/**
	 * Releases a facility.
	 * <p>
	 * This method attempts to release (let go of the ownership) a busy server of a given facility.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @param token An object which identifies the event target.
	 */
	public final void release(FacilityIdentifier facilityIdentifier, Object token)
	{
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}

		// locate server (j) reserved by releasing token
		FacilityData facilityData = get_facility(facilityIdentifier);

		FacilityServer matchingServer = null;
		for (int iterServerNumber = 0; iterServerNumber < facilityData.getTotalServers(); iterServerNumber++)
		{
			FacilityServer iterServer = facilityData.getServer(iterServerNumber);
			if (Objects.equals(iterServer.getBusyToken(), token))
			{
				matchingServer = iterServer;
				break;
			}
		}

		if (matchingServer == null)
		{
			// no server reserved
			throw new UnsupportedOperationException("There is no server reserved for the token in the facility.");
		}

		matchingServer.setBusyToken(null);

		matchingServer.incrementReleaseCount();
		matchingServer.increaseTotalBusyTime(clock - matchingServer.getBusyTime());

		facilityData.decrementBusyServers();

		if (traceEnabled)
		{
			msg("RELEASE FACILITY " + facilityData.getName() + " FOR TOKEN " + token);
		}

		if (facilityData.getEventQueueLength() > 0)
		{
			// queue not empty: dequeue request ('k' = index of element) & update queue measures
			EventDescriptor dequeuedEventDescriptor = facilityData.getHeadEventDescriptor();
			facilityData.setHeadEventDescriptor(dequeuedEventDescriptor.getNext());

			double te = dequeuedEventDescriptor.getRemainingTimeToEvent();
			facilityData.increaseTotalQueueingTime(facilityData.getEventQueueLength() * (clock - facilityData.getTimeOfLastChange()));
			facilityData.decrementEventQueueLength();
			facilityData.incrementQueueExitCount();
			facilityData.setTimeOfLastChange(clock);
			if (traceEnabled)
			{
				msg("DEQUEUE FOR TOKEN " + dequeuedEventDescriptor.getToken() + "  (inq = " + facilityData.getEventQueueLength() + ")");
			}

			if (te == 0.0)
			{
				// blocked request: place request at head of event list (so its facility request can be re-initiated before any other requests scheduled for this time)
				dequeuedEventDescriptor.setTriggerTime(clock);
				dequeuedEventDescriptor.setNext(eventQueueHead);
				eventQueueHead = dequeuedEventDescriptor;

				if (traceEnabled)
				{
					msg("RESCHEDULE EVENT " + dequeuedEventDescriptor.getEventCode() + " FOR TOKEN " + dequeuedEventDescriptor.getToken());
				}
			}
			else
			{
				// return after preemption: reserve facility for dequeued request & reschedule remaining event time
				matchingServer.setBusyToken(dequeuedEventDescriptor.getToken());
				matchingServer.setBusyPriority(dequeuedEventDescriptor.getPriority());
				matchingServer.setBusyTime(clock);

				facilityData.incrementBusyServers();

				if (traceEnabled)
				{
					msg("RESERVE " + fname(facilityIdentifier) + " FOR TOKEN " + dequeuedEventDescriptor.getToken());
				}

				dequeuedEventDescriptor.setTriggerTime(clock + te);
				enlist_evl(dequeuedEventDescriptor);

				if (traceEnabled)
				{
					msg("RESUME EVENT " + dequeuedEventDescriptor.getEventCode() + " FOR TOKEN " + dequeuedEventDescriptor.getToken());
				}
			}
		}
	}

	/**
	 * Gets the status of a facility.
	 * <p>
	 * This method returns {@code true} if a facility is busy (that is, if all its servers are busy).
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @return A value indicating if the facility is busy.
	 */
	public final boolean status(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		return facilityData.getBusyServers() == facilityData.getTotalServers();
	}

	/**
	 * Gets current queue length of a facility.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @return The current queue length of the facility.
	 */
	public final int inq(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		return facilityData.getEventQueueLength();
	}

	/**
	 * Gets the utilization of a facility.
	 * <p>
	 * This is the sum of the percentage of the time in which each of the facility servers was busy.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @return The current queue length of the facility.
	 *
	 */
	public final double U(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		double b = 0.0;
		double t = clock - start;
		if (t > 0.0)
		{
			for (int serverNumber = 0; serverNumber < facilityData.getTotalServers(); serverNumber++)
			{
				FacilityServer facilityServer = facilityData.getServer(serverNumber);
				b += facilityServer.getTotalBusyTime();
			}
			b /= t;
		}
		return b;
	}

	/**
	 * Gets the mean busy time of a facility.
	 * <p>
	 * The busy time of a facility server is the timespan after that ranges from its request to its release.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @return The mean busy time of the facility.
	 */
	public final double B(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		int n = 0;
		double b = 0.0;
		for (int serverNumber = 0; serverNumber < facilityData.getTotalServers(); serverNumber++)
		{
			FacilityServer facilityServer = facilityData.getServer(serverNumber);
			b += facilityServer.getTotalBusyTime();
			n += facilityServer.getReleaseCount();
		}
		return ((n > 0) ? b / n : b);
	}

	/**
	 * Gets the average queue length of a facility.
	 *
	 * @param facilityIdentifier Identifier of the facility.
	 * @return The average queue length of the facility.
	 */
	public final double Lq(FacilityIdentifier facilityIdentifier)
	{
		FacilityData facilityData = get_facility(facilityIdentifier);
		double t = clock - start;
		return ((t > 0.0) ? (facilityData.getTotalQueueingTime() / t) : 0.0);
	}

	/**
	 * Turns trace on or off.
	 *
	 * @param n {@code true} if trace should be on.
	 */
	public final void trace(boolean n)
	{
		traceEnabled = n;
	}

	/**
	 * Checks whether trace is on or off.
	 *
	 * @return {@code true} if trace is on.
	 */
	public final boolean trace()
	{
		return traceEnabled;
	}

	/**
	 * Prints a log message with a timestamp.
	 *
	 * @param message The log message.
	 */
	private final void msg(String message)
	{
		outputStream.println(String.format("At time %12.3f -- %s", clock, message));
	}

	/**
	 * Generates a report message on the output stream.
	 */
	public final void report()
	{
		Iterator<FacilityIdentifier> f = facilities.keySet().iterator();
		if (!f.hasNext())
		{
			outputStream.println("no facilities defined:  report abandoned");
		}
		else
		{
			outputStream.println();
			outputStream.println("smpl SIMULATION REPORT");
			outputStream.println();
			outputStream.println();

			outputStream.println(String.format("MODEL %-56sTIME: %11.3f", mname(), clock));
			outputStream.println(String.format("%68s%11.3f", "INTERVAL: ", clock - start));
			outputStream.println();
			outputStream.println("MEAN BUSY     MEAN QUEUE        OPERATION COUNTS");
			outputStream.println(" FACILITY          UTIL.     PERIOD        LENGTH     RELEASE   PREEMPT   QUEUE");

			while (f.hasNext())
			{
				FacilityIdentifier facilityIdentifier = f.next();
				FacilityData facilityData = facilities.get(facilityIdentifier);

				int n = 0;
				for (int serverNumber = 0; serverNumber < facilityData.getTotalServers(); serverNumber++)
				{
					FacilityServer facilityServer = facilityData.getServer(serverNumber);
					n += facilityServer.getReleaseCount();
				}

				String fn;
				if (facilityData.getTotalServers() == 1)
				{
					fn = facilityData.getName();
				}
				else
				{
					fn = facilityData.getName() + "[" + facilityData.getTotalServers() + "]";
				}

				outputStream.println(String.format(" %-17s%6.4f %10.3f %13.3f %11d %9d %7d", fn, U(facilityIdentifier), B(facilityIdentifier), Lq(facilityIdentifier), n, facilityData.getPreemptCount(), facilityData.getQueueExitCount()));
			}
		}
	}

	/**
	 * Redirect the output stream.
	 *
	 * @param dest The output stream.
	 */
	public final void sendto(PrintStream dest)
	{
		if (dest == null)
		{
			throw new IllegalArgumentException("The output stream must be provided!");
		}
		outputStream = dest;
	}

	/**
	 * Gets the redirected output stream.
	 *
	 * @return The output stream.
	 */
	public final PrintStream sendto()
	{
		return outputStream;
	}
}
