package org.unbiquitous.uos.core.adaptabitilyEngine.events;

import java.util.List;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;


public class DummyEventDriver implements UosEventDriver {
	
	private static DummyEventDriver currentDummyEventDriver ;
	
	private ServiceCall lastServiceCall ;
	
	private int lastServiceCallCount = 0;

	public DummyEventDriver() {
		currentDummyEventDriver = this;
	}
	
	@Override
	public void registerListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		// store service call for test check
		lastServiceCall = serviceCall;
		lastServiceCallCount++;
	}

	@Override
	public void unregisterListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		// store service call for test check
		lastServiceCall = serviceCall;
		lastServiceCallCount++;

	}

	@Override
	public UpDriver getDriver() {
		
		UpDriver driver = new UpDriver("br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.events.DummyEventDriver");
		
		driver.addService("registerListener")
				.addParameter("eventKey", ParameterType.MANDATORY);
		
		driver.addService("unregisterListener")
				.addParameter("eventKey", ParameterType.OPTIONAL);
		
		return driver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {}

	@Override
	public void destroy() {}

	/**
	 * @return the currentDummyEventDriver
	 */
	public static DummyEventDriver getCurrentDummyEventDriver() {
		return currentDummyEventDriver;
	}

	/**
	 * @return the lastServiceCall
	 */
	public ServiceCall getLastServiceCall() {
		return lastServiceCall;
	}

	/**
	 * @return the lastServiceCallCount
	 */
	public int getLastServiceCallCount() {
		return lastServiceCallCount;
	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
