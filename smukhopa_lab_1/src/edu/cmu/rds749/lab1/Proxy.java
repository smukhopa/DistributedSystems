package edu.cmu.rds749.lab1;

import edu.cmu.rds749.common.AbstractProxy;
import edu.cmu.rds749.common.BankAccountStub;
import org.apache.commons.configuration2.Configuration;
import rds749.NoServersAvailable;

import java.util.ArrayList;

/**
 * Implements the Proxy.
 */
public class Proxy extends AbstractProxy
{

    ServerList serverList;

    public Proxy(Configuration config)
    {
        super(config);
        serverList = new ServerList();
    }

    public int readBalance() throws NoServersAvailable
    {
        System.out.println("(In Proxy)");

        Server server = null;
        BankAccountStub bankAccountStub = null;
        try {
            server = serverList.getActiveServer();
            bankAccountStub = server.getBankAccountStub();
        } catch (NullPointerException e) {
            throw new NoServersAvailable();
        }

        //BankAccountStub bankAccountStub = server.getBankAccountStub();

        int amount = -1;

        while (server != null) {
            try {
                amount = bankAccountStub.readBalance();
                break;
            } catch(BankAccountStub.NoConnectionException e) {
                // This means that the server failed
                serverList.deleteServer(server);

                // find new active server
                try {
                    server = serverList.getNewActiveServer();
                    bankAccountStub = server.getBankAccountStub();
                } catch(NoServersAvailable n) {
                    throw n;
                }
            }
        }

        return amount;
    }

    public int changeBalance(int update) throws NoServersAvailable
    {
        System.out.println("(In Proxy)");

        Server server = null;
        BankAccountStub bankAccountStub = null;

        try {
            server = serverList.getActiveServer();
            bankAccountStub = server.getBankAccountStub();
        } catch (NullPointerException e) {
            throw new NoServersAvailable();
        }

        //Server server = serverList.getActiveServer();
        //BankAccountStub bankAccountStub = server.getBankAccountStub();
        int amount = -1;

        while (server != null) {
            try {
                amount = bankAccountStub.changeBalance(update);
                break;
            } catch(BankAccountStub.NoConnectionException e) {
                // This means that the server failed
                serverList.deleteServer(server);

                // find new active server
                try {
                    server = serverList.getNewActiveServer();
                    bankAccountStub = server.getBankAccountStub();
                } catch(NullPointerException n) {
                    throw new NoServersAvailable();
                }
            }
        }

        return amount;

    }

    public long register(String hostname, int port)
    {
        // establish conenction to server
        BankAccountStub bankAccountStub = connectToServer(hostname, port);

        // Create a new server
        Server server = new Server(bankAccountStub, hostname, port);

        // Add the server to ServerList
        serverList.addServer(server);

        // We also need to decide the active server
        if (serverList.getServerListSize() == 1) {
            serverList.setActiveServer(server);
        }
        // else keep the old server as the active server

        // send back id of server
        return server.getUuid();
    }

    public void heartbeat(long ID, long serverTimestamp)
    {
        serverList.heartbeat(ID, serverTimestamp);
    }
}
