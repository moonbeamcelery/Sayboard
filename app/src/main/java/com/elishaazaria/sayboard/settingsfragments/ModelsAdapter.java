package com.elishaazaria.sayboard.settingsfragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elishaazaria.sayboard.Model;
import com.elishaazaria.sayboard.ModelLink;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;

import java.util.List;
import java.util.Locale;

public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.ViewHolder> {

    public enum DataState {
        CLOUD, INSTALLED, DOWNLOADING, QUEUED
    }

    public static class Data {
        private ModelLink modelLink;
        private Model model;
        private DataState state;

        public Data(ModelLink modelLink) {
            this.modelLink = modelLink;
            this.model = null;
            state = DataState.CLOUD;
        }

        public Data(Model model) {
            this.modelLink = null;
            this.model = model;
            state = DataState.INSTALLED;
        }

        public Data(ModelLink modelLink, Model model) {
            this.modelLink = modelLink;
            this.model = model;
            state = DataState.INSTALLED;
        }

        public String getFilename() {
            if (modelLink != null) {
                return modelLink.getFilename();
            } else if (model != null) {
                return model.filename;
            } else {
                return "Undefined";
            }
        }

        public Locale getLocale() {
            if (model != null) {
                return model.locale;
            } else if (modelLink != null) {
                return modelLink.locale;
            } else return Locale.forLanguageTag("und");
        }

        public void wasInstalled(Model model) {
            this.model = model;
            state = DataState.INSTALLED;
        }

        public boolean wasDeleted() {
            this.model = null;
            state = DataState.CLOUD;
            return this.modelLink == null;
        }

        public void wasQueued() {
            state = DataState.QUEUED;
        }

        public void downloading() {
            state = DataState.DOWNLOADING;
        }

        public void downloadCanceled() {
            if (state == DataState.DOWNLOADING)
                state = DataState.CLOUD;
        }

        public DataState getState() {
            return state;
        }

        public ModelLink getModelLink() {
            return modelLink;
        }

        public Model getModel() {
            return model;
        }
    }

    private final Context context;
    private final List<Data> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    ModelsAdapter(Context context, List<Data> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fragment_models_entry, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = mData.get(position);
        holder.titleTextView.setText(data.getLocale().getDisplayName());
        holder.subtitleTextView.setText(data.getFilename());

        switch (data.getState()) {
            case CLOUD:
                holder.downloadButton.setImageResource(R.drawable.ic_download);
                break;
            case INSTALLED:
                holder.downloadButton.setImageResource(R.drawable.ic_delete);
                break;
            case DOWNLOADING:
                holder.downloadButton.setImageResource(R.drawable.ic_downloading);
                break;
            case QUEUED:
                holder.downloadButton.setImageResource(R.drawable.ic_add_circle_outline);
                break;
        }

        holder.data = data;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subtitleTextView;
        ImageButton downloadButton;
        Data data;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            downloadButton = itemView.findViewById(R.id.downloadButton);

            downloadButton.setOnClickListener(this::onButtonClick);
            itemView.setOnClickListener(this::onClick);
        }

        private void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition(), data);
        }

        private void onButtonClick(View view) {
            if (mClickListener != null) {
                mClickListener.onButtonClicked(view, getAdapterPosition(), data);
            }
        }
    }

    // convenience method for getting data at click position
    Data getItem(int id) {
        return mData.get(id);
    }

    public Data get(ModelInfo modelInfo) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getFilename().equals(modelInfo.filename)) {
                return mData.get(i);
            }
        }
        return null;
    }

    public boolean changed(Data data) {
        int index = mData.indexOf(data);
        if (index == -1) return false;
        notifyItemChanged(index);
        return true;
    }

    public boolean removed(Data data) {
        int index = mData.indexOf(data);
        if (index == -1) return false;
        mData.remove(index);
        notifyItemRemoved(index);
        return true;
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, Data data);

        void onButtonClicked(View view, int position, Data data);
    }
}
