package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Customer{

    private String name;
    private Lock accountLock;
    private Condition enoughFundsCondition;
    private List<AccountADT> myAccounts;

    public Customer(String name){
        setName(name);
        accountLock = new ReentrantLock();
        enoughFundsCondition = accountLock.newCondition();
        myAccounts = new ArrayList<>(3);
    }

    public Boolean withdraw(AccountADT account, double amount){
        boolean stillWaiting = true;
        accountLock.lock();
        try {
            while(!account.transState(amount)){
                if(!stillWaiting){
                    Thread.currentThread().interrupt();
                }
                stillWaiting = enoughFundsCondition.await(1, TimeUnit.SECONDS);
            }
            System.out.println("Withdraw Thread: Balance is " + account.getBalance() + " at the start of the thread");
            account.withdraw(amount);
            System.out.println("Withdraw Thread: Attempting to withdraw "+ amount);
            System.out.println("Withdraw Thread: Balance is " + account.getBalance() + " at the end of the thread");
        }catch(InterruptedException e){
            System.out.println("Can't Wait Any Longer!");
            return false;
        }finally{
            enoughFundsCondition.signalAll();
            accountLock.unlock();
        }
        return true;
    }

    public Boolean deposit(AccountADT account, double amount){
        accountLock.lock();
        try {
            System.out.println("Deposit Thread: Balance at start: "+ account.getBalance());
            account.deposit(amount);
            System.out.println("Deposit Thread: Amount deposited: "+amount);
            System.out.println("Deposit Thread: Balance at end: "+ account.getBalance());
        }finally{
            enoughFundsCondition.signalAll();
            accountLock.unlock();
        }
        return true;
    }

    public Boolean transfer(AccountADT source, AccountADT target, double amount){
        accountLock.lock();
        try {
            if(!withdraw(source,amount)){
                System.out.println("Insufficient funds!");
                return false;
            }
            deposit(target,amount);
        }finally{
            enoughFundsCondition.signalAll();
            accountLock.unlock();
        }
        return true;
    }

    public void openAccount(AccountADT account){
        myAccounts.add(account);
    }

    public AccountADT getAccount(int accountID) {
        AccountADT account = null;

        return account;
    }

    public double viewBalance(AccountADT account){
        System.out.println(account.getBalance());
        return account.getBalance();
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
