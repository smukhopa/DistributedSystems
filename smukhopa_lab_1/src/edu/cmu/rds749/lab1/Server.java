package edu.cmu.rds749.lab1;

import edu.cmu.rds749.common.BankAccountStub;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by student on 9/25/16.
 */
public class Server {

    private BankAccountStub bankAccountStub;
    private String hostname;
    private int port;
    private long uuid;
    private long timestamp;
    private int heartbeatInterval = 5000;
    private Timer timer;
    private boolean status;

    public Server(BankAccountStub bankAccountStub, String hostname, int port) {
        this.bankAccountStub = bankAccountStub;
        this.hostname = hostname;
        this.port = port;
        this.uuid = UUID.randomUUID().getMostSignificantBits();
        this.timestamp = -1;
        this.status = true;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return this.status;
    }

    public BankAccountStub getBankAccountStub() {
        return this.bankAccountStub;
    }

    public long getUuid() {
        return this.uuid;
    }

    public void setTimestamp(long timestamp) {

        // This happens when a server sends a heartbeat for the
        // first time
        System.out.println(timestamp);
        if (this.timestamp == -1) {
            this.timestamp = timestamp;

            // Start countdown
            countdown();
            return;
        }

        // If the timestamp coming in is old
        if (timestamp < this.timestamp) {
            return;
        }

        timer.cancel();
        this.timestamp = timestamp;
        countdown();
    }

    public void countdown() {
        timer = new Timer();
        timer.schedule(new ServerTimer(), 2 * heartbeatInterval);
    }

    // Start a timer which counts down to 2X heartbeat
    class ServerTimer extends TimerTask {

        @Override
        public void run() {
            timer.cancel();
            status = false;
        }
    }
}
