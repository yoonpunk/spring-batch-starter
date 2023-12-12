package com.example.springbatch.fault.tolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FaultTolerantConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("faultTolerantJob")
            .start(step())
            .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("faultTolerantStep")
            .<String, String>chunk(3)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    if (i == 1) {
                        System.out.println("This exception is skipped");
                        throw new IllegalArgumentException("This exception is skipped");
                    }
                    return i > 3 ? null : "item" + i;
                }
            })
            .processor(new ItemProcessor<String, String>() {
                RepeatTemplate repeatTemplate = new RepeatTemplate();
                @Override
                public String process(String item) throws Exception {
                    System.out.println("This exception is retied");
                    throw new IllegalStateException("This exception is retied");
                }
            })
            .writer(items -> System.out.println(items))
            .faultTolerant()
            .skip(IllegalArgumentException.class)
            .skipLimit(2)
            .retry(IllegalStateException.class)
            .retryLimit(3)
            .build();
    }
}
