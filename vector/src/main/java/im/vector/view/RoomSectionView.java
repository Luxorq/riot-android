package im.vector.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class RoomSectionView extends HomeSectionView{
    public RoomSectionView(Context context) {
        super(context);
    }

    public RoomSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoomSectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setup() {
        super.setup();
        mHeader.setVisibility(View.GONE);
    }
}
