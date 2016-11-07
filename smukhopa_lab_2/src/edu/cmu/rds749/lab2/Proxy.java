package edu.cmu.rds749.lab2;

import edu.cmu.rds749.common.AbstractProxy;
import edu.cmu.rds749.common.BankAccountStub;
import org.apache.commons.configuration2.Configuration;

import java.util.*;


/**
 * Created by jiaqi on 8/28/16.
 *
 * Implements the Proxy.
 */
public class Proxy extends AbstractProxy
{
    private ServerList list;
    private static HashMap<Integer, Integer> checkBalanceMap;
    private static HashMap<Integer, Integer> updateBalanceMap;
    private static boolean isServerBeingAdded;
    private static boolean isMessageBeingProcessed;
    private static Queue<Integer> readMessageQueue;
    private static Queue<UpdateMessage> updateMessageQueue;
    private static Queue<Server> incomingServerQueue;

    public Proxy(Configuration config)
    {
        super(config);
        System.out.println("SERVER PROXY STARTED");
        list = new ServerList();
        checkBalanceMap = new HashMap<>();
        updateBalanceMap = new HashMap<>();
        isServerBeingAdded = false;
        isMessageBeingProcessed = false;
        readMessageQueue = new LinkedList<>();
        updateMessageQueue = new LinkedList<>();
        incomingServerQueue = new LinkedList<>();
    }

    @Override
    protected void serverRegistered(long id, BankAccountStub stub)
    {
        addServer(id, stub);
    }

    private synchronized void addServer(long id, BankAccountStub stub) {
        //isServerBeingAdded = true;
        incomingServerQueue.add(new Server(id, stub));
        while (!isMessageBeingProcessed && !incomingServerQueue.isEmpty()) {
            isServerBeingAdded = true;
            Server qServer = incomingServerQueue.poll();
            long qid = qServer.id;
            BankAccountStub qstub = qServer.stub;
            if (list.getNumberOfServers() == 0) {
                System.out.println("REGISTERING SERVER " + qid);
                list.addToList(qstub, qid);
                System.out.println("NUMBER OF SERVERS : " + list.getNumberOfServers());
            } else {
                while (list.getAnyServer() != null) {
                    try {
                        Server server = list.getAnyServer();
                        int balance = server.stub.getState();
                        qstub.setState(balance);
                        System.out.println("REGISTERING SERVER " + qid);
                        list.addToList(qstub, qid);
                        System.out.println("NUMBER OF SERVERS : " + list.getNumberOfServers());
                        break;
                    } catch (BankAccountStub.NoConnectionException e) {

                    }
                }
            }
            isServerBeingAdded = false;
        }
        //isServerBeingAdded = false;
    }

    @Override
    protected void beginReadBalance(int reqid)
    {
        // Put every message in a queue
        readMessageQueue.add(reqid);
        isMessageBeingProcessed = true;
        while(!isServerBeingAdded && !readMessageQueue.isEmpty()) {
            // At this point, no server is being added and there are messages in the message queue
            //isMessageBeingProcessed = true;
            System.out.println("IN PROXY BEGIN READ BALANCE - ID : " + reqid);
            list.readBalance(readMessageQueue.poll());
            //isMessageBeingProcessed = false;
        }
        isMessageBeingProcessed = false;
    }

    @Override
    protected void beginChangeBalance(int reqid, int update)
    {
        // put every message in a queue
        updateMessageQueue.add(new UpdateMessage(reqid, update));
        while (!isServerBeingAdded && !updateMessageQueue.isEmpty()) {
            isMessageBeingProcessed = true;
            System.out.println("IN PROXY BEGIN CHANGE BALANCE - ID : " + reqid + " UPDATE : " + update);
            UpdateMessage message = updateMessageQueue.poll();
            list.updateBalance(message.reqid, message.update);
            isMessageBeingProcessed = false;
        }
    }

    @Override
    protected void endReadBalance(long serverid, int reqid, int balance)
    {
        System.out.println("IN PROXY END READ BALANCE - ID : " + reqid + " BALANCE : " + balance);
        sendToClientReadBalance(serverid, reqid, balance);
    }

    @Override
    protected void endChangeBalance(long serverid, int reqid, int balance)
    {
        System.out.println("IN PROXY END CHANGE BALANCE - ID : " + reqid + " BALANCE : " + balance);
        sendToClientUpdateBalance(serverid, reqid, balance);
    }

    @Override
    protected void serversFailed(List<Long> failedServers)
    {
        super.serversFailed(failedServers);
        list.removeFromList(failedServers);
    }

    private synchronized void sendToClientReadBalance(long serverid, int reqid, int amt) {
        if (!checkBalanceMap.containsKey(reqid)) {
            checkBalanceMap.put(reqid, amt);
            clientProxy.endReadBalance(reqid, amt);
        }
    }

    private synchronized void sendToClientUpdateBalance(long serverid, int reqid, int amt) {
        if (!updateBalanceMap.containsKey(reqid)) {
            updateBalanceMap.put(reqid, amt);
            clientProxy.endChangeBalance(reqid, amt);
        }
    }

    private class ServerList {

        private HashMap<Long, Server> serverHashMap;

        private ServerList() {
            serverHashMap = new HashMap<>();
        }

        private int getNumberOfServers() {
            return serverHashMap.size();
        }

        private synchronized Server getAnyServer() {
            Iterator<Long> iter = serverHashMap.keySet().iterator();
            return serverHashMap.get(iter.next());
        }

        private void addToList(BankAccountStub stub, long id) {
            Server server = new Server(id, stub);
            serverHashMap.put(id, server);
        }

        private void removeFromList(List<Long> failedServers) {
            for (int i = 0; i < failedServers.size(); i++) {
                if (serverHashMap.containsKey(failedServers.get(i))) {
                    serverHashMap.remove(failedServers.get(i));
                    System.out.println("DE-REGISTERING SERVER " + failedServers.get(i));
                    System.out.println("NUMBER OF SERVERS : " + serverHashMap.size());
                }
            }
        }

        private synchronized void readBalance(int reqid) {
            ReadBalanceThread thread = new ReadBalanceThread(reqid);
            thread.start();
        }

        private synchronized void updateBalance(int reqid, int amt) {
            UpdateBalanceThread thread = new UpdateBalanceThread(reqid, amt);
            thread.start();
        }

        private class ReadBalanceThread extends Thread {
            private int reqid;
            private ReadBalanceThread(int reqid) {
                this.reqid = reqid;
            }

            @Override
            public void run() {
                if (serverHashMap.isEmpty()) {
                    clientProxy.RequestUnsuccessfulException(reqid);
                } else {
                    Iterator<Long> iter = serverHashMap.keySet().iterator();
                    while (iter.hasNext()) {
                        Server server = serverHashMap.get(iter.next());
                        BankAccountStub stub = server.stub;
                        try {
                            stub.beginReadBalance(reqid);
                        } catch (BankAccountStub.NoConnectionException e) {
                            e.printStackTrace();
                            clientProxy.RequestUnsuccessfulException(reqid);
                        }
                    }
                }
            }
        }

        private class UpdateBalanceThread extends Thread {
            private int reqid;
            private int amt;
            private UpdateBalanceThread(int reqid, int amt) {
                this.reqid = reqid;
                this.amt = amt;
            }

            @Override
            public void run() {
                if (serverHashMap.isEmpty()) {
                    clientProxy.RequestUnsuccessfulException(reqid);
                } else {
                    Iterator<Long> iter = serverHashMap.keySet().iterator();
                    while (iter.hasNext()) {
                        Server server = serverHashMap.get(iter.next());
                        BankAccountStub stub = server.stub;
                        try {
                            stub.beginChangeBalance(reqid, amt);
                        } catch (BankAccountStub.NoConnectionException e) {
                            e.printStackTrace();
                            clientProxy.RequestUnsuccessfulException(reqid);
                        }
                    }
                }
            }
        }
    }
}
