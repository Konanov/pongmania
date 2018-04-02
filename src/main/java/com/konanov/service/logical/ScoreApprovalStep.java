package com.konanov.service.logical;

import com.konanov.model.person.Player;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ScoreApprovalStep implements Processor<RatingCalculatingStep.FinalScore, ScoreApprovalStep.Approval> {

    private Subscriber<? super Approval> subscriber;

    @Override
    public void subscribe(Subscriber<? super Approval> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(RatingCalculatingStep.FinalScore finalScore) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }

    class Approval {
        private Player guest;

    }
}
