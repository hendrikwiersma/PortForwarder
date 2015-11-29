/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package port.forwarder;

import java.net.Inet4Address;

public class Server {
    private Inet4Address ipAddress;
    private int port;
    
    public void setIPAddress(Inet4Address ipAddress){
        this.ipAddress = ipAddress;
    }
    public void setPort(int port){
        this.port = port;
    }
    public Inet4Address getIPAddress(){
        return this.ipAddress;
    }
    public int getPort(){
        return this.port;
    }
}
