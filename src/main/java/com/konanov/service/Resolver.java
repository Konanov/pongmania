package com.konanov.service;

import org.reactivestreams.Processor;

public interface Resolver<T, R> extends Processor<T, R> {
}
