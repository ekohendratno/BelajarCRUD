package id.kopas.firebase.belajarcrud;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<Siswa> siswaArrayList;
    private Context context;

    public interface dataListener{
        void onDeleteData(Siswa data, int position);
        void onEditData(Siswa data, int position);
    }

    dataListener listener;

    public RecyclerViewAdapter(ArrayList<Siswa> siswaArrayList, Context context) {
        this.siswaArrayList = siswaArrayList;
        this.context = context;
        listener = (MainActivity)context;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nama, alamat;
        private ImageView actEdit, actDel;

        ViewHolder(View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.nama);
            alamat = itemView.findViewById(R.id.alamat);
            actEdit = itemView.findViewById(R.id.actEdit);
            actDel = itemView.findViewById(R.id.actDel);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item1, parent, false);
        return new ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String nama = siswaArrayList.get(position).nama;
        final String alamat = siswaArrayList.get(position).alamat;

        holder.nama.setText("Nama: "+nama);
        holder.alamat.setText("Alamat: "+alamat);

        holder.actEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditData(siswaArrayList.get(position), position);

            }
        });

        holder.actDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setMessage("Yakin mau hapus data?");
                alert.setPositiveButton("Yakin", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onDeleteData(siswaArrayList.get(position), position);
                    }
                });
                alert.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();

            }
        });

        /**
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final String[] action = {"Update", "Delete"};
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setItems(action,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                break;
                            case 1:
                                break;
                        }
                    }
                });
                alert.create();
                alert.show();
                return true;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return siswaArrayList.size();
    }

}