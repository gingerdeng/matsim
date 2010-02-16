/* *********************************************************************** *
 * project: org.matsim.*
 * OTFTVeh2MVI.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.vis.otfvis.executables;

import java.io.BufferedReader;
import java.io.IOException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.StringUtils;
import org.matsim.ptproject.qsim.QNetwork;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.fileio.OTFFileWriter;
import org.matsim.vis.otfvis.data.fileio.qsim.OTFFileWriterQSimConnectionManagerFactory;
import org.matsim.vis.otfvis.data.fileio.qsim.OTFQSimServerQuadBuilder;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.PositionInfo;

/**
 * This is a standalone executable to convert T.veh.gz files to .mvi files.
 *
 * @author dstrippgen
 */
public class OTFTVeh2MVI extends OTFFileWriter {
	private  String vehFileName = "";

	private final OTFAgentsListHandler.Writer writer = new OTFAgentsListHandler.Writer();

	public OTFTVeh2MVI(QNetwork net, String vehFileName, double interval_s, String outFileName) {
		super(interval_s, new OTFQSimServerQuadBuilder(net), outFileName, new OTFFileWriterQSimConnectionManagerFactory());
		this.vehFileName = vehFileName;
	}

	@Override
	protected void onAdditionalQuadData(OTFConnectionManager connect) {
		this.quad.addAdditionalElement(this.writer);
	}

	private double lastTime = -1;

	private void convert() {

		open();
		// read and convert data from veh-file

		BufferedReader reader = null;
		try {
			reader = IOUtils.getBufferedReader(this.vehFileName);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			reader.readLine(); // header, we do not use it
			String line = null;
			while ( (line = reader.readLine()) != null) {
				String[] result = StringUtils.explode(line, '\t', 16);
				if (result.length == 16) {
					double easting = Double.parseDouble(result[11]);
					double northing = Double.parseDouble(result[12]);

					if ((easting >= this.quad.getMinEasting()) && (easting <= this.quad.getMaxEasting()) && (northing >= this.quad.getMinNorthing()) && (northing <= this.quad.getMaxNorthing())) {
						String agent = result[0];
						String time = result[1];
//					String dist = result[5];
						String speed = result[6];
						String elevation = result[13];
						String azimuth = result[14];
						//String type = result[7];
//						ExtendedPositionInfo position = new ExtendedPositionInfo(new IdImpl(agent), easting, northing,
//								Double.parseDouble(elevation), Double.parseDouble(azimuth), Double.parseDouble(speed), AgentSnapshotInfo.AgentState.AGENT_MOVING, Integer.parseInt(result[7]), Integer.parseInt(result[15]));
						AgentSnapshotInfo position = new PositionInfo(new IdImpl(agent), easting, northing,
								Double.parseDouble(elevation), Double.parseDouble(azimuth) ) ;
						position.setColorValueBetweenZeroAndOne( Double.parseDouble(speed) ) ;
						position.setAgentState( AgentSnapshotInfo.AgentState.PERSON_DRIVING_CAR ) ;
						position.setType( Integer.parseInt(result[7]) ) ;
						position.setUserDefined( Integer.parseInt(result[15]) );
						addVehicle(Double.parseDouble(time), position);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		finish();
	}

	private void addVehicle(double time, AgentSnapshotInfo position) {

		// Init lastTime with first occurence of time!
		if (this.lastTime == -1) this.lastTime = time;

		if (time != this.lastTime) {

			if (time % 600 == 0 ) {
				System.out.println("Parsing T = " + time + " secs");
				Gbl.printElapsedTime();
				Gbl.startMeasurement();
			}
			// the time changes
				// this is a dumpable timestep
				try {
					dump((int)this.lastTime);
					this.writer.positions.clear();
				} catch (IOException e) {
					e.printStackTrace();
				}
			this.lastTime = time;
		}
// I do not really know which second will be written, as it might be any second AFTER nextTime, when NOTHING has happened on "nextTime", as the if-clause will be executed only then
// still I can collect all vehicles, as to every time change it will get erased...
//		if (time == nextTime) {
			this.writer.positions.add(position);
//		}
	}



	@Override
	public void finish() {
		close();
	}


	public static void main(String[] args) {

//		String netFileName = "../studies/schweiz/2network/ch.xml";
//		String vehFileName = "../runs/run168/run168.it210.T.veh";
//		String netFileName = "../../tmp/studies/ivtch/network.xml";
//		String vehFileName = "../../tmp/studies/ivtch/T.veh";
//		String outFileName = "output/testSWI2.mvi.gz";

		String netFileName = "../../tmp/studies/padang/padang_net.xml";
//		String vehFileName = "./output/colorizedT.veh.txt.gz";
		String vehFileName = "../../tmp/studies/padang/run301.it100.colorized.T.veh.gz";
		String outFileName = "./output/testrun301.mvi";
		int intervall_s = 60;

		Gbl.createConfig(null);

		Scenario scenario = new ScenarioImpl();
		Network net = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);
		QNetwork qnet = new QNetwork(net);

		new OTFTVeh2MVI(qnet, vehFileName, intervall_s, outFileName).convert();
	}

}
