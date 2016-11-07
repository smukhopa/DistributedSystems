package edu.cmu.rds749.lab2;

import edu.cmu.rds749.common.BankAccountStub;

/**
 * Created by student on 10/23/16.
 */
public class Server {

    protected long id;
    protected BankAccountStub stub;

    public Server(long id, BankAccountStub stub) {
        this.id = id;
        this.stub = stub;
    }
}
