package com.example.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
//@ComponentScan(basePackages = {"com.example.springbatch.domain.job"}) // 3장 job 실습
//@ComponentScan(basePackages = {"com.example.springbatch.domain.jobinstance"}) // 3장 jobinstance 실습
//@ComponentScan(basePackages = {"com.example.springbatch.domain.jobparameters"}) // 3장 jobParameters 실습
//@ComponentScan(basePackages = {"com.example.springbatch.domain.jobexecution"}) // 3장 jobExecution 실습
//@ComponentScan(basePackages = {"com.example.springbatch.domain.step"}) // 4장 step 실습
@ComponentScan(basePackages = {"com.example.springbatch.domain.stepexecution"}) // 4장 step 실습
//@ComponentScan(basePackages = {"com.example.springbatch.chunk"}) // 7장 chunk 실습
public class SpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }

}
