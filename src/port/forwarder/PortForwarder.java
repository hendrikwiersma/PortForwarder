/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package port.forwarder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortForwarder implements Runnable {

    private static int listenPort;
    private static final List<Server> servers = new ArrayList<>();
    private int currentServerIndex = 0;
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    public static void main(String[] args) {
        
        System.out.println("On which port should the portforwarder listen?");
        boolean correctPort = false;
        while(correctPort == false){
            try {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                String inputString = bufferRead.readLine();
                String[] command = inputString.split(" ");
                // Get the ipaddress from the command.
                try {
                    listenPort = Integer.parseInt(command[0]);
                    correctPort = true;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // If the number of paramters is too small it will notify the user.
                    System.out.println("Usage: portnumber");
                    correctPort = false;
                }catch (NumberFormatException e) {
                    System.err.println("Invalid number");
                    correctPort = false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //Now we have the port we can start listening.
        Thread mainThread = new Thread(new PortForwarder());
        mainThread.start();
        
        //This part is for the user to enter commands after connecting.
        System.out.println("Add a server by using the command: addserver ipaddress port");
        //Keep listenening for new commands.
        while(true){
            try {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                String inputString = bufferRead.readLine();
                String[] command = inputString.split(" ");
                // Get the ipaddress from the command.
                try {
                    if (command[0].equals("addserver")) {
                        //To add a new server we create a Server object
                        Server newServer = new Server();
                        //Set the address of the new server
                        newServer.setIPAddress((Inet4Address) Inet4Address.getByName(command[1]));
                        //And the port it uses.
                        newServer.setPort(Integer.parseInt(command[2]));
                        System.out.println("Added the server on address " + newServer.getIPAddress() + " and port " + newServer.getPort());
                        servers.add(newServer);
                    } else if (command[0].equals("list")) {
                        for(int i = 0; i < servers.size(); i++){
                            System.out.println("Server " + i + " : " + servers.get(i).getIPAddress() + " port " + servers.get(i).getPort());
                        }
                    }else {
                        System.out.println("Unknown command: " + command[0]);
                        return;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // If the number of paramters is too small it will notify the user.
                    System.out.println("Usage: addserver ipaddress port");
                    return;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        
        try (ServerSocket server = new ServerSocket(listenPort)) {
          while (true) {
            try {
              Socket request = server.accept();
              Socket serversocket = new Socket(servers.get(currentServerIndex).getIPAddress(), servers.get(currentServerIndex).getPort());
              //Start a new thread for every incoming request
              Runnable r = new port.forwarder.RequestProcessor(request, serversocket);
              r.run();
              //Loop through every server and go to the first one if the end is reached.
              currentServerIndex++;
              if(currentServerIndex >= servers.size()){
                  currentServerIndex = 0;
              }
            }catch (IOException ex) {
              logger.log(Level.WARNING, "Error accepting connection", ex);
            }
          }
        } catch (IOException ex) {
            Logger.getLogger(PortForwarder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendHeader(Writer out, String responseCode,
            String contentType, int length)
            throws IOException {
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}
