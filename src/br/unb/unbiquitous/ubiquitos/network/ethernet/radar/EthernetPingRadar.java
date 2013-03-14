package br.unb.unbiquitous.ubiquitos.network.ethernet.radar;


import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import br.unb.cic.ethutil.EthUtil;
import br.unb.cic.ethutil.EthUtilClientListener;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManager;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetConnectionManager.EthernetConnectionType;
import br.unb.unbiquitous.ubiquitos.network.radar.Radar;
import br.unb.unbiquitous.ubiquitos.network.radar.RadarListener;


/**
 * This class implements a ethernet Radar for the smart-space usaing PING discovery mode
 * It implements 3 interfaces:
 *   Runnable - For running on a independent thread
 *   EthUtilClientListener - for recieving the Ethernet discovery events
 *   Radar - For stating and stoping the Radar. Default for all UbiquitOS radars
 *   
 * It has a listener that is invoked when a host is found or left the LAN. 
 *   
 *
 * @author Passarinho
 */
public class EthernetPingRadar implements EthUtilClientListener, Radar {
    
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
    /** Object for logging registration. */
	private static final Logger logger = Logger.getLogger(EthernetPingRadar.class);
    
	
	/** A Convenient way to Access Ethernet */
	private EthUtil ethUtil = null;
	
    /** This is the list of devices present in the smart-space. */
    private Set<String> localHostRepository = null;
    
    /** A RadarListener object interested in receiving UbiquitOS Radar notifications, 
     * like "a new device has entered the smart-space" and "device X has left the smart-space". */
    private RadarListener radarListener;
    
    /** Indicates whether the radar is running or not. */
    private boolean started = false;
    
    /**
     * The connection manager responsible for handling the information of connections.
     */
    private ConnectionManager connectionManager;
    
    /* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
    /**
     * Constructor
     * @param radarControlCenter 
     * @param listener Some object interested in receive Radar notifications
     *  about devices entrance and exit.
     */
    public EthernetPingRadar(RadarListener radarListener ) {
    	// add the listener
    	this.radarListener = radarListener;
    	ethUtil = new EthUtil(this);
    }
    
    /* *****************************
	 *   	PUBLIC METHODS - Runnable
	 * *****************************/
    
    /**
     * Runnable implementation
     *  - called my runnable.start() method to start the thread
     */
    public void run() {
        try {
            // Start the device discovery. According to the defined discovery Mode
            // log it.
        	logger.debug("[EthernetPingRadar] Starting Radar... PING Discovery");
        	// start PING discovery.
            ethUtil.discoverDevices(EthUtil.DISCOVER_DEVICES_USING_PING);
        } catch (Exception ex) {
        	logger.error("[EthernetPingRadar] Could Not realize the host discovery...");
        }
    }
    
    /* *****************************
	 *   	PUBLIC METHODS - Radar
	 * *****************************/
    
    /**
     * Start the space scan process.
     */
    public void startRadar() {
    	// sets the flag
        started = true; 
    }
    
    /**
     * Stop the space scan process.
     */
    public void stopRadar() {
    	// sets the flag
    	started = false;
    }
    
    
    /* *****************************
	 *   	PUBLIC METHODS - EthUtilClientListener
	 * *****************************/
    
    
    /**
     * Part of the EthUtilClientListener interface implementation.
     * Method invoked when a new device is discovered by the PING host discovery
     * 
     * @param host 
     */
    public void deviceDiscovered(String host) {

    	logger.debug("[EthernetPingRadar] A device was found [" + host + "].");
        //Notify listeners.
    	logger.info("[EthernetPingRadar] [" + host + "] is in the smart-space.");
    	// Creates a EthernetDevice Object
    	//FIXME: PingRadar : This asumption only works with the TCP-Plugin and don't consider the PortParameter.
    	EthernetDevice device = new EthernetDevice(host, 14984, EthernetConnectionType.TCP);
    	// Notifies the listener
    	radarListener.deviceEntered(device);
    }
    
    
    /**
     * Part of the EthUtilClientListener interface implementation.
     * Method invoked when the discovery method is finished.
     * 
     * @param host 
     */
    public void deviceDiscoveryFinished(Vector<String> recentilyDiscoveredHosts) {
        
    	logger.info("[EthernetPingRadar] Ethernet Discovery Finished. Found Devices: "+ recentilyDiscoveredHosts);

    	// If localHostRepository equals null, First time Discovery is called), populates it with the found hosts
    	if (localHostRepository == null){
    		localHostRepository = new HashSet<String>(recentilyDiscoveredHosts);
    	}else{
    		//else, checks if some host exited the smart-space
    		for (String existingHost : localHostRepository) {
    			// If localHost wasn't found on the network... It left the smart-space
				if (!recentilyDiscoveredHosts.contains(existingHost)){
					// remove from localRepository.
					localHostRepository.remove(existingHost); 
					// notifies the Radar Control Center
					logger.info("[EthernetPingRadar] Host ["+existingHost+"] has left the smart-space.");
			    	//FIXME: PingRadar : This asumption only works with the TCP-Plugin and don't consider the PortParameter.
					radarListener.deviceLeft(new EthernetDevice(existingHost,  14984, EthernetConnectionType.TCP)); 
				}
			}
    		// add all found hosts to the local repository
    		localHostRepository.addAll(recentilyDiscoveredHosts);
    	}
    	
        //If stopRadar method was called, a new device discovery will not be started
        if (started) {
            //Start a new host discovery process
        	logger.debug("[EthernetPingRadar] Starting a new discovery.");
        	ethUtil.discoverDevices(EthUtil.DISCOVER_DEVICES_USING_PING);
        }
    }

    @Override
    public void setConnectionManager(ConnectionManager connectionManager) {
    	this.connectionManager = connectionManager;
    }
}