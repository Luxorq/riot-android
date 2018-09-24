package im.vector.adapters;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import im.vector.R;
import im.vector.util.KedrCallHistory;
import im.vector.util.VectorUtils;

public class CallsAdapter extends AbsFilterableAdapter<RoomViewHolder> {
    private final List<KedrCallHistory> mCalls;
    private final List<KedrCallHistory> mFilteredCalls;
    private final HomeRoomAdapter.OnSelectRoomListener mListener;

    public CallsAdapter(Context context, HomeRoomAdapter.OnSelectRoomListener listener) {
        super(context);
        mCalls = new ArrayList<>();
        mFilteredCalls = new ArrayList<>();
        mListener = listener;
    }


    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_room_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final @NonNull RoomViewHolder viewHolder, int position) {
        if (position < mFilteredCalls.size()) {
            final KedrCallHistory call = mFilteredCalls.get(position);
            final Room room = mSession.getDataHandler().getRoom(call.getRoomId());
            viewHolder.populateViews(mContext, mSession, room, call);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSelectRoom(room, viewHolder.getAdapterPosition(), (String) v.getTag());
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.onLongClickRoom(v, room, viewHolder.getAdapterPosition());
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFilteredCalls.size();
    }

    @Override
    protected Filter createFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();

                filterCalls(constraint);

                results.values = mFilteredCalls;
                results.count = mFilteredCalls.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                onFilterDone(constraint);
                notifyDataSetChanged();
            }
        };
    }

    private void filterCalls(CharSequence constraint) {
        mFilteredCalls.clear();
        mFilteredCalls.addAll(getFilteredRooms(mContext, mSession, mCalls, constraint));
    }

    private static List<KedrCallHistory> getFilteredRooms(final Context context, final MXSession session,
                                                          final List<KedrCallHistory> callsToFilter, final CharSequence constraint) {
        final String filterPattern = constraint != null ? constraint.toString().trim() : null;
        if (!TextUtils.isEmpty(filterPattern)) {
            List<KedrCallHistory> filteredCall = new ArrayList<>();
            Pattern pattern = Pattern.compile(Pattern.quote(filterPattern), Pattern.CASE_INSENSITIVE);
            for (final KedrCallHistory call : callsToFilter) {
                final String roomName = VectorUtils.getRoomDisplayName(context, session, session.getDataHandler().getRoom(call.getRoomId()));
                if (pattern.matcher(roomName).find()) {
                    filteredCall.add(call);
                }
            }
            return filteredCall;
        } else {
            return callsToFilter;
        }
    }

    public boolean isEmpty() {
        return mCalls.isEmpty();
    }

    public boolean hasNoResult() {
        return mFilteredCalls.isEmpty();
    }

    public int getBadgeCount() {
        return 0;
    }

    @CallSuper
    public void setCalls(final List<KedrCallHistory> calls) {
        if (calls != null) {
            mCalls.clear();
            mCalls.addAll(calls);
            filterCalls(mCurrentFilterPattern);
        }
        notifyDataSetChanged();
    }
}
