/* *********************************************************************** *
 * project: org.matsim.*
 * RunLegModeDistanceDistribution.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.benjamin.scenarios.munich.analysis.modular.legModeDistanceDistribution;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

import playground.benjamin.scenarios.munich.analysis.filter.PersonFilter;
import playground.benjamin.scenarios.munich.analysis.filter.UserGroup;
import playground.vsp.analysis.DefaultAnalysis;

/**
 * @author benjamin
 *
 */
public class RunLegModeDistanceDistribution {
	private final static Logger logger = Logger.getLogger(RunLegModeDistanceDistribution.class);
	
	String iterationOutputDir;
	String baseFolder;
	String configFile;
	String iteration;

	UserGroup userGroup = null;

	DefaultAnalysis analysis;
	
	public RunLegModeDistanceDistribution(String baseFolder, String configFile, String iteration, UserGroup userGroup){
		this.baseFolder = baseFolder;
		this.configFile = configFile;
		this.iteration = iteration;
		this.userGroup = userGroup;
	}
	
	void run() {
		Scenario scenario = loadScenario();
		this.analysis = new DefaultAnalysis(scenario, baseFolder, this.iterationOutputDir, null, null);
		this.analysis.init(null);
		this.analysis.preProcess();
		this.analysis.run();
		this.analysis.postProcess();
		this.analysis.writeResults();
	}

	private Scenario loadScenario() {
		String networkFile;
		String popFile;

		this.iterationOutputDir = this.baseFolder  + "ITERS/it." + this.iteration + "/";
		logger.info("Setting iteration output directory to " + this.iterationOutputDir);

		Config config = ConfigUtils.loadConfig(configFile);
		String runId = config.controler().getRunId();
		if(runId == null){
			networkFile = this.baseFolder + "output_network.xml.gz";
			popFile = this.iterationOutputDir + this.iteration + ".plans.xml.gz";
		} else {
			networkFile = this.baseFolder + runId + ".output_network.xml.gz";
			popFile = this.iterationOutputDir + runId + "." + this.iteration + ".plans.xml.gz";
		}
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario).readFile(networkFile);
		logger.info("Setting network to " + networkFile);
		new MatsimPopulationReader(scenario).readFile(popFile);
		logger.info("Setting population to " + popFile);
		
		Scenario relevantScenario;
		
		if(this.userGroup == null){
			logger.warn("Values are calculated for the whole population ...");
			relevantScenario = scenario;
		} else {
			logger.warn("Values are calculated for user group " + this.userGroup + " ...");
			PersonFilter personFilter = new PersonFilter();
			Population pop = scenario.getPopulation();
			Population relevantPop = personFilter.getPopulation(pop, userGroup);

			ScenarioImpl sc = (ScenarioImpl) ScenarioUtils.createScenario((ConfigUtils.createConfig()));
			sc.setPopulation(relevantPop);
			relevantScenario = sc;
		}
		return relevantScenario;
	}

	protected DefaultAnalysis getAnalysis() {
		return this.analysis;
	}
}
