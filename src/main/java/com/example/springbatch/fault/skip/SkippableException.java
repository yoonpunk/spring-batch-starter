package com.example.springbatch.fault.skip;

public class SkippableException extends Exception {

    SkippableException(String s) {
        super(s);
    }
}
