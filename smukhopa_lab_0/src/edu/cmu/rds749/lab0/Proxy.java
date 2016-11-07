package edu.cmu.rds749.lab0;

import Ice.Current;
import edu.cmu.rds749.common.AbstractProxy;
import edu.cmu.rds749.common.ServerMain;
import edu.cmu.rds749.common.rds749.BankAccountPrx;

/**
 * Created by jiaqi on 8/28/16.
 *
 * Implements the Proxy.
 */
public class Proxy extends AbstractProxy
{
    public Proxy(String [] hostname, String [] port)
    {
        super(hostname, port);
    }

    public int readBalance(Current __current)
    {
        System.out.println("(In Proxy)");
        // student answer - Please change next line
        int num = actualBankAccounts[0].readBalance();
        return num;
    }

    public int changeBalance(int update, Current __current)
    {
        System.out.println("(In Proxy)");
        // student answer - Please change next line:
        int num = actualBankAccounts[0].changeBalance(update);
        return num;
    }

}
