package br.unb.unbiquitous.ubiquitos.uos.connectivityTest;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.json.JSONDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

/*
 * This test only works along with other on thsi package.
 * Such testes are designed to be executed on different machines.
 */
public class TestActiveDiscreteTCP extends TestCase {

	private static Logger logger = Logger.getLogger(TestActiveDiscreteTCP.class);

	protected UOSApplicationContext applicationContext;

	private static final int TIME_BETWEEN_TESTS = 500;
	
	private static final int TIME_TO_LET_BE_FOUND = 25000;
	
	protected static long currentTest = 0;

	private Object lock = Object.class;
	
	private boolean isOnTest = false;
	
	
	@Override
	protected synchronized void setUp() throws Exception {
		
		synchronized (lock) {
			if (isOnTest){
				System.out.println("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			System.out.println("====== Locked ("+lock.hashCode()+") "+isOnTest+"  ======");
			isOnTest = true;
		}
		
		logger.info("\n");
		logger.info("============== Teste : "+currentTest+++" ========================== Begin");
		logger.info("\n");
		
		
		applicationContext = new UOSApplicationContext();
		applicationContext.init("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesTCP");
			
	}
	
	@Override
	protected synchronized void tearDown() throws Exception {
		applicationContext.tearDown();
		logger.info("============== Teste : "+(currentTest-1)+" ========================== End");
		Thread.sleep(TIME_BETWEEN_TESTS);
		synchronized (lock) {
			if (!isOnTest){
				System.out.println("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			System.out.println("====== UnLocked ("+lock.hashCode()+") "+isOnTest+"  ======");
			isOnTest = false;
			lock.notify();
		}
	}
	
	
	public void _testTCPConsumesDiscreteUDP() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testTCPConsumesDiscreteUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the UDP machine");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContext.getGateway().getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContext.getGateway().callService(
				this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplIdUDPDevice",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.error("Not possible to listDrivers from the UDP machine");
		}
	
		logger.info("---------------------- testTCPConsumesDiscreteUDP END ---------------------- ");
	}
	
	
	public void _testTCPConsumesDiscreteBluetooth() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testTCPConsumesDiscreteBluetooth BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the Bluetooth machine");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContext.getGateway().getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContext.getGateway().callService(
				this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplIdBluetoothDevice",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.error("Not possible to listDrivers from the Bluetooth machine");
		}
	
		logger.info("---------------------- testTCPConsumesDiscreteBluetooth END ---------------------- ");
	}
	
}