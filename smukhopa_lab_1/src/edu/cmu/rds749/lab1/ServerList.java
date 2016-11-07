package edu.cmu.rds749.lab1;

import rds749.NoServersAvailable;

import java.util.ArrayList;

/**
 * Created by student on 9/26/16.
 */

// Contains list of all active servers
public class ServerList {

    // List of active servers
    private ArrayList<Server> serverList;

    // The Active server
    private Server activeServer;

    public ServerList() {
        serverList = new ArrayList<>();
    }

    // Method to add to the server list
    public void addServer(Server server) {
        serverList.add(server);
    }

    public Server getActiveServer() {
        return activeServer;
    }

    public void setActiveServer(Server server) {
        this.activeServer = server;
    }

    public Server getNewActiveServer() throws NoServersAvailable {


            for(int i = 0; i < serverList.size(); i++) {
                Server server = serverList.get(i);
                if (server.getStatus() == true) {
                    this.activeServer = server;
                    return this.activeServer;
                }
            }

            // If it reaches here, then there are no servers available
            throw new NoServersAvailable();
            //this.activeServer = serverList.get(0);
            //return serverList.get(0);


    }

    // Returns the number of active servers
    public int getServerListSize() {

        int size = 0;
        for (int i = 0; i < serverList.size(); i++) {
            Server server = serverList.get(i);
            if (server.getStatus() == true) {
                size++;
            }
        }
        return size;
        //return serverList.size();
    }

    public void heartbeat(long id, long timestamp) {

        // First step is to find the server sending the heartbeat
        Server server = findServer(id);

        // Set the timestamp
        server.setTimestamp(timestamp);
    }

    // Given an ID, find the server
    public Server findServer(long id) {
        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).getUuid() == id) {

                Server server = serverList.get(i);

                if (server.getStatus() == false) {
                    server.setStatus(true);
                }
                return server;

                //return serverList.get(i);
            }
        }

        // Execution shouldn't come here
        return null;
    }

    // delete the server
    public void deleteServer(Server server) {
        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).equals(server)) {
                //serverList.remove(i);
                server.setStatus(false);
                break;
            }
        }
    }



}
