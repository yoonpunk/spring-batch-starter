package com.example.springbatch.domain.jobparameters;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobParameterTest implements ApplicationRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    /**
     * command line에서 jar로 실행 시 아래와 같은 파라미터를 넣으려면 다음과 같은 명령어로 실행한다.
     * java -jar spring-batch-0.0.1-SNAPSHOT.jar 'name=user1' 'seq(long)=2L' 'date(date)=2023/11/6' 'age(double)=16.5'
     * 주의사항: 파라미터 전달 시 따옴표를 사용할 것
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
                .addLong("seq", 2L)
                .addDate("date", new Date())
                .addDouble("age", 16.5)
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
}
