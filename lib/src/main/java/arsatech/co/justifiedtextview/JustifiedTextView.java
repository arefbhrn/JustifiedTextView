package arsatech.co.justifiedtextview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;

public class JustifiedTextView extends android.support.v7.widget.AppCompatTextView {

    private Paint paint = new Paint();
    private boolean wrapEnabled;
    private Align _align = Align.RIGHT;
    private Bitmap cache = null;
    private boolean cacheEnabled = false;

    public JustifiedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPadding(10, 10, 10, 10);
        wrapEnabled = true;
    }

    public JustifiedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(10, 10, 10, 10);
        wrapEnabled = true;
    }

    public JustifiedTextView(Context context) {
        super(context);
        setPadding(10, 10, 10, 10);
        wrapEnabled = true;
    }

    @Override
    public void setDrawingCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void setText(CharSequence st, boolean wrap) {
        wrapEnabled = wrap;
        super.setText(st);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        //at least 10px padding bottom
        super.setPadding(left + 10, top + 10, right + 10, bottom + 10);
    }

    public void setTextAlign(Align align) {
        _align = align;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // If wrap is disabled then,
        // request original onDraw
        if (!wrapEnabled) {
            super.onDraw(canvas);
            return;
        }
        // Active canas needs to be set
        // based on cacheEnabled
        Canvas activeCanvas = null;
        // Set the active canvas based on
        // whether cache is enabled
        if (cacheEnabled) {
            if (cache != null) {
                // Draw to the OS provided canvas
                // if the cache is not empty
                canvas.drawBitmap(cache, 0, 0, paint);
                return;
            } else {
                // Create a bitmap and set the activeCanvas
                // to the one derived from the bitmap
                cache = Bitmap.createBitmap(getWidth(), getHeight(),
                        Config.ARGB_4444);
                activeCanvas = new Canvas(cache);
            }
        } else {
            // Active canvas is the OS
            // provided canvas
            activeCanvas = canvas;
        }
        // Pull widget properties
        paint.setColor(getCurrentTextColor());
        paint.setTypeface(getTypeface());
        paint.setTextSize(getTextSize());
        paint.setTextAlign(_align);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //minus out the paddings pixel
        float dirtyRegionWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int maxLines = Integer.MAX_VALUE;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            maxLines = getMaxLines();
        }
        int lines = 1;
        String[] blocks = fromHtml(getText().toString()).toString().split("((?<=\n)|(?=\n))");
        float horizontalFontOffset = 0;
        float verticalOffset = horizontalFontOffset = getLineHeight() - 0.5f;
        float spaceOffset = paint.measureText(" ");
        for (int i = 0; i < blocks.length && lines <= maxLines; i++) {
            String block = blocks[i];
            float horizontalOffset = 0;
            if (block.length() == 0) {
                continue;
            } else if (block.equals("\n")) {
                verticalOffset += horizontalFontOffset;
                continue;
            }
            block = block.trim();
            if (block.length() == 0) {
                continue;
            }
            Object[] wrappedObj = Utils.createWrappedLine(block, paint, spaceOffset, dirtyRegionWidth);
            String wrappedLine = ((String) wrappedObj[0]);
            float wrappedEdgeSpace = (Float) wrappedObj[1];
            String[] lineAsWords = wrappedLine.split(" ");
            float strecthOffset = wrappedEdgeSpace != Float.MIN_VALUE ? wrappedEdgeSpace / (lineAsWords.length - 1) : 0;
            for (int j = 0; j < lineAsWords.length; j++) {
                String word = lineAsWords[j];
                if (lines == maxLines && j == lineAsWords.length - 1) {
                    activeCanvas.drawText("...", horizontalOffset, verticalOffset, paint);
                } else if (j == 0) {
                    //if it is the first word of the line, text will be drawn starting from right edge of textview
                    if (_align == Align.RIGHT) {
                        activeCanvas.drawText(word, getWidth() - (getPaddingRight()), verticalOffset, paint);
                        // add in the paddings to the horizontalOffset
                        horizontalOffset += getWidth() - (getPaddingRight());
                    } else {
                        activeCanvas.drawText(word, getPaddingLeft(), verticalOffset, paint);
                        horizontalOffset += getPaddingLeft();
                    }
                } else {
                    activeCanvas.drawText(word, horizontalOffset, verticalOffset, paint);
                }
                if (_align == Align.RIGHT)
                    horizontalOffset -= paint.measureText(word) + spaceOffset + strecthOffset;
                else
                    horizontalOffset += paint.measureText(word) + spaceOffset + strecthOffset;
            }
            lines++;
            if (blocks[i].length() > 0) {
                blocks[i] = blocks[i].substring(wrappedLine.length());
                verticalOffset += blocks[i].length() > 0 ? horizontalFontOffset : 0;
                i--;
            }
        }
        if (cacheEnabled) {
            // Draw the cache onto the OS provided
            // canvas.
            canvas.drawBitmap(cache, 0, 0, paint);
        }
    }

    private String fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result.toString();
    }

}
