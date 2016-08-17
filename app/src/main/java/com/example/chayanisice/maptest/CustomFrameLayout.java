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

    public CustomFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public interface DragCallback {
        void onDrag(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
        void noDrag();
        void onFling(double speedX, double speedY);
    }

    public void setOnDragListener(DragCallback listener) {
        this.dragListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getPointerCount() == 1){
            gestureDetector.onTouchEvent(ev);
            if(ev.getAction() == MotionEvent.ACTION_UP) {
                if(isScrolling && !isFling) {
                    isScrolling  = false;
                    dragListener.noDrag();
                }
                if(isFling){
                    isFling = false;
                    return true; //consume fling events
                }
            }
        }
        return false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(dragListener != null) {
                isScrolling = true;
                dragListener.onDrag(e1, e2, distanceX, distanceY);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            if(dragListener != null) {
                isFling = true;
                dragListener.onFling(velocityX, velocityY);
            }
            return false;
        }
    }
}
