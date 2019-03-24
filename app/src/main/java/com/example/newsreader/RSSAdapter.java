package com.example.newsreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;








public class RSSAdapter extends RecyclerView.Adapter<RSSAdapter.RSSViewHolder> implements Filterable{

    private List<RSSObject> RSSObjectList;
    private List<RSSObject> RSSObjectListFull;
    private Context mContext;



    public static class RSSViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private View RSSView;
        private ItemClickListener itemClickListener;


        public RSSViewHolder(View v) {
            super(v);
            RSSView = v;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(),false);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),true);
            return true;        }
    }

    public RSSAdapter(List<RSSObject> rssObjects, Context mContext) {
        this.RSSObjectList = rssObjects;
        this.mContext = mContext;
        RSSObjectListFull = new ArrayList<>(RSSObjectList);


    }

    @Override
    public RSSViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        RSSViewHolder holder = new RSSViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RSSViewHolder holder, int position) {

        final RSSObject rssObject = RSSObjectList.get(holder.getAdapterPosition());
        ((TextView)holder.RSSView.findViewById(R.id.txtTitle)).setText(rssObject.title);
        ((TextView)holder.RSSView.findViewById(R.id.txtContent)).setText(rssObject.description);
        ImageView imageView = holder.RSSView.findViewById(R.id.imgView);
        Picasso.with(mContext)
                .load(rssObject.getImageURL())
                .into(imageView);

        holder.setItemClickListener(new ItemClickListener(){
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (!isLongClick)
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(RSSObjectList.get(position).getLink()));
                    Log.d("INTENT", browserIntent.toString());
                    mContext.startActivity(browserIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

       return RSSObjectList.size();

    }

    @Override
    public Filter getFilter() {
        return RSSFilter;
    }
    private Filter RSSFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<RSSObject> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(RSSObjectListFull);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (RSSObject obj : RSSObjectListFull) {
                    if (obj.getTitle().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            RSSObjectList.clear();
            RSSObjectList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
