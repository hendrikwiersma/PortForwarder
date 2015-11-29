/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package port.forwarder;
import java.io.*; 
import java.net.*; 
import java.util.logging.*;

public class RequestProcessor implements Runnable {

  private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
  private final Socket connectionIn;
  private final Socket connectionOut;

  public RequestProcessor(Socket connectionIn, Socket connectionOut) {
        this.connectionIn = connectionIn;
        this.connectionOut = connectionOut;
  }

  @Override
  public void run() {
    //Next is the part for the HTTP requests.
    try {
        OutputStream clientOut = new BufferedOutputStream(connectionIn.getOutputStream());
        OutputStream serverOut = new BufferedOutputStream(connectionOut.getOutputStream());
        
        InputStream clientInput = connectionIn.getInputStream();
        byte[] clientData = new byte[1024];
        //Read all the data that the client send to the portforwarder
        clientInput.read(clientData);
        //Then send it to the fileserver. No need to read it into a string.
        serverOut.write(clientData);
        //Flush the stream to finish sending.
        serverOut.flush();
        
        InputStream serverInput = connectionOut.getInputStream();
        byte[] serverData = new byte[1024];
        //Then the fileserver will return a command.
        serverInput.read(serverData);
        //And send it to the client.
        clientOut.write(serverData);
        clientOut.flush();

    } catch (IOException ex) {
        logger.log(Level.WARNING,
                "Error talking to " + connectionIn.getRemoteSocketAddress(), ex);
    } finally {
        try {
            connectionIn.close();
        } catch (IOException ex) {
        }
    }
  }
}