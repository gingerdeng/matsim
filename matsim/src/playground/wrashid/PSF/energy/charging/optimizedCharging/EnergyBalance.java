package playground.wrashid.PSF.energy.charging.optimizedCharging;

import java.util.LinkedList;
import java.util.PriorityQueue;

import org.matsim.api.basic.v01.Id;

import playground.wrashid.PSF.energy.consumption.EnergyConsumption;
import playground.wrashid.PSF.energy.consumption.EnergyConsumptionInfo;
import playground.wrashid.PSF.energy.consumption.LinkEnergyConsumptionLog;
import playground.wrashid.PSF.parking.ParkLog;
import playground.wrashid.PSF.parking.ParkingInfo;
import playground.wrashid.PSF.parking.ParkingTimes;
import playground.wrashid.PSF.energy.charging.ChargeLog;
import playground.wrashid.PSF.energy.charging.ChargingTimes;
import playground.wrashid.PSF.energy.charging.EnergyChargingInfo;

public class EnergyBalance {

	// for each parking log element we have the energy consumption, preceding it
	// stored under the same index value in the lists.
	LinkedList<ParkLog> parkingTimes = new LinkedList<ParkLog>();
	LinkedList<Double> energyConsumption = new LinkedList<Double>(); // in
	// [J]
	LinkedList<Double> maxChargableEnergy = new LinkedList<Double>(); // in

	// [J]
	
	ChargingTimes chargingTimes;
//	private double minEnergyToCharge;
	private double batteryCapacity;
	

	public EnergyBalance(ParkingTimes parkTimes, EnergyConsumption energyConsumption, 
			double batteryCapacity, ChargingTimes chargingTimes) {
		
		this.chargingTimes=chargingTimes;
		//this.minEnergyToCharge=minEnergyToCharge;
		this.batteryCapacity=batteryCapacity;
		
		// prepare parking times
		parkingTimes = (LinkedList<ParkLog>) parkTimes.getParkingTimes().clone();
		// add the last parking event of the day to the queue
		parkingTimes.add(new ParkLog(parkTimes.getCarLastTimeParkedFacilityId(), parkTimes.getLastParkingArrivalTime(),
				parkTimes.getFirstParkingDepartTime()));

		int maxIndex = parkingTimes.size();

		double sumEnergyConsumption;
		LinkedList<LinkEnergyConsumptionLog> energyConsumptionLog = energyConsumption.getLinkEnergyConsumption();
		LinkEnergyConsumptionLog tempEnergyConsumptionLog = null;
		/*
		 * - we compare the starting parking time, because if we would use the
		 * endParkingTime, then we would run into a problem. with the last
		 * parking of the day, because its end time is shorter than of all
		 * parkings
		 */

		for (int i = 0; i < maxIndex; i++) {
			sumEnergyConsumption = 0;
			while (energyConsumptionLog.peek()!=null && energyConsumptionLog.peek().getEnterTime() < parkingTimes.get(i).getStartParkingTime()) {
				tempEnergyConsumptionLog = energyConsumptionLog.poll();
				sumEnergyConsumption += tempEnergyConsumptionLog.getEnergyConsumption();
			}

			this.energyConsumption.add(sumEnergyConsumption);
		}

		/*
		 * update the maxChargableEnergy(i) means, how much energy can be
		 * charged at maximum at parking with index i (because of the battery
		 * contstraint.
		 * 
		 */

		// initialize the first element
		maxChargableEnergy.add(this.energyConsumption.get(0));

		for (int i = 1; i < maxIndex; i++) {
			maxChargableEnergy.add(maxChargableEnergy.get(i - 1) + this.energyConsumption.get(i));
		}
	}

	/*
	 * This method adds the parking prices of the specified parking (index) to the priority list.
	 */
	private PriorityQueue<FacilityChargingPrice> getChargingPrice() {
		PriorityQueue<FacilityChargingPrice> chargingPrice = new PriorityQueue<FacilityChargingPrice>();
		int maxIndex = parkingTimes.size() - 1;

		double timeOfFirstParkingStart = parkingTimes.get(0).getStartParkingTime();
		// FacilityChargingPrice tempPrice;

		// find out, if we were driving at 0:00 in the night or the car was
		// parked
		if (parkingTimes.get(maxIndex).getStartParkingTime() > parkingTimes.get(maxIndex).getEndParkingTime()) {
			// we did have the car parked in the night and need to handle that
			// specially
			maxIndex = maxIndex - 1;
		}

		// the last parking needs to be handled specially
		int offSet = 10000;
		int offSetOfEarlyNightHours = 100000000;
		for (int i = 0; i <= maxIndex; i++) {
			int minTimeSlotNumber = Math.round(i * offSet + (float) parkingTimes.get(i).getStartParkingTime() / 900);
			int maxTimeSlotNumber = Math.round(i * offSet + (float) parkingTimes.get(i).getEndParkingTime() / 900);

			addChargingPriceToPriorityQueue(chargingPrice, minTimeSlotNumber, maxTimeSlotNumber, i);
		}

		// if we need to handle the last (night parking)
		if (maxIndex < parkingTimes.size() - 1) {

			maxIndex = parkingTimes.size() - 1;

			// create time/price slots for morning parking part after 0:00
			int minTimeSlotNumber = Math.round(offSetOfEarlyNightHours);
			int maxTimeSlotNumber = Math.round(offSetOfEarlyNightHours + (float) parkingTimes.get(maxIndex).getEndParkingTime()
					/ 900);

			addChargingPriceToPriorityQueue(chargingPrice, minTimeSlotNumber, maxTimeSlotNumber, maxIndex);

			if (parkingTimes.get(maxIndex).getStartParkingTime() < 86400) {
				// only handle the first part of the last parking, if the day is
				// less than 24 hours long

				minTimeSlotNumber = Math.round(maxIndex * offSet + (float) parkingTimes.get(maxIndex).getStartParkingTime() / 900);
				maxTimeSlotNumber = Math.round(maxIndex * offSet + (float) 86399 / 900); 
				// this is just one second before mid night to get the right end slot
				
				addChargingPriceToPriorityQueue(chargingPrice, minTimeSlotNumber, maxTimeSlotNumber, maxIndex);
			}
		}

		return chargingPrice;
	}

	private void addChargingPriceToPriorityQueue(PriorityQueue<FacilityChargingPrice> chargingPrice, int minTimeSlotNumber,
			int maxTimeSlotNumber, int parkingIndex) {
		double tempPrice;
		for (int j = 0; j < maxTimeSlotNumber - minTimeSlotNumber; j++) {
			double time= Math.floor(parkingTimes.get(parkingIndex).getStartParkingTime()/900)*900 + j * 900;
			tempPrice = EnergyChargingInfo.getEnergyPrice(time,
					parkingTimes.get(parkingIndex).getFacilityId());

			FacilityChargingPrice tempFacilityChPrice = new FacilityChargingPrice(tempPrice, minTimeSlotNumber + j,parkingIndex,time, parkingTimes.get(parkingIndex).getFacilityId(),parkingTimes.get(parkingIndex).getStartParkingTime(), parkingTimes.get(parkingIndex).getEndParkingTime());
			chargingPrice.add(tempFacilityChPrice);
		}
	}

	// assuming, there is enough electricity in the car for driving the whole day
	// TODO: for more general case
	// TODO: need to take min Energy into consideration!!!
	public ChargingTimes getChargingTimes() {
		
		// this should only be called once (return immediatly if already populated)
		
		if (chargingTimes.getChargingTimes().size()>0){
			return chargingTimes;
		}
		
		PriorityQueue<FacilityChargingPrice> chargingPrice=getChargingPrice();
		
		// TODO: this needs to come from input parameter...
		double minEnergyLevelToCharge=batteryCapacity;
		
		// at home the car must have reached 'minEnergyLevelToCharge'
		while (maxChargableEnergy.get(maxChargableEnergy.size()-1)>0){
			FacilityChargingPrice bestEnergyPrice=chargingPrice.poll();
			
			if (bestEnergyPrice==null){
				System.out.println();
			}
			
			int parkingIndex=bestEnergyPrice.getEnergyBalanceParkingIndex();
			Id facilityId=parkingTimes.get(parkingIndex).getFacilityId();
			
			
			
			double maximumEnergyThatNeedsToBeCharged=maxChargableEnergy.get(parkingIndex);
			
			// skip the charging slot, if no charging at the current parking is needed
			// TODO: this can be made more efficient later (perhaps)
			if (maximumEnergyThatNeedsToBeCharged==0){
				continue;
			}
			
			double energyCharged=bestEnergyPrice.getEnergyCharge(maximumEnergyThatNeedsToBeCharged);
			
			// set energyCharged to maximumEnergyThatNeedsToBeCharged, if they are very close, to counter 
			// rounding errors.
			int precision=100000;
			if (Math.abs(maximumEnergyThatNeedsToBeCharged*precision-energyCharged*precision)<1){
				energyCharged=maximumEnergyThatNeedsToBeCharged;
			}
			
			ChargeLog chargeLog=bestEnergyPrice.getChargeLog(maximumEnergyThatNeedsToBeCharged);
			
			chargingTimes.addChargeLog(chargeLog);
			
			updateMaxChargableEnergy(parkingIndex,energyCharged);
			
		}
		
		
		return chargingTimes;
	}
	
	private void updateMaxChargableEnergy(int indexOfUsedParkingToCharge, double amountOfEnergy){
		for (int i=indexOfUsedParkingToCharge;i<maxChargableEnergy.size();i++){
			double oldValue=maxChargableEnergy.get(i);
			maxChargableEnergy.remove(i);
			maxChargableEnergy.add(i, oldValue-amountOfEnergy);
		}
	}

}
