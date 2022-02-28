package com.my_project.telescopecontrol;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;


public class NamesRecyclerAdapter extends RecyclerView.Adapter<NamesRecyclerAdapter.myVieHolder> implements Filterable {


    private List<star> starobjects;
    private List<star> starobjectsfull;
    private OnNameListener myOnNameListener;
    public List<star> single_result;


    public NamesRecyclerAdapter(ArrayList<star> starobjects, OnNameListener OnNameListener) {
        this.starobjects = starobjects;
        this.myOnNameListener = OnNameListener;
        starobjectsfull = new ArrayList<>(starobjects);
    }

    @NonNull
    @Override

    public myVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item_of_recyclerview, parent, false);
        myVieHolder vieholder = new myVieHolder(view, myOnNameListener);
        return vieholder;

    }

    @Override
    public void onBindViewHolder(@NonNull myVieHolder holder, int position) {
        holder.textView.setText(starobjects.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return starobjects.size();
    }


    class myVieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        OnNameListener onNameListener;

        /**
         * viewholder contains the items every row consists of.
         */
        public myVieHolder(@NonNull View itemView, OnNameListener onNameListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            this.onNameListener = onNameListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            onNameListener.onNameClick(getAdapterPosition());
        }

    }

    public interface OnNameListener {
        void onNameClick(int position);

    }

    @Override
    public Filter getFilter() {
        return starFilter;
    }


    public List<star> getSingle_result() {


        return single_result;
    }

       //filtering logic:
    private final Filter starFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<star> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                //show eveything if nothing is typed in the search field
                filteredList.addAll(starobjectsfull);
            } else {
                //turn everything typed to lowercase and then remove any spaces with trim()
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (star star : starobjectsfull) {
                    // add to filst if the pattern of object is matching with the search typed string
                    if (star.getName_ascii().toLowerCase().contains(filterPattern)) {
                        filteredList.add(star);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            single_result = (List<star>) results.values;
            return results;
        }

        @Override
        //publish the results to the UI thread:
        protected void publishResults(CharSequence constraint, FilterResults results) {
            starobjects.clear();
            starobjects.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


}
