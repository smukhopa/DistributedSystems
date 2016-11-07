package edu.cmu.rds749.lab2;

/**
 * Created by student on 10/26/16.
 */
public class UpdateMessage {
    int reqid;
    int update;
    public UpdateMessage(int reqid, int update) {
        this.reqid = reqid;
        this.update = update;
    }
}
