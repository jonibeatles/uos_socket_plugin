package br.unb.unbiquitous.ubiquitos.network.ethernet.channelManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.cache.CacheController;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ChannelManager;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetTCPClientConnection;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetTCPServerConnection;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager.EthernetConnectionManager.EthernetConnectionType;
import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;

public class EthernetTCPChannelManager implements ChannelManager{ 
	
	private static final Logger logger = Logger.getLogger(EthernetTCPChannelManager.class);
	
	/*********************************
	 * ATTRIBUTES
	 *********************************/
	
	private List<NetworkDevice> freePassiveDevices;
	
	private Map<String, EthernetTCPServerConnection> startedServers;
	
	private int defaultPort;
	private int controlPort;
	
	/**
     * Controller responsible for the active connections cache. 
     */
    private CacheController cacheController;
	
	/*********************************
	 * CONSTRUCTORS
	 * @param cacheController 
	 *********************************/
	
	public EthernetTCPChannelManager(int defaultPort, int controlPort ,String portRange, CacheController cacheController){
		
		this.defaultPort = defaultPort;
		this.controlPort = controlPort;
		
		this.cacheController = cacheController;
		
		this.startedServers = new HashMap<String, EthernetTCPServerConnection>();
		
		freePassiveDevices = new ArrayList<NetworkDevice>();
		String[] limitPorts = portRange.split("-");
		int inferiorPort = Integer.parseInt(limitPorts[0]);
		int superiorPort = Integer.parseInt(limitPorts[1]);
		for(int port = inferiorPort; port <= superiorPort; port++){
			freePassiveDevices.add(new EthernetDevice("0.0.0.0",port,EthernetConnectionType.TCP));
		}
	}
	
	/********************************
	 * PUBLIC METHODS
	 ********************************/
	
	public ClientConnection openActiveConnection(String networkDeviceName) throws NetworkException, IOException{
		String[] address = networkDeviceName.split(":");
		
		String host ;
		int port ;
		if (address.length == 1){
			port = controlPort;
		}else if(address.length == 2){
			port = Integer.parseInt(address[1]);
		}else{
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
    	host = address[0];
    	
    	ClientConnection cached = cacheController.getConnection(networkDeviceName);
		if (cached != null){
			logger.info("\nEthernetTCPChannelManager: openActiveConnection: Returning cached connection for host '"+host+"'\n\n"); 
			return cached;
		}
    	
    	EthernetTCPClientConnection etcc ;
		try {
			etcc = new EthernetTCPClientConnection(host, port, cacheController);
		} catch (Exception e) {
			etcc = new EthernetTCPClientConnection(host, defaultPort, cacheController);
		}
		return etcc;
	}
	
	
	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException{
		String[] address = networkDeviceName.split(":");
		
		if(address.length != 2){
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
		EthernetTCPServerConnection server = startedServers.get(networkDeviceName);
		if(server == null){
			String host = address[0];
	    	int port = Integer.parseInt(address[1]);
			// Passive (Stream) connections shouldn't be cached
	    	server = new EthernetTCPServerConnection(new EthernetDevice(host, port, EthernetConnectionType.TCP), null);
	    	startedServers.put(networkDeviceName, server);
		}
		
		return server.accept();
	}
	
	
	public NetworkDevice getAvailableNetworkDevice(){
		NetworkDevice networkDevice = freePassiveDevices.remove(0);
		freePassiveDevices.add(networkDevice);
		return networkDevice;
	}
	
	
	public void tearDown() throws NetworkException, IOException {
		for(EthernetTCPServerConnection server : startedServers.values()){
			server.closeConnection();
		}
	}
}