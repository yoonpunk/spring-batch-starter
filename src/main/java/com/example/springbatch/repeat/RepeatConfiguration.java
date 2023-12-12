package com.example.springbatch.repeat;

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
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RepeatConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("repeatJob")
            .start(repeatPolicyStep())
            .next(repeatExceptionStep())
            .build();
    }

    @Bean
    public Step repeatPolicyStep() {
        return stepBuilderFactory.get("repeatPolicyStep")
            .<String, String>chunk(3)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    return i > 3 ? null : "item" + i;
                }
            })
            .processor(new ItemProcessor<String, String>() {
                RepeatTemplate repeatTemplate = new RepeatTemplate();
                @Override
                public String process(String item) throws Exception {
//                    // 반복 횟수 제한
//                    repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(3));
//                    // 총 수행시간 제한
//                    repeatTemplate.setCompletionPolicy(new TimeoutTerminationPolicy(3000));

                    // 복합 정책 제한
                    CompositeCompletionPolicy compositeCompletionPolicy = new CompositeCompletionPolicy();
                    CompletionPolicy[] completionPolicyList = new CompletionPolicy[] {
                        new SimpleCompletionPolicy(3), new TimeoutTerminationPolicy(3000)
                    };
                    compositeCompletionPolicy.setPolicies(completionPolicyList);
                    repeatTemplate.setCompletionPolicy(compositeCompletionPolicy);

                    repeatTemplate.iterate(new RepeatCallback() {
                        @Override
                        public RepeatStatus doInIteration(RepeatContext repeatContext) throws Exception {
                            System.out.println("repeatTemplate is testing.");
                            return RepeatStatus.CONTINUABLE;
                        }
                    });

                    return item;
                }
            })
            .writer(items -> System.out.println(items))
            .build();
    }

    @Bean
    public Step repeatExceptionStep() {
        return stepBuilderFactory.get("repeatExceptionStep")
            .<String, String>chunk(3)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    return i > 3 ? null : "item" + i;
                }
            })
            .processor(new ItemProcessor<String, String>() {
                RepeatTemplate repeatTemplate = new RepeatTemplate();
                @Override
                public String process(String item) throws Exception {

                    repeatTemplate.setExceptionHandler(simpleLimitExceptionHandler());
                    repeatTemplate.iterate(new RepeatCallback() {
                        @Override
                        public RepeatStatus doInIteration(RepeatContext repeatContext) throws Exception {
                            System.out.println("repeatTemplate is testing.");
                            throw new RuntimeException("runtimeException");
                        }
                    });

                    return item;
                }
            })
            .writer(items -> System.out.println(items))
            .build();
    }

    @Bean
    public ExceptionHandler simpleLimitExceptionHandler() {
        return new SimpleLimitExceptionHandler(3);
    }

}
