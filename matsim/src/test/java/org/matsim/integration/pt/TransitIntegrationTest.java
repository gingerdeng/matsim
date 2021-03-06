/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.integration.pt;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.PtConstants;
import org.matsim.testcases.MatsimTestUtils;

public class TransitIntegrationTest {

	@Rule public MatsimTestUtils utils = new MatsimTestUtils();

	@Test(expected = RuntimeException.class)
	public void testPtInteractionParams() {
		Config config = ConfigUtils.createConfig();
		config.controler().setOutputDirectory(utils.getOutputDirectory());
		PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams(PtConstants.TRANSIT_ACTIVITY_TYPE);
		params.setScoringThisActivityAtAll(true);
		params.setTypicalDuration(60.0);
		config.planCalcScore().addActivityParams(params);
		// ---
		config.controler().setLastIteration(0); // in case the exception is _not_ thrown, we don't need 100 iterations to find that out ...
		// ---
		Controler controler = new Controler(config);
		controler.run();
	}

	@Test
	public void test_RunTutorial() {
		Config config = this.utils.loadConfig(IOUtils.newUrl(utils.getScenario("pt-tutorial"), "config.xml"));
		config.planCalcScore().setWriteExperiencedPlans(true);
		config.controler().setLastIteration(0);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		config.controler().setCreateGraphs(false);
		config.plans().setInputFile("population2.xml");
		Controler controler = new Controler(config);
        controler.run();

		MutableScenario s = (MutableScenario) controler.getScenario();
		Assert.assertNotNull(s.getTransitSchedule());
		Assert.assertEquals(4, s.getTransitSchedule().getFacilities().size());
		Assert.assertEquals(1, s.getTransitSchedule().getTransitLines().size());
	}
}
