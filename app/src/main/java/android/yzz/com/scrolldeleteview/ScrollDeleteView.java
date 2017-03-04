package android.yzz.com.scrolldeleteview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by yzz on 2017/3/4 0004.
 * 此布局分为两部分，1.content；2.附加View
 */
public class ScrollDeleteView extends ViewGroup {

    private int MineDistance;
    private boolean isIntercept = false;
    private float mTouchX = 0;
    private int MaxScrollX = 0;
    private Scroller mScroller;
    public static final int HIDE = 0;
    public static final int EXPAEND = 1;
    private int mState = HIDE;
    private boolean isMove = false;
    private boolean isclick = false;
    private OnAppendClickListener mOnAppendClickListener;

    public ScrollDeleteView(Context context) {
        super(context);
    }

    public ScrollDeleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollDeleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        MineDistance = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext());
        if (getChildCount() > 1) {
            for (int i = 1; i < getChildCount(); i++) {
                View v = getChildAt(i);
                final int position = i;
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hide();
                        if (mOnAppendClickListener != null)
                            mOnAppendClickListener.click(v, position);
                    }
                });
            }
        }
    }


    public void setOnAppendClickListener(OnAppendClickListener mOnAppendClickListener) {
        this.mOnAppendClickListener = mOnAppendClickListener;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //这里制定如下规则，第一个child作为content，后面的child作为附加视图
        int count = getChildCount();
        if (count == 0) return;
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int wModel = MeasureSpec.getMode(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int hModel = MeasureSpec.getMode(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //width
        if (wModel != MeasureSpec.EXACTLY)
            w = measureWidth();
        if (hModel != MeasureSpec.EXACTLY)
            h = measureHeight(count);
        setMeasuredDimension(w, h);
    }

    private int measureWidth() {
        int w = 0;
        View child = getChildAt(0);
        MarginLayoutParams lp;
        if (child.getLayoutParams() instanceof MarginLayoutParams) {
            lp = (MarginLayoutParams) child.getLayoutParams();
        } else {
            lp = new MarginLayoutParams(child.getLayoutParams());
        }
        //加上child的宽度和左右的Margin
        w += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

        //还要加上容器的左右的Padding值
        return w + getPaddingLeft() + getPaddingRight();
    }

    private int measureHeight(int count) {
        int h = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp;

            if (child.getLayoutParams() instanceof MarginLayoutParams) {
                lp = (MarginLayoutParams) child.getLayoutParams();
            } else {
                lp = new MarginLayoutParams(child.getLayoutParams());
            }
            //要加上top和bottom的距离
            h = Math.max(h, child.getHeight() + lp.topMargin + lp.bottomMargin);
        }
        //加上top和bottom的padding的距离
        return h + getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //这里制定如下规则，第一个child作为content，后面的child作为附加视图
        int count = getChildCount();
        if (count == 0) return;
        int left;
        int right;
        int top;
        int bottom;
        int currentW = getMeasuredWidth();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp;
            if (child.getLayoutParams() instanceof MarginLayoutParams) {
                lp = (MarginLayoutParams) child.getLayoutParams();
            } else {
                lp = new MarginLayoutParams(child.getLayoutParams());
            }

            if (i == 0)
                left = getPaddingLeft() + lp.leftMargin;
            else left = currentW + lp.leftMargin;

            top = getPaddingTop() + lp.topMargin;
            right = left + child.getMeasuredWidth();
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
            currentW = right + lp.rightMargin;
        }
        //获得可滑动的最大边界
        MaxScrollX = currentW - getMeasuredWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isIntercept = false;
                mTouchX = ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = mTouchX - ev.getRawX();
                switch (mState) {
                    case HIDE:
                        if (distance >= MineDistance && distance <= MaxScrollX) isIntercept = true;
                        break;
                    case EXPAEND:
                        if (distance <= -MineDistance && distance >= -MaxScrollX)
                            isIntercept = true;
                        break;
                    default:
                        isIntercept = false;
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;
                break;
        }
        return isIntercept;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //这里改变背景
                if (mState == HIDE)
                    setBackgroundColor(Color.BLUE);
                isclick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int distance = (int) (mTouchX - event.getRawX());
                if (Math.abs(distance) < MineDistance) {
                    isMove = false;
                } else {
                    isMove = true;
                    setBackgroundColor(Color.WHITE);
                }

                if (mState == HIDE && distance <= MaxScrollX && distance >= MineDistance)
                    scrollTo(distance, 0);
                if (mState == EXPAEND && distance >= -MaxScrollX && distance <= -MineDistance)
                    scrollTo(MaxScrollX + distance, 0);
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundColor(Color.WHITE);
                if (isclick && !isMove) {
                    if (mOnAppendClickListener != null)
                        mOnAppendClickListener.clickBg();
                }
                if (getScrollX() >= MaxScrollX) {
                    mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);
                    invalidate();
                    mState = HIDE;
                    break;
                }
                if (getScrollX() >= MaxScrollX / 2) {
                    mScroller.startScroll(getScrollX(), 0, MaxScrollX - getScrollX(), 0, 500);
                    invalidate();
                    mState = EXPAEND;
                } else {
                    mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);
                    invalidate();
                    mState = HIDE;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                setBackgroundColor(Color.WHITE);
                isclick = false;
                //这里是事件丢失
                if (getScrollX() >= MaxScrollX / 2) {
                    mScroller.startScroll(getScrollX(), 0, MaxScrollX - getScrollX(), 0, 500);
                    invalidate();
                    mState = EXPAEND;
                } else {
                    mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);
                    invalidate();
                    mState = HIDE;
                }
                break;
        }
        return true;
    }

    public interface OnAppendClickListener {
        void click(View v, int position);

        void clickBg();
    }

    public void hide() {
        scrollTo(0, 0);
        invalidate();
        mState = HIDE;
    }
}
