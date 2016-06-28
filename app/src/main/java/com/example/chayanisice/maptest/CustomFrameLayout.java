package com.example.chayanisice.maptest;

import android.widget.FrameLayout;
import android.view.GestureDetector;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by chayanisice on 6/14/16.
 */

public class CustomFrameLayout extends FrameLayout {

    private GestureDetector gestureDetector;
    private DragCallback dragListener;
    private boolean isScrolling = false;
    private long previousEvent;
    private float previousX, previousY;

    public CustomFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public interface DragCallback {
        void onDrag(double distance, long time, float prevX, float prevY, float curX, float curY);
        void noDrag();
    }

    public void setOnDragListener(DragCallback listener) {
        this.dragListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            if(isScrolling ) {
                isScrolling  = false;
                dragListener.noDrag();
            }
        }
        return false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            long timeDiff;
            double distance;

            //that's when user starts dragging
            if(dragListener != null) {
                if(isScrolling == false){
                    previousEvent = e1.getEventTime();
                    previousX = e1.getX();
                    previousY = e1.getY();
                }
                isScrolling = true;

                timeDiff = e2.getEventTime()-previousEvent;
                distance = Math.sqrt(Math.pow(distanceX,2)+Math.pow(distanceY,2));
                dragListener.onDrag(distance, timeDiff, previousX, previousY, e2.getX(), e2.getY());

                previousEvent = e2.getEventTime();
                previousX = e2.getX();
                previousY = e2.getY();
            }
            return false;
        }
    }
}
