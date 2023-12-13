package com.example.springbatch.fault.skip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SkipConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("skipJob")
//            .start(step1())
//            .start(step2())
            .start(step3())
            .build();
    }

    // SkippableException 3번 발생 시, 중단
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("skipStep")
            .<String, String>chunk(5)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    if (i == 3) {
                        throw new SkippableException("skip");
                    }
                    System.out.println("ItemReader: " + i);
                    return i > 20 ? null : String.valueOf(i);
                }
            })
            .processor(new ItemProcessor<String, String>() {
                int count = 0;
                @Override
                public String process(String item) throws Exception {
                    System.out.println("ItemProcessor: " + item);
                    if (item.equals("6") || item.equals("8")) {
                        throw new SkippableException("Process count=" + count);
                    }

                    return String.valueOf(Integer.valueOf(item) * -1);
                }
            })
            .writer(new ItemWriter<String>() {
                int count = 0;
                @Override
                public void write(List<? extends String> itemList) throws Exception {
                    for (String item : itemList) {
                        if (item.equals("-14")) {
                            throw new SkippableException("Write fail count=" + count);
                        } else {
                            System.out.println("ItemWriter: " + item);
                        }
                    }
                }
            })
            .faultTolerant()
//            .skip(SkippableException.class)
//            .skipLimit(1)
            .skipPolicy(limitCheckingItemSkipPolicy())
            .build();
    }

    // NoSkippableException 발생 시, skip 하지 않음
    @Bean
    public Step step2() {
        return stepBuilderFactory.get("skipStep2")
            .<String, String>chunk(5)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    if (i == 3) {
                        throw new NoSkippableException("do not skip");
                    }
                    System.out.println("ItemReader: " + i);
                    return i > 20 ? null : String.valueOf(i);
                }
            })
            .writer(new ItemWriter<String>() {
                int count = 0;
                @Override
                public void write(List<? extends String> itemList) throws Exception {
                    for (String item : itemList) {
                        if (item.equals("-14")) {
                            throw new SkippableException("Write fail count=" + count);
                        } else {
                            System.out.println("ItemWriter: " + item);
                        }
                    }
                }
            })
            .faultTolerant()
            .skip(SkippableException.class)
            .noSkip(NoSkippableException.class)
            .skipLimit(1)
            .build();
    }

    // processor가 없이 writer에서 skip예외 발생하면, writer에서 청크단위로 다시 실행한다.
    // (processor가 존재할 때, processor에서 하나씩 받아와 write에서 처리하는 방식과 다름)
    @Bean
    public Step step3() {
        return stepBuilderFactory.get("skipStep3")
            .<String, String>chunk(5)
            .reader(new ItemReader<String>() {
                int i = 0;
                @Override
                public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    i++;
                    if (i == 3) {
                        throw new SkippableException("skip");
                    }
                    System.out.println("ItemReader: " + i);
                    return i > 20 ? null : String.valueOf(i);
                }
            })
            .writer(new ItemWriter<String>() {
                int count = 0;
                @Override
                public void write(List<? extends String> itemList) throws Exception {
                    for (String item : itemList) {
                        if (item.equals("14")) {
                            throw new SkippableException("Write fail count=" + count);
                        } else {
                            System.out.println("ItemWriter: " + itemList);
                        }
                    }
                }
            })
            .faultTolerant()
            .skip(SkippableException.class)
            .noSkip(NoSkippableException.class)
            .skipLimit(4)
            .build();
    }

    @Bean
    public SkipPolicy limitCheckingItemSkipPolicy() {

        Map exceptionClassMap = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptionClassMap.put(SkippableException.class, true);

        return new LimitCheckingItemSkipPolicy(3, exceptionClassMap);
    }

}
