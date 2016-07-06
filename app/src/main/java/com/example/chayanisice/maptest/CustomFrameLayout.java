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
    private boolean isFling = false;

    private final int SWIPE_MIN_DISTANCE = 120;
    private final int SWIPE_THRESHOLD_VELOCITY = 200;

    public CustomFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public interface DragCallback {
        //void onDrag(double distance, long time, float prevX, float prevY, float curX, float curY);
        void onDrag();
        void noDrag();
        void onFling();
    }

    public void setOnDragListener(DragCallback listener) {
        this.dragListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            if(isScrolling && !isFling) {
                isScrolling  = false;
                dragListener.noDrag();
            }
            if(isFling){
                isFling = false;
                //dragListener.noDrag();
            }
        }
        return false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(dragListener != null) {
                isScrolling = true;
                dragListener.onDrag();
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            if(dragListener != null) {
                isFling = true;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // Right to left, your code here
                    dragListener.onFling();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) >  SWIPE_THRESHOLD_VELOCITY) {
                    // Left to right, your code here
                    dragListener.onFling();
                    return true;
                }
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    // Bottom to top, your code here
                    dragListener.onFling();
                    return true;
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    // Top to bottom, your code here
                    dragListener.onFling();
                    return true;
                }
            }
            return false;
        }
    }
}
