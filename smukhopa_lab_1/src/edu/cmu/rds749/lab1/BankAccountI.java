package edu.cmu.rds749.lab1;

import edu.cmu.rds749.common.AbstractServer;
import org.apache.commons.configuration2.Configuration;

import java.awt.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Implements the BankAccounts transactions interface
 */

public class BankAccountI extends AbstractServer
{
    private final Configuration config;

    private int balance = 0;
    private long serverId = -1;
    private ProxyControl ctl;
    Timer timer;

    public BankAccountI(Configuration config) {
        super(config);
        this.config = config;
        timer = new Timer();
    }

    protected void doStart(ProxyControl ctl) throws Exception {

        this.ctl = ctl;

        // First thing is to register
        serverId = ctl.register(config.getString("serverHost"), config.getInt("serverPort"));

        // Get the interval
        int interval = config.getInt("heartbeatIntervalMillis");

        // Now start sending heartbeats
        timer.schedule(new Heartbeat(),
                        0, // Initial start delay
                        interval); // subsequent rate


    }

    protected int handleReadBalance() {

        System.out.println("read balance server");
        return this.balance;
    }

    protected int handleChangeBalance(int update) {

        System.out.println("change balance server");
        this.balance += update;
        return this.balance;
    }

    // Used for sending heartbeats every few seconds
    class Heartbeat extends TimerTask {
        @Override
        public void run() {
            long unixTime = System.currentTimeMillis();
            try {
                ctl.heartbeat(serverId, unixTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
