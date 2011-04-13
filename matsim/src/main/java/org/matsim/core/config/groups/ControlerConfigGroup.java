/* *********************************************************************** *
 * project: org.matsim.*
 * ControlerConfigGroup.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.config.groups;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.core.config.Module;
import org.matsim.core.utils.misc.StringUtils;

public class ControlerConfigGroup extends Module {

	public enum RoutingAlgorithmType {Dijkstra, AStarLandmarks}

	public enum EventsFileFormat {txt, xml}

	private static final long serialVersionUID = 1L;

	public static final String GROUP_NAME = "controler";

	private static final String OUTPUT_DIRECTORY = "outputDirectory";
	private static final String FIRST_ITERATION = "firstIteration";
	private static final String LAST_ITERATION = "lastIteration";
	private static final String ROUTINGALGORITHM_TYPE = "routingAlgorithmType";
	private static final String RUNID = "runId";
	private static final String LINKTOLINK_ROUTING_ENABLED = "enableLinkToLinkRouting";
	/*package*/ static final String EVENTS_FILE_FORMAT = "eventsFileFormat";
	private static final String WRITE_EVENTS_INTERVAL = "writeEventsInterval";
	private static final String WRITE_PLANS_INTERVAL = "writePlansInterval";
	/*package*/ static final String MOBSIM = "mobsim";

	private String outputDirectory = "./output";
	private int firstIteration = 0;
	private int lastIteration = 1000;
	private RoutingAlgorithmType routingAlgorithmType = RoutingAlgorithmType.Dijkstra;

	private boolean linkToLinkRoutingEnabled = false;

	private String runId = null;

	private Set<EventsFileFormat> eventsFileFormats = Collections.unmodifiableSet(EnumSet.of(EventsFileFormat.xml));

	private int writeEventsInterval=10;
	private int writePlansInterval=10;

	private String mobsim = null;

	public ControlerConfigGroup() {
		super(GROUP_NAME);
	}

	@Override
	public String getValue(final String key) {
		if (OUTPUT_DIRECTORY.equals(key)) {
			return getOutputDirectory();
		} else if (FIRST_ITERATION.equals(key)) {
			return Integer.toString(getFirstIteration());
		} else if (LAST_ITERATION.equals(key)) {
			return Integer.toString(getLastIteration());
		} else if (ROUTINGALGORITHM_TYPE.equals(key)){
			return this.getRoutingAlgorithmType().toString();
		} else if (RUNID.equals(key)){
			return this.getRunId();
		} else if (LINKTOLINK_ROUTING_ENABLED.equals(key)){
			return Boolean.toString(this.linkToLinkRoutingEnabled);
		} else if (EVENTS_FILE_FORMAT.equals(key)) {
			boolean isFirst = true;
			StringBuilder str = new StringBuilder();
			for (EventsFileFormat format : this.eventsFileFormats) {
				if (!isFirst) {
					str.append(',');
				}
				str.append(format.toString());
				isFirst = false;
			}
			return str.toString();
		} else if (WRITE_EVENTS_INTERVAL.equals(key)) {
			throw new RuntimeException("use direct getter.  Aborting ..." ) ;
//			return Integer.toString(getWriteEventsInterval());
		} else if (MOBSIM.equals(key)) {
			return getMobsim();
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public void addParam(final String key, final String value) {
		if (OUTPUT_DIRECTORY.equals(key)) {
			setOutputDirectory(value.replace('\\', '/'));
		} else if (FIRST_ITERATION.equals(key)) {
			setFirstIteration(Integer.parseInt(value));
		} else if (LAST_ITERATION.equals(key)) {
			setLastIteration(Integer.parseInt(value));
		} else if (ROUTINGALGORITHM_TYPE.equals(key)){
			if (RoutingAlgorithmType.Dijkstra.toString().equalsIgnoreCase(value)){
				setRoutingAlgorithmType(RoutingAlgorithmType.Dijkstra);
			}
			else if (RoutingAlgorithmType.AStarLandmarks.toString().equalsIgnoreCase(value)){
				setRoutingAlgorithmType(RoutingAlgorithmType.AStarLandmarks);
			}
			else {
				throw new IllegalArgumentException(value + " is not a valid parameter value for key: "+ key + " of config group " + GROUP_NAME);
			}
		} else if (RUNID.equals(key)){
			this.setRunId(value.trim());
		} else if (LINKTOLINK_ROUTING_ENABLED.equalsIgnoreCase(key)){
			if (value != null) {
				this.linkToLinkRoutingEnabled = Boolean.parseBoolean(value.trim());
			}
		} else if (EVENTS_FILE_FORMAT.equals(key)) {
			String[] parts = StringUtils.explode(value, ',');
			Set<EventsFileFormat> formats = EnumSet.noneOf(EventsFileFormat.class);
			for (String part : parts) {
				String trimmed = part.trim();
				if (trimmed.length() > 0) {
					formats.add(EventsFileFormat.valueOf(trimmed));
				}
			}
			this.eventsFileFormats = formats;
		} else if (WRITE_EVENTS_INTERVAL.equals(key)) {
			setWriteEventsInterval(Integer.parseInt(value));
		} else if (WRITE_PLANS_INTERVAL.equals(key)) {
			setWritePlansInterval(Integer.parseInt(value));
		} else if (MOBSIM.equals(key)) {
			setMobsim(value);
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public final TreeMap<String, String> getParams() {
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put(OUTPUT_DIRECTORY, getValue(OUTPUT_DIRECTORY));
		map.put(FIRST_ITERATION, getValue(FIRST_ITERATION));
		map.put(LAST_ITERATION, getValue(LAST_ITERATION));
		map.put(ROUTINGALGORITHM_TYPE, getValue(ROUTINGALGORITHM_TYPE));
		map.put(RUNID, getValue(RUNID));
		map.put(LINKTOLINK_ROUTING_ENABLED, Boolean.toString(this.isLinkToLinkRoutingEnabled()));
		map.put(EVENTS_FILE_FORMAT, getValue(EVENTS_FILE_FORMAT));
		map.put(WRITE_EVENTS_INTERVAL, Integer.toString(this.getWriteEventsInterval()) );
		map.put(WRITE_PLANS_INTERVAL, Integer.toString(this.getWritePlansInterval()) );
		map.put(MOBSIM, getValue(MOBSIM));
		return map;
	}

	@Override
	public final Map<String, String> getComments() {
		Map<String,String> map = super.getComments();
		map.put(ROUTINGALGORITHM_TYPE, "The type of routing (least cost path) algorithm used, may have the values: " + RoutingAlgorithmType.Dijkstra + " or " + RoutingAlgorithmType.AStarLandmarks);
		map.put(RUNID, "An identifier for the current run which is used as prefix for output files and mentioned in output xml files etc.");
		map.put(EVENTS_FILE_FORMAT, "Specifies the file format for writing events. Currently supported: txt, xml. Multiple values can be specified separated by commas (',').");
		map.put(WRITE_EVENTS_INTERVAL, "iterationNumber % writeEventsInterval == 0 defines in which iterations events are written " +
				"to a file. `0' disables events writing completely.");
		map.put(WRITE_PLANS_INTERVAL, "iterationNumber % writePlansInterval == 0 defines (hopefully) in which iterations plans are " +
				"written to a file. `0' disables plans writing completely.  Some plans in early iterations are always written");
		map.put(MOBSIM, "Defines which mobility simulation will be used. Currently supported: queueSimulation, qsim, jdeqsim, multimodalQSim");
		return map;
	}

	/* direct access */

	public void setOutputDirectory(final String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getOutputDirectory() {
		return this.outputDirectory;
	}

	public void setFirstIteration(final int firstIteration) {
		this.firstIteration = firstIteration;
	}

	public int getFirstIteration() {
		return this.firstIteration;
	}

	public void setLastIteration(final int lastIteration) {
		this.lastIteration = lastIteration;
	}

	public int getLastIteration() {
		return this.lastIteration;
	}

	public RoutingAlgorithmType getRoutingAlgorithmType() {
		return this.routingAlgorithmType;
	}

	public void setRoutingAlgorithmType(final RoutingAlgorithmType type) {
		this.routingAlgorithmType = type;
	}

	public String getRunId() {
		return this.runId;
	}

	public void setRunId(final String runid) {
		this.runId = runid;
	}

	public boolean isLinkToLinkRoutingEnabled() {
		return this.linkToLinkRoutingEnabled;
	}

	public void setLinkToLinkRoutingEnabled(final boolean enabled) {
		this.linkToLinkRoutingEnabled = enabled;
	}

	public Set<EventsFileFormat> getEventsFileFormats() {
		return this.eventsFileFormats;
	}

	public void setEventsFileFormats(final Set<EventsFileFormat> eventsFileFormats) {
		this.eventsFileFormats = Collections.unmodifiableSet(EnumSet.copyOf(eventsFileFormats));
	}

	public int getWriteEventsInterval() {
		return this.writeEventsInterval;
	}

	public void setWriteEventsInterval(final int writeEventsInterval) {
		this.writeEventsInterval = writeEventsInterval;
	}

	public String getMobsim() {
		return this.mobsim;
	}

	public void setMobsim(final String mobsim) {
		this.mobsim = mobsim;
	}

	public int getWritePlansInterval() {
		return this.writePlansInterval;
	}

	public void setWritePlansInterval(final int writePlansInterval) {
		this.writePlansInterval = writePlansInterval;
	}
}
