package com.example.async_app;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
class Employee{
    String name;
    int id;
    int salary;
    String dept;
}
public class Demo {
    public static void main(String []args){
        List<Employee> employeeList = new ArrayList<>();
        Map<String,Double> map = employeeList.stream().collect(Collectors.groupingBy(Employee::getDept,Collectors.averagingInt(Employee::getSalary)));
        Map<String,String> map2 = employeeList.stream().collect(Collectors.groupingBy(Employee::getDept,Collectors.collectingAndThen(Collectors.averagingInt(Employee::getSalary),avg->String.format("%.2f",avg))));
        List<Integer> array = Arrays.asList(1,2,3,4,5,6,7,8);
         Map<String, Integer> summm = employeeList.stream().collect(Collectors.groupingBy(Employee::getDept,Collectors.summingInt(Employee::getSalary)));
        int sum = array.stream().mapToInt(Integer::intValue).sum();
        int sum2 = array.stream().reduce(0, (a,b)->a+b);
        System.out.println(sum2);
        System.out.println(summm);
    }
}
