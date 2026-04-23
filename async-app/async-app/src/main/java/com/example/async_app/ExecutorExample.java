package com.example.async_app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class MyObject{
    int id;
    MyObject(int id){
        this.id = id;
    }
}

public class ExecutorExample {
    private static String process(MyObject obj){
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        return "Processed " + obj.id + " by " + Thread.currentThread().getName();
    }
    public static void main(String []args){
        List<MyObject> objects = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            objects.add(new MyObject(i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<CompletableFuture<String>> futures = objects.stream().map(obj -> CompletableFuture.supplyAsync(() -> process(obj),executorService)).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<String> result = allFutures.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList())).join();

        result.forEach(System.out::println);

        executorService.shutdown();
    }
}
