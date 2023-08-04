package com.swanky.readro.service;

import android.view.View;
import android.view.animation.Animation;
import com.google.android.material.textfield.TextInputLayout;

public class VisibilityAnimator {

    private TextInputLayout nowReadLayout;
    private TextInputLayout wantLayout;
    private TextInputLayout finishedLayout;
    private TextInputLayout processLayout;
    private TextInputLayout destinationLayout;
    private Animation invisibleAnimation;
    private Animation visibleAnimation;

    public VisibilityAnimator(Animation invisibleAnimation, Animation visibleAnimation, TextInputLayout nowReadLayout, TextInputLayout wantLayout, TextInputLayout finishedLayout) {
        this.nowReadLayout = nowReadLayout;
        this.wantLayout = wantLayout;
        this.finishedLayout = finishedLayout;
        this.invisibleAnimation = invisibleAnimation;
        this.visibleAnimation = visibleAnimation;
    }


    public void startAnimation(int processPosition, int destinationPosition){

        switch (processPosition){
            case 0 :
                //Now Read
                processLayout = nowReadLayout;
                if (destinationPosition == 1){
                    destinationLayout = wantLayout;
                }else {
                    destinationLayout = finishedLayout;
                }
                break;

            case 1:
                //Want
                processLayout = wantLayout;
                if (destinationPosition == 0){
                    destinationLayout = nowReadLayout;
                }else {
                    destinationLayout = finishedLayout;
                }
                break;

            case 2:
                //Finished
                processLayout = finishedLayout;
                if (destinationPosition == 0){
                    destinationLayout = nowReadLayout;
                }else {
                    destinationLayout = wantLayout;
                }
                break;
        }

        invisibleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                processLayout.setVisibility(View.INVISIBLE);
                destinationLayout.setVisibility(View.VISIBLE);
                destinationLayout.startAnimation(visibleAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        processLayout.startAnimation(invisibleAnimation);

    }





}
