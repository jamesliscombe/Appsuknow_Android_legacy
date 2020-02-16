package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ChoicesParentRecyclerViewAdapter extends RecyclerView.Adapter<ChoicesParentRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private Map<String,String> choices = new HashMap<>();
    private Context context;
    private Integer numChoices;
    private String incomingDealId;

    public ChoicesParentRecyclerViewAdapter(Map<String,String> choices, Integer numChoices, String incomingDealId, Context context) {
        this.choices = choices;
        this.context = context;
        this.numChoices = numChoices;
        this.incomingDealId = incomingDealId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View choiceParentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.choice_parent_listitem, parent, false);
        ViewHolder choiceParentViewHolder = new ViewHolder(choiceParentView);
        return choiceParentViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        if(choices.containsKey(String.valueOf(position))) {
            Log.d(TAG,"We made it inside");
            holder.choiceParentTextView.setText(choices.get(String.valueOf(position)));
        } else {
            holder.choiceParentTextView.setText("Choice " + (position + 1));
        }

        holder.choiceParentRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("goto", "ChoicesParentActivity");
                Intent intent = new Intent(context, ChoicesChildActivity.class);
                intent.putExtra("incomingDealId", incomingDealId);
                intent.putExtra("incomingSection", String.valueOf(position));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return numChoices;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView choiceParentTextView;
        RelativeLayout choiceParentRelativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            choiceParentTextView = itemView.findViewById(R.id.text_view_choice_parent);
            choiceParentRelativeLayout = itemView.findViewById(R.id.relative_layout_choice_parent_cell);
        }

    }
}
