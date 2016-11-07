package edu.cmu.rds749.lab2;

import edu.cmu.rds749.common.AbstractServer;
import org.apache.commons.configuration2.Configuration;

/**
 * Implements the BankAccounts transactions interface
 * Created by utsav on 8/27/16.
 */

public class BankAccountI extends AbstractServer
{
    private final Configuration config;

    private int balance = 0;
    private ProxyControl ctl;

    public BankAccountI(Configuration config) {
        super(config);
        this.config = config;
    }

    @Override
    protected void doStart(ProxyControl ctl) throws Exception {
        this.ctl = ctl;
        System.out.println("SERVER STARTED");
    }

    @Override
    protected void handleBeginReadBalance(int reqid) {
        System.out.println("READING BALANCE, SENDING BALANCE - ID : " + reqid + " BALANCE : " + balance);
        ctl.endReadBalance(reqid, balance);

    }

    @Override
    protected void handleBeginChangeBalance(int reqid, int update) {
        balance = balance + update;
        System.out.println("CHANGING BALANCE, SENDING BALANCE - ID : " + reqid + " BALANCE : " + balance);
        ctl.endChangeBalance(reqid, balance);
    }

    @Override
    protected int handleGetState()
    {
        return balance;
    }

    @Override
    protected int handleSetState(int balance)
    {
        this.balance = balance;
        return balance;
    }
}