package com.example.async_app;

class NumberPrinter {
    private int number = 1;
    private int n;
    public NumberPrinter(int n){
        this.n = n;
    }
    public synchronized void printOdd(){
        while(number <= n){
            if(number % 2 == 0){
                try{
                    wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }else{
                System.out.println(Thread.currentThread().getName() + " -> " + number);
                number++;
                notify();
            }
        }
    }
    public synchronized void printEven(){
        while(number <= n){
            if(number % 2 == 1){
                try{
                    wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }else{
                System.out.println(Thread.currentThread().getName() + " -> " + number);
                number++;
                notify();
            }
        }
    }
}
public class Main {
    public static void main(String[] args) {
        int n = 10;
        NumberPrinter printer = new NumberPrinter(n);
        Thread oddThread = new Thread(printer::printOdd,"OddThread");
        Thread evenThread = new Thread(printer::printEven,"EvenThread");
        oddThread.start();
        evenThread.start();
    }
}


