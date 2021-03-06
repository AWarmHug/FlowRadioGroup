package com.warm.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：warm
 * 时间：2017-11-23 16:14
 * 描述：
 */

public class FlowLayout extends ViewGroup {

    /**
     * 横向间隙
     */
    private int mSpaceH;

    /**
     * 纵向间隙
     */
    private int mSpaceV;

    /**
     * 设置默认多少列
     */
    private int mHorizontalSize;

    private int mRealHorizontalSize;

    public static final int NINE = -1;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowLayout, 0, 0);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int item = array.getIndex(i);
            if (item == R.styleable.FlowLayout_horizontalSize) {
                mHorizontalSize = array.getInt(item, 0);
                setHorizontalSize(mHorizontalSize);

            } else if (item == R.styleable.FlowLayout_spaceH) {
                mSpaceH = array.getDimensionPixelSize(item, 0);
            } else {
                mSpaceV = array.getDimensionPixelSize(item, 0);
            }
        }
        array.recycle();
    }

    public int getHorizontalSize() {
        return mHorizontalSize;
    }

    public void setHorizontalSize(int horizontalSize) {
        this.mHorizontalSize = horizontalSize;
        if (!isNine()) {
            setRealHorizontalSize(mHorizontalSize);
        }
    }

    private boolean isNine(){
        return mHorizontalSize == NINE;
    }

    private void setRealHorizontalSize(int realHorizontalSize) {
        this.mRealHorizontalSize = realHorizontalSize;
    }

    public int getSpaceH() {
        return mSpaceH;
    }

    public void setSpaceH(int spaceH) {
        this.mSpaceH = spaceH;
    }

    public int getSpaceV() {
        return mSpaceV;
    }

    public void setSpaceV(int spaceV) {
        this.mSpaceV = spaceV;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int x = getPaddingLeft() + getPaddingRight();
        int y = getPaddingTop() + getPaddingBottom();
        int itemMaxHeight = 0;
        if (isNine()) {
            switch (getChildCount()) {
                case 1:
                    setRealHorizontalSize(1);
                    break;
                case 2:
                case 4:
                    setRealHorizontalSize(2);
                    break;
                default:
                    setRealHorizontalSize(3);
                    break;
            }
        }

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                ViewGroup.LayoutParams lp = child.getLayoutParams();

                int cw, ch, cwMargin = 0, chMargin = 0;
                if (lp instanceof MarginLayoutParams) {
                    MarginLayoutParams childLP = (MarginLayoutParams) lp;
                    /**
                     * 如果传入widthUsed,当使用wrap_content，会自动适配为最小宽度，会使一行最边缘的控件宽度变为 parent#Width-widthUsed;
                     * 这个widthUsed和heightUsed,当前横 纵已经使用了长度，一般用于设置权重之后，计算剩余可以摆放的位置，只用传0就可以，
                     * 可以看{@link android.widget.LinearLayout#measureHorizontal（1018行，1117行）和measureChildBeforeLayout}
                     */
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    cwMargin = childLP.leftMargin + childLP.rightMargin;
                    chMargin = childLP.topMargin + childLP.bottomMargin;
                } else {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                }

                cw = child.getMeasuredWidth() + cwMargin;
                ch = child.getMeasuredHeight() + chMargin;


                if (itemMaxHeight < ch) {
                    if (itemMaxHeight == 0) {
                        //计算每添加一个子空间时的宽度，如果当前计算的宽度大于了父控件的宽度，这就需要换行
                        //每一行的高度以当前行最大的item为准
                        y += ch;
                    } else {
                        y += ch - itemMaxHeight;
                    }
                    itemMaxHeight = ch;
                }
                x += cw;
                if (x > widthSize) {
                    itemMaxHeight = ch;
                    y += itemMaxHeight;
                    y += mSpaceV;
                    x = getPaddingLeft() + getPaddingRight();
                    x += cw;
                }
                x += mSpaceH;
            }
        }

        //实际计算得出的宽高
        int measureWidth = widthSize;
        int measureHeight = heightMode == MeasureSpec.EXACTLY ? heightSize : y;
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        int itemWidth = lp.width;
        int itemHeight = lp.height;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        if (mRealHorizontalSize != 0) {
            itemWidth = (MeasureSpec.getSize(parentWidthMeasureSpec) - (getPaddingLeft() + getPaddingRight()) - (mRealHorizontalSize - 1) * mSpaceH) / mRealHorizontalSize - (lp.leftMargin + lp.rightMargin);
            if (isNine()) {
                switch (getChildCount()) {
                    case 1:
                        itemHeight = itemWidth / 2;
                        break;

                    default:
                        itemHeight = itemWidth;
                        break;
                }
            }
        }

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, itemWidth);
        childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                        + heightUsed, itemHeight);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        final ViewGroup.LayoutParams lp = child.getLayoutParams();

        int itemWidth = lp.width;
        int itemHeight = lp.height;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        if (mRealHorizontalSize != 0) {
            itemWidth = (MeasureSpec.getSize(parentWidthMeasureSpec) - (getPaddingLeft() + getPaddingRight()) - (mRealHorizontalSize - 1) * mSpaceH) / mHorizontalSize;
            if (isNine()) {
                switch (getChildCount()) {
                    case 1:
                        itemHeight = itemWidth / 2;
                        break;

                    default:
                        itemHeight = itemWidth;
                        break;
                }
            }
        }
        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight(), itemWidth);

        childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), itemHeight);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measureWidth = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int itemMaxHeight = 0;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                //判断当前行最后一个left+child.getMeasuredWidth()，是否大于父控件的宽度，如果大于换行

                ViewGroup.LayoutParams lp = child.getLayoutParams();

                int leftMargin = 0, topMargin = 0, rightMargin = 0, bottomMargin = 0;

                if (lp instanceof MarginLayoutParams) {
                    MarginLayoutParams childLP = (MarginLayoutParams) lp;
                    leftMargin = childLP.leftMargin;
                    topMargin = childLP.topMargin;
                    rightMargin = childLP.rightMargin;
                    bottomMargin = childLP.bottomMargin;
                }
                int cl, ct, cr, cb;

                cl = left + leftMargin;
                ct = top + topMargin;
                cr = cl + child.getMeasuredWidth();
                cb = ct + child.getMeasuredHeight();

                int ch = child.getMeasuredHeight() + topMargin + bottomMargin;

                if (itemMaxHeight < ch) {
                    itemMaxHeight = ch;
                }

                if (cr + rightMargin > measureWidth - getPaddingRight()) {
                    //行数++,+纵向间隙，left恢复为原来值
                    left = getPaddingLeft();

                    top += itemMaxHeight;
                    top += mSpaceV;

                    itemMaxHeight = ch;

                    cl = left + leftMargin;
                    ct = top + topMargin;
                    cr = cl + child.getMeasuredWidth();
                    cb = ct + child.getMeasuredHeight();
                }

                child.layout(cl, ct, cr, cb);

                left = cr + rightMargin;
                left += mSpaceH;
            }
        }
    }


    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}
