package com.example.springbatch.fault.skip;

public class NoSkippableException extends Exception {

    NoSkippableException(String s) {
        super(s);
    }
}
